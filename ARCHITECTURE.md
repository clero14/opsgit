# System Architecture

## Overview

The GitOps orchestration system is designed to automate the deployment and update of Docker Compose-based infrastructure by monitoring a git repository for changes.

## Components

### 1. gitops.groovy
The main orchestration script written in Groovy.

**Key Classes:**
- `GitOpsOrchestrator`: Main class that handles the orchestration logic

**Key Methods:**
- `loadConfig()`: Loads configuration from JSON file
- `loadPreviousChecksums()`: Loads previously saved checksums
- `calculateChecksum()`: Computes SHA-256 checksum for files
- `calculateDirectoryChecksum()`: Computes checksum for directories
- `gitPull()`: Executes git pull in the deployment repository
- `identifyChanges()`: Detects what has changed by comparing checksums
- `triggerUpdates()`: Executes appropriate make targets based on changes
- `savePreviousChecksums()`: Persists checksums for next run

### 2. gitops-config.json
Configuration file that specifies:
- `deploymentRepo`: Path to the git repository being monitored
- `composeFile`: Name of the docker-compose file
- `envFile`: Name of the environment variables file
- `services`: Array of service names with additional configuration

### 3. Makefile
Defines how to perform updates:
- `gitopsAll`: Full stack update (all services)
- `gitops<ServiceName>`: Individual service updates

### 4. Deployment Repository Structure
```
deployment-repo/
├── compose.yml           # Main compose file
├── .env                  # Environment variables
├── .gitops_checksums.json  # Auto-generated checksums
└── <service-name>/       # Service-specific configs
    └── config files...
```

## Workflow

### Step 1: Git Pull
```
gitPull()
    └── Executes: git pull in deployment repo
```

### Step 2: Identify Changes
```
identifyChanges()
    ├── Calculate compose.yml checksum
    ├── Calculate .env checksum
    ├── Calculate service directory checksums
    ├── Compare with previous checksums
    └── Determine:
        ├── Full update needed? (compose/env changed)
        └── Service-specific updates? (service dir changed)
```

### Step 3: Trigger Updates
```
triggerUpdates()
    ├── If full update:
    │   └── Execute: make gitopsAll
    └── If service updates:
        └── For each changed service:
            └── Execute: make gitops<ServiceName>
```

### Step 4: Save Checksums
```
savePreviousChecksums()
    └── Write checksums to .gitops_checksums.json
```

## Change Detection Algorithm

### Checksum Calculation

**File Checksum:**
```
SHA-256(file_content) → checksum
```

**Directory Checksum:**
```
For each file in directory (sorted):
    file_checksum = SHA-256(file_content)
    Append to digest
directory_checksum = SHA-256(all_file_checksums)
```

### Change Detection Logic

```
IF compose_checksum != previous_compose_checksum OR 
   env_checksum != previous_env_checksum:
    → Trigger full update (make gitopsAll)

ELSE FOR EACH service IN services:
    IF service_dir_checksum != previous_service_dir_checksum:
        → Trigger service update (make gitops<Service>)

ELSE:
    → No changes detected
```

## State Management

### Checksum Storage Format (.gitops_checksums.json)
```json
{
    "compose": "abc123...",
    "env": "def456...",
    "service_web": "ghi789...",
    "service_db": "jkl012..."
}
```

### State Lifecycle
1. **First Run**: No checksums exist → Calculate all → Save → Trigger full update
2. **Subsequent Runs**: Load checksums → Calculate new → Compare → Update changed → Save new

## Docker Compose Integration

### Full Update Flow
```
make gitopsAll
    ├── docker-compose down      (stop all)
    ├── docker-compose pull      (pull images)
    └── docker-compose up -d     (start all)
```

### Service-Specific Update Flow
```
make gitops<Service>
    ├── docker-compose stop <service>     (stop service)
    ├── docker-compose rm -f <service>    (remove container)
    ├── docker-compose pull <service>     (pull image)
    └── docker-compose up -d <service>    (start service)
```

## Configuration Management

### Service Configuration Pattern

For a service with additional configuration:
1. Service name in compose.yml: `logstash`
2. Configuration directory: `logstash/`
3. Make target: `gitopsLogstash`
4. Config entry: `"services": ["logstash"]`

### Adding a New Service

1. Add service to compose.yml
2. Create service directory (if needed)
3. Add service to gitops-config.json services array
4. Add make target to Makefile

Example:
```makefile
gitopsRedis:
    @echo "==> Updating Redis service..."
    docker-compose stop redis
    docker-compose rm -f redis
    docker-compose pull redis
    docker-compose up -d redis
```

## Error Handling

### Git Pull Failures
- If `git pull` fails, the orchestration stops
- No updates are triggered
- Exit with error message

### Make Target Failures
- Full update failure: Logged but continues
- Service update failure: Logged, continues with next service
- Exit codes are captured and reported

### Checksum Calculation
- If file doesn't exist: Returns null
- If directory doesn't exist: Returns null
- Null checksums don't match previous, triggering updates

## Security Considerations

1. **File System Access**: Script needs read/write access to deployment repo
2. **Docker Access**: User must have docker/docker-compose permissions
3. **Git Credentials**: Must be configured for the deployment repository
4. **State File**: `.gitops_checksums.json` should not be in git (gitignored)

## Performance

### Checksum Calculation
- Files: O(n) where n is file size
- Directories: O(f * s) where f is number of files, s is average file size
- Typical execution time: < 1 second for small repos

### Git Operations
- Pull time depends on changes and network
- Typically < 5 seconds for small changes

### Update Operations
- Depends on Docker operations
- Image pulls can take minutes
- Service restarts typically < 30 seconds

## Scalability

- **Number of Services**: No practical limit, linear scaling
- **Repository Size**: Checksum calculation is efficient
- **Update Frequency**: Can run every minute if needed
- **Concurrent Runs**: Not supported, use locking if needed

## Extension Points

### Custom Change Detection
Modify `identifyChanges()` to add custom logic:
- Check file timestamps
- Parse file contents
- Use git diff instead of checksums

### Custom Update Logic
Modify `triggerUpdates()` or Makefile:
- Add pre/post hooks
- Add notifications (email, Slack, etc.)
- Add rollback logic

### Custom Checksum Algorithm
Replace SHA-256 with:
- MD5 (faster, less secure)
- Git blob hashing
- File timestamps

## Monitoring and Logging

### Log Output
The script outputs:
- Configuration loaded
- Git pull results
- Checksums calculated
- Changes detected
- Make commands executed
- Success/failure status

### Integration with Log Systems
Redirect output to logging system:
```bash
groovy gitops.groovy | logger -t gitops
groovy gitops.groovy >> /var/log/gitops.log 2>&1
```

## Testing Strategy

### Unit Testing
- Test checksum calculation
- Test change detection logic
- Mock git and make commands

### Integration Testing
- Use test repository
- Mock docker commands
- Verify correct make targets called

### End-to-End Testing
- Real git repository
- Real docker-compose setup
- Verify services actually update

## Troubleshooting Guide

### Problem: Changes not detected
**Possible causes:**
- Checksums file is stale
- Git pull didn't fetch changes
- Changes not committed

**Solutions:**
- Delete `.gitops_checksums.json`
- Check git status
- Verify commits exist

### Problem: Wrong service updated
**Possible causes:**
- Service name mismatch
- Multiple files changed

**Solutions:**
- Check service names match in config and Makefile
- Review what changed: `git diff`

### Problem: Make target fails
**Possible causes:**
- Docker not running
- Service name incorrect
- compose.yml syntax error

**Solutions:**
- Start Docker daemon
- Verify service names
- Validate compose file: `docker-compose config`

## Future Enhancements

Potential improvements:
1. Parallel service updates
2. Rollback capability
3. Dry-run mode
4. Notification system
5. Web dashboard
6. Health checks before/after updates
7. Blue-green deployments
8. Canary releases
9. Integration with CI/CD systems
10. Support for multiple deployment repositories
