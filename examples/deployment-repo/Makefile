# Makefile for GitOps orchestration
# This file defines how to handle updates for docker-compose services

.PHONY: help gitopsAll gitopsLogstash gitopsElasticsearch gitopsKibana

help:
	@echo "GitOps Makefile - Docker Compose Orchestration"
	@echo ""
	@echo "Available targets:"
	@echo "  gitopsAll          - Full update of all services"
	@echo "  gitopsLogstash     - Update Logstash service"
	@echo "  gitopsElasticsearch - Update Elasticsearch service"
	@echo "  gitopsKibana       - Update Kibana service"
	@echo ""
	@echo "Usage: make <target>"

# Full update - restart all services with new configuration
gitopsAll:
	@echo "==> Performing full GitOps update..."
	@echo "==> Stopping all services..."
	docker-compose down
	@echo "==> Pulling latest images..."
	docker-compose pull
	@echo "==> Starting all services..."
	docker-compose up -d
	@echo "==> Full update completed"

# Update Logstash service specifically
gitopsLogstash:
	@echo "==> Updating Logstash service..."
	@echo "==> Stopping Logstash..."
	docker-compose stop logstash
	docker-compose rm -f logstash
	@echo "==> Pulling latest Logstash image..."
	docker-compose pull logstash
	@echo "==> Starting Logstash..."
	docker-compose up -d logstash
	@echo "==> Logstash update completed"

# Update Elasticsearch service specifically
gitopsElasticsearch:
	@echo "==> Updating Elasticsearch service..."
	@echo "==> Stopping Elasticsearch..."
	docker-compose stop elasticsearch
	docker-compose rm -f elasticsearch
	@echo "==> Pulling latest Elasticsearch image..."
	docker-compose pull elasticsearch
	@echo "==> Starting Elasticsearch..."
	docker-compose up -d elasticsearch
	@echo "==> Elasticsearch update completed"

# Update Kibana service specifically
gitopsKibana:
	@echo "==> Updating Kibana service..."
	@echo "==> Stopping Kibana..."
	docker-compose stop kibana
	docker-compose rm -f kibana
	@echo "==> Pulling latest Kibana image..."
	docker-compose pull kibana
	@echo "==> Starting Kibana..."
	docker-compose up -d kibana
	@echo "==> Kibana update completed"
