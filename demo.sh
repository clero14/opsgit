#!/bin/bash
# Demo script to show how the GitOps orchestrator works

set -e

echo "========================================"
echo "GitOps Orchestrator Demo"
echo "========================================"
echo ""

# Create a test deployment repository
TEST_REPO="/tmp/test-deployment-repo"
echo "1. Creating test deployment repository at $TEST_REPO"

if [ -d "$TEST_REPO" ]; then
    echo "   Cleaning up existing test repo..."
    rm -rf "$TEST_REPO"
fi

mkdir -p "$TEST_REPO"
cd "$TEST_REPO"

# Initialize git repo
git init
git config user.email "test@example.com"
git config user.name "Test User"

# Copy example files
echo "2. Setting up example files..."
cp -r ../examples/deployment-repo/* .
cp ../Makefile .

# Create initial commit
git add .
git commit -m "Initial commit"

echo ""
echo "3. Creating test configuration..."
cat > ../test-config.json << EOF
{
    "deploymentRepo": "$TEST_REPO",
    "composeFile": "compose.yml",
    "envFile": ".env",
    "services": ["logstash"]
}
EOF

echo ""
echo "4. Running GitOps orchestrator (first run)..."
cd /home/runner/work/opsgit/opsgit
groovy gitops.groovy test-config.json

echo ""
echo "5. Simulating a change to logstash configuration..."
cd "$TEST_REPO"
echo "# Updated configuration" >> logstash/pipeline/logstash.conf
git add .
git commit -m "Update logstash configuration"

echo ""
echo "6. Running GitOps orchestrator (detecting changes)..."
cd /home/runner/work/opsgit/opsgit
groovy gitops.groovy test-config.json

echo ""
echo "========================================"
echo "Demo completed!"
echo "========================================"
echo ""
echo "Check the output above to see how the orchestrator:"
echo "  - Detected the change in the logstash directory"
echo "  - Would have triggered 'make gitopsLogstash'"
echo ""
echo "Test files are in: $TEST_REPO"
echo "Checksums are stored in: $TEST_REPO/.gitops_checksums.json"
