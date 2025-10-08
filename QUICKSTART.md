# Quick Start Guide

This guide will help you get started with the GitOps orchestration system quickly.

## Prerequisites

1. Install Groovy:
   ```bash
   # Ubuntu/Debian
   sudo apt-get update
   sudo apt-get install groovy
   
   # macOS
   brew install groovy
   
   # Or use SDKMAN
   curl -s "https://get.sdkman.io" | bash
   sdk install groovy
   ```

2. Ensure Docker and Docker Compose are installed:
   ```bash
   docker --version
   docker-compose --version
   ```

## Setup Steps

### 1. Prepare Your Deployment Repository

Create a git repository with your Docker Compose infrastructure:

```bash
mkdir -p /path/to/deployment-repo
cd /path/to/deployment-repo
git init

# Create your compose.yml
cat > compose.yml << 'EOF'
version: '3.8'
services:
  myapp:
    image: nginx:latest
    ports:
      - "80:80"
EOF

# Create your .env file
cat > .env << 'EOF'
APP_VERSION=latest
EOF

# Create service-specific config (if needed)
mkdir -p myapp
echo "# Service config" > myapp/config.conf

# Commit initial state
git add .
git commit -m "Initial infrastructure"
```

### 2. Configure GitOps Orchestrator

Edit `gitops-config.json` in the opsgit directory:

```json
{
    "targetDir": "/path/to/deployment-repo"
}
```

**Note:** Services are now auto-detected! No need to list them in the configuration.

### 3. Set Up Makefile in Deployment Repository

Your deployment repository needs a Makefile with gitops targets. You can use the example as a starting point:

```bash
# Copy the example Makefile
cp /path/to/opsgit/examples/deployment-repo/Makefile /path/to/deployment-repo/

# Edit Makefile to match your services
cd /path/to/deployment-repo
nano Makefile
```

Add targets for your services following the naming convention `gitops<ServiceName>`:

```makefile
gitopsMyapp:
    @echo "==> Updating Myapp service..."
    docker-compose stop myapp
    docker-compose rm -f myapp
    docker-compose pull myapp
    docker-compose up -d myapp
```

### 4. Run Initial Orchestration

```bash
cd /path/to/opsgit
groovy gitops.groovy
```

This first run will:
- Pull latest changes from git (if the target directory is a git repository)
- Calculate initial checksums for compose.yml, .env, and any service directories
- Save checksums to `.gitops_checksums.json` in your target directory
- Trigger updates if changes are detected

### 5. Test Change Detection

Make a change in your deployment repository:

```bash
cd /path/to/deployment-repo
echo "# Updated config" >> myapp/config.conf
git add .
git commit -m "Update myapp config"
```

Run the orchestrator again:

```bash
cd /path/to/opsgit
groovy gitops.groovy
```

You should see it detect the change and trigger `make gitopsMyapp`.

## Automation

### Using Cron

Add to your crontab (`crontab -e`):

```cron
# Run every 5 minutes
*/5 * * * * cd /path/to/opsgit && groovy gitops.groovy >> /var/log/gitops.log 2>&1
```

### Using Systemd Timer

Create `/etc/systemd/system/gitops.service`:

```ini
[Unit]
Description=GitOps Orchestration
After=network.target

[Service]
Type=oneshot
WorkingDirectory=/path/to/opsgit
ExecStart=/usr/bin/groovy gitops.groovy
User=youruser
StandardOutput=journal
StandardError=journal
```

Create `/etc/systemd/system/gitops.timer`:

```ini
[Unit]
Description=GitOps Orchestration Timer
Requires=gitops.service

[Timer]
OnCalendar=*:0/5
Persistent=true

[Install]
WantedBy=timers.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable gitops.timer
sudo systemctl start gitops.timer
sudo systemctl status gitops.timer
```

## Troubleshooting

### Check Checksums

View the current checksums:

```bash
cd /path/to/deployment-repo
cat .gitops_checksums.json
```

### Force Full Update

Delete the checksums file to force a full recalculation:

```bash
cd /path/to/deployment-repo
rm .gitops_checksums.json
```

### Test Make Targets

Test individual targets manually:

```bash
cd /path/to/deployment-repo
make -n gitopsAll  # Dry run
make gitopsAll     # Actual run
```

## Next Steps

- Customize the Makefile in your deployment repository for your specific services
- The orchestrator will automatically detect any service directories you add
- Set up monitoring and alerting for the orchestration runs
- Review logs regularly to ensure smooth operation

For more detailed information, see the main [README.md](README.md).
