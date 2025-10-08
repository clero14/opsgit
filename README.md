# opsgit

GitOps orchestration tool for Docker Compose based infrastructure.

## Overview

This project orchestrates Docker Compose based infrastructure by monitoring a deployment repository for changes and automatically triggering updates to affected services.

## Features

- **Automated Git Monitoring**: Pulls latest changes from the deployment repository
- **Intelligent Change Detection**: Uses checksums to identify what has changed
- **Selective Updates**: Only updates services that have actually changed
- **Full Stack Updates**: Triggers complete redeployment when core files change
- **Service-Specific Updates**: Updates individual services when only their configuration changes

## Architecture

The system works in three main steps:

1. **Git Pull**: Fetches the latest changes from the deployment repository
2. **Identify Changes**: Calculates checksums for files and directories to detect what changed
3. **Update Changed Parts**: Triggers appropriate make targets based on detected changes

### Change Detection Logic

- **Full Update Trigger**: If `compose.yml` or `.env` files change, the entire stack is redeployed (`make gitopsAll`)
- **Service Update Trigger**: If only a service directory (e.g., `logstash/`) changes, only that service is updated (`make gitopsLogstash`)

## Requirements

- Groovy (for running the orchestration script)
- Git
- Docker and Docker Compose
- Make

## Installation

1. Clone this repository:
```bash
git clone https://github.com/clero14/opsgit.git
cd opsgit
```

2. Install Groovy if not already installed:
```bash
# On Ubuntu/Debian
sudo apt-get install groovy

# On macOS
brew install groovy
```

## Configuration

Create or edit `gitops-config.json`:

```json
{
    "deploymentRepo": "/path/to/deployment-repo",
    "composeFile": "compose.yml",
    "envFile": ".env",
    "services": ["logstash", "elasticsearch", "kibana"]
}
```

### Configuration Options

- `deploymentRepo`: Path to the git repository containing your docker-compose configuration
- `composeFile`: Name of the docker-compose file (default: `compose.yml`)
- `envFile`: Name of the environment file (default: `.env`)
- `services`: Array of service names that have additional configuration directories

## Deployment Repository Structure

Your deployment repository should follow this structure:

```
deployment-repo/
├── compose.yml          # Docker Compose configuration
├── .env                 # Environment variables
├── logstash/           # Service-specific configuration directory
│   ├── pipeline/
│   │   └── logstash.conf
│   └── config/
│       └── logstash.yml
├── elasticsearch/      # Another service directory (optional)
└── kibana/            # Another service directory (optional)
```

### Service Configuration

Services with additional configuration files should have a directory with the same name as the service. For example:
- Service name: `logstash`
- Configuration directory: `logstash/`

## Makefile Targets

The `Makefile` defines how updates are performed:

- `make gitopsAll` - Full update of all services (stops, pulls, restarts everything)
- `make gitopsLogstash` - Update only the Logstash service
- `make gitopsElasticsearch` - Update only the Elasticsearch service
- `make gitopsKibana` - Update only the Kibana service

You can customize these targets or add new ones for your specific services.

## Usage

### Running the Orchestrator

```bash
# Using default config file (gitops-config.json)
groovy gitops.groovy

# Using custom config file
groovy gitops.groovy /path/to/custom-config.json
```

### Running Manually

You can also manually trigger updates in your deployment repository:

```bash
cd /path/to/deployment-repo

# Full update
make gitopsAll

# Service-specific update
make gitopsLogstash
```

### Automated Execution

For continuous monitoring, you can set up a cron job:

```bash
# Run every 5 minutes
*/5 * * * * cd /path/to/opsgit && groovy gitops.groovy >> /var/log/gitops.log 2>&1
```

Or use a systemd timer for more robust scheduling.

## How It Works

1. **First Run**: The script calculates checksums for all monitored files and directories and saves them to `.gitops_checksums.json` in the deployment repository

2. **Subsequent Runs**:
   - Pulls latest changes from git
   - Calculates new checksums
   - Compares with previous checksums
   - Triggers appropriate make targets based on what changed
   - Saves new checksums for next run

3. **Checksum Storage**: The `.gitops_checksums.json` file stores SHA-256 checksums:
   - `compose`: Checksum of compose.yml
   - `env`: Checksum of .env
   - `service_<name>`: Checksum of each service directory

## Example Workflow

1. Developer updates `logstash/pipeline/logstash.conf` in the deployment repository
2. Developer commits and pushes changes
3. GitOps orchestrator runs (via cron or manual execution)
4. Script performs `git pull`
5. Calculates checksum of `logstash/` directory
6. Detects change in logstash directory
7. Executes `make gitopsLogstash`
8. Only Logstash service is restarted with new configuration
9. Checksum is saved for future comparison

## Example Files

See the `examples/deployment-repo/` directory for sample configuration files:
- `compose.yml` - Example Docker Compose file with ELK stack
- `.env` - Example environment variables
- `logstash/` - Example service configuration directory

## Troubleshooting

### Script doesn't detect changes

- Check that the deployment repository path is correct in `gitops-config.json`
- Verify that `.gitops_checksums.json` exists in the deployment repository
- Run with verbose output to see checksum calculations

### Make targets fail

- Ensure Docker and Docker Compose are installed and running
- Check that you're in the correct directory (deployment repository)
- Verify that docker-compose.yml syntax is valid

### Permission issues

- Ensure the script has read/write access to the deployment repository
- Check that the user running the script can execute docker commands

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

This project is open source and available under the MIT License.