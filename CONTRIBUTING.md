# Contributing to GitOps Orchestration

Thank you for your interest in contributing to the GitOps Orchestration project!

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/opsgit.git`
3. Create a feature branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes
6. Commit and push
7. Create a Pull Request

## Development Setup

### Prerequisites

- Groovy 3.0+
- Git 2.0+
- Docker and Docker Compose (for testing)
- Make

### Installing Dependencies

```bash
# Install Groovy
sdk install groovy

# Or on Ubuntu/Debian
sudo apt-get install groovy

# Or on macOS
brew install groovy
```

## Testing Your Changes

### Run Validation

```bash
./validate.sh
```

### Run Integration Tests

```bash
./test.sh
```

### Manual Testing

1. Create a test deployment repository:
```bash
mkdir /tmp/test-repo
cd /tmp/test-repo
git init
# Add compose.yml, .env, etc.
```

2. Update `gitops-config.json` to point to your test repo

3. Run the orchestrator:
```bash
groovy gitops.groovy
```

## Code Style

### Groovy Code Style

- Use 4 spaces for indentation
- Follow Groovy naming conventions
- Add comments for complex logic
- Keep methods focused and small

### Shell Script Style

- Use bash for shell scripts
- Add error handling with `set -e`
- Include descriptive echo statements
- Make scripts executable with proper shebang

### Documentation Style

- Use Markdown for all documentation
- Include code examples
- Add clear section headers
- Keep explanations concise

## Project Structure

```
opsgit/
├── gitops.groovy           # Main orchestration script
├── gitops-config.json      # Configuration file
├── Makefile                # Docker Compose update targets
├── README.md               # Main documentation
├── QUICKSTART.md           # Getting started guide
├── ARCHITECTURE.md         # Technical documentation
├── CONTRIBUTING.md         # This file
├── LICENSE                 # MIT License
├── .gitignore             # Git ignore rules
├── validate.sh            # Validation script
├── test.sh                # Integration tests
├── demo.sh                # Demo script
└── examples/              # Example files
    └── deployment-repo/
```

## Adding New Features

### Adding a New Configuration Option

1. Update `gitops-config.json` with the new option
2. Update the `loadConfig()` method in `gitops.groovy`
3. Document the new option in README.md
4. Add validation in `validate.sh` if needed

### Adding a New Change Detection Method

1. Add a new method to the `GitOpsOrchestrator` class
2. Call it from `identifyChanges()`
3. Update `ARCHITECTURE.md` with the algorithm
4. Add tests in `test.sh`

### Adding Support for New Services

1. Add service to `examples/deployment-repo/compose.yml`
2. Add make target to `Makefile`
3. Add service to default config in `gitops.groovy`
4. Update documentation

## Testing Guidelines

### What to Test

- Checksum calculation accuracy
- Change detection logic
- Git operations
- Make target execution
- Error handling
- Edge cases (missing files, empty directories, etc.)

### Writing Tests

Add tests to `test.sh`:

```bash
# Test description
echo ""
echo "Test X: Description"
echo "===================="
cd "$DEPLOYMENT_REPO"
# Make changes
git add . && git commit -m "Test change"

cd "$SCRIPT_DIR"
groovy gitops.groovy "$TEST_CONFIG" | tee /tmp/test-output.txt

if grep -q "Expected output" /tmp/test-output.txt; then
    echo "✓ Test passed"
else
    echo "✗ Test failed"
    exit 1
fi
```

## Documentation

### Updating Documentation

When making changes, update relevant documentation:

- **README.md**: User-facing features and usage
- **QUICKSTART.md**: Setup and getting started
- **ARCHITECTURE.md**: Technical details and algorithms
- **Code comments**: Complex logic explanation

### Documentation Standards

- Use clear, concise language
- Include examples
- Add code blocks with syntax highlighting
- Keep sections organized with headers

## Pull Request Process

1. **Before submitting:**
   - Run `./validate.sh` and ensure it passes
   - Run `./test.sh` and ensure all tests pass
   - Update documentation
   - Add tests for new features

2. **Pull Request Template:**
   ```markdown
   ## Description
   Brief description of changes

   ## Changes Made
   - Change 1
   - Change 2

   ## Testing
   - [ ] Validation passed
   - [ ] Integration tests passed
   - [ ] Manual testing completed

   ## Documentation
   - [ ] README updated
   - [ ] ARCHITECTURE updated (if needed)
   - [ ] Code comments added
   ```

3. **Review Process:**
   - Maintainers will review your PR
   - Address any requested changes
   - Once approved, PR will be merged

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Accept constructive criticism
- Focus on what's best for the project
- Show empathy towards other contributors

### Unacceptable Behavior

- Harassment or discriminatory language
- Trolling or insulting comments
- Publishing others' private information
- Other unprofessional conduct

## Getting Help

- Open an issue for bugs or feature requests
- Ask questions in issue discussions
- Check existing issues and documentation first

## Recognition

Contributors will be recognized in:
- Git commit history
- Release notes
- Contributors section (coming soon)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
