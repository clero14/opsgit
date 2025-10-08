#!/bin/bash
# Validation script to check if the GitOps orchestration system is properly set up

echo "========================================"
echo "GitOps System Validation"
echo "========================================"
echo ""

ERRORS=0

# Check if required files exist
echo "Checking required files..."
FILES=(
    "gitops.groovy"
    "gitops-config.json"
    "Makefile"
    "README.md"
    ".gitignore"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file exists"
    else
        echo "  ✗ $file missing"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo "Checking example files..."
EXAMPLE_FILES=(
    "examples/deployment-repo/compose.yml"
    "examples/deployment-repo/.env"
    "examples/deployment-repo/logstash/pipeline/logstash.conf"
    "examples/deployment-repo/logstash/config/logstash.yml"
)

for file in "${EXAMPLE_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file exists"
    else
        echo "  ✗ $file missing"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo "Checking executability..."
if [ -x "gitops.groovy" ]; then
    echo "  ✓ gitops.groovy is executable"
else
    echo "  ✗ gitops.groovy is not executable"
    ERRORS=$((ERRORS + 1))
fi

if [ -x "demo.sh" ]; then
    echo "  ✓ demo.sh is executable"
else
    echo "  ✗ demo.sh is not executable"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "Checking Groovy syntax..."
if command -v groovy &> /dev/null; then
    echo "  ✓ Groovy is installed"
    if groovy -e "println 'Groovy is working'" &> /dev/null; then
        echo "  ✓ Groovy is working"
    else
        echo "  ✗ Groovy test failed"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "  ⚠ Groovy is not installed (optional for validation)"
    echo "    Install with: sudo apt-get install groovy (Linux)"
    echo "    or: brew install groovy (macOS)"
fi

echo ""
echo "Checking required tools..."
TOOLS=(
    "git"
    "make"
    "sha256sum"
)

for tool in "${TOOLS[@]}"; do
    if command -v "$tool" &> /dev/null; then
        echo "  ✓ $tool is available"
    else
        echo "  ✗ $tool is not available"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo "Validating JSON configuration..."
if command -v python3 &> /dev/null; then
    if python3 -c "import json; json.load(open('gitops-config.json'))" 2>/dev/null; then
        echo "  ✓ gitops-config.json is valid JSON"
    else
        echo "  ✗ gitops-config.json has invalid JSON syntax"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "  ⚠ Python3 not available, skipping JSON validation"
fi

echo ""
echo "Checking Makefile syntax..."
if make -n help &> /dev/null; then
    echo "  ✓ Makefile syntax is valid"
else
    echo "  ✗ Makefile has syntax errors"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "========================================"
if [ $ERRORS -eq 0 ]; then
    echo "✓ All validations passed!"
    echo "========================================"
    echo ""
    echo "System is ready to use. See README.md for usage instructions."
    exit 0
else
    echo "✗ Validation failed with $ERRORS error(s)"
    echo "========================================"
    exit 1
fi
