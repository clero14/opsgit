# Makefile for GitOps Orchestrator Development
# This file is for developing the orchestrator itself
# Your deployment repository needs its own Makefile (see examples/deployment-repo/Makefile)

.PHONY: help test validate demo

help:
	@echo "GitOps Orchestrator - Development Commands"
	@echo ""
	@echo "Available targets:"
	@echo "  test       - Run integration tests"
	@echo "  validate   - Validate the configuration and setup"
	@echo "  demo       - Run a quick demo"
	@echo ""
	@echo "Note: Your deployment repository needs its own Makefile"
	@echo "      See examples/deployment-repo/Makefile for reference"

test:
	@echo "Running integration tests..."
	./test.sh

validate:
	@echo "Validating setup..."
	./validate.sh

demo:
	@echo "Running demo..."
	./demo.sh

