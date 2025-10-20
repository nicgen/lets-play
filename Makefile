# Makefile for Let's Play Spring Boot API

.PHONY: help start stop restart logs build test dev traefik

# Colors
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
NC := \033[0m

help: ## Show available commands
	@echo "$(BLUE)Let's Play API - Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-15s$(NC) %s\n", $$1, $$2}'

start: ## Start MongoDB + API
	@docker compose up -d
	@echo "$(GREEN)✓ Started at http://localhost:8080$(NC)"

stop: ## Stop all services
	@docker compose down

restart: ## Restart all services
	@docker compose restart

logs: ## View logs
	@docker compose logs -f

build: ## Rebuild and start
	@docker compose up -d --build

test: ## Run tests
	@./mvnw test

dev: ## Start MongoDB only (for local development)
	@docker compose up -d mongodb
	@echo "$(YELLOW)MongoDB started. Run:$(NC) ./mvnw spring-boot:run"

traefik: ## Start with Traefik (requires traefik_net network)
	@docker compose -f docker-compose.yml -f docker-compose.traefik.yml up -d
	@echo "$(GREEN)✓ Started with Traefik$(NC)"

clean: ## Remove all containers and volumes
	@docker compose down -v
