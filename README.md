# Let's Play - Spring Boot CRUD API

A production-ready RESTful CRUD API built with Spring Boot and MongoDB for user and product management. Features JWT authentication, role-based authorization, and comprehensive security measures.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-4.4+-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## Features

- **RESTful API** - Clean, resource-oriented endpoints
- **JWT Authentication** - Token-based authentication with 15-minute expiration
- **Role-Based Authorization** - ADMIN and USER roles with granular permissions
- **MongoDB Integration** - NoSQL database for flexible data storage
- **Password Security** - BCrypt hashing with strength 12
- **Public Endpoints** - GET /api/products accessible without authentication
- **Complete CRUD** - Full Create, Read, Update, Delete operations
- **CORS & Rate Limiting** - Cross-origin support and brute force protection
- **17 Test Files** - Comprehensive test coverage with 80%+ code coverage
- **Insomnia Collection** - 31 pre-configured API requests with auto token extraction

---

## Quick Start

### Prerequisites

- **Java 17+**
- **Docker & Docker Compose**
- **Git**

### 1. Clone & Setup

```bash
git clone <repository-url>
cd lets-play

# Copy environment file
cp .env.example .env
```

### 2. Configure .env

Edit `.env` and set the required variables:

```bash
# Generate JWT secret (required)
openssl rand -base64 32

# Then set in .env:
JWT_SECRET=<your-generated-secret>
SPRING_DATA_MONGODB_URI=mongodb://root:password@mongodb:27017/letsplay?authSource=admin
SPRING_DATA_MONGODB_DATABASE=letsplay

# Optional
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
RATELIMIT_CAPACITY=5
RATELIMIT_REFILL_MINUTES=1
```

### 3. Launch with Make (Recommended)

Use the Makefile for quick commands:

```bash
# View all available commands
make help

# Start MongoDB + API
make start

# Start with Traefik Reverse Proxy
make traefik
```

**Alternative: Docker Compose**

```bash
# Basic setup (MongoDB + API)
docker compose up -d

# With Traefik override
docker compose -f docker-compose.yml -f docker-compose.traefik.yml up -d
```

Application runs on `http://localhost:8080` (or `https://api.yourdomain.com` with Traefik)

**Traefik Note:** Requires Traefik running. See [Traefik Setup Guide](https://github.com/nicgen/Traefik-Reverse-Proxy)

### 4. Verify Setup

```bash
# Check containers
docker compose ps

# View logs
docker compose logs -f api

# Test API
curl http://localhost:8080/api/products
```

---

## Next Steps

- **API Documentation** - See [DOC/API.md](DOC/API.md)
- **Testing & Insomnia** - See [DOC/TESTING.md](DOC/TESTING.md)
- **Security Details** - See [DOC/SECURITY.md](DOC/SECURITY.md)
- **Production Deployment** - See [DOC/DEPLOYMENT.md](DOC/DEPLOYMENT.md)
- **Configuration Reference** - See [DOC/CONFIGURATION.md](DOC/CONFIGURATION.md)

---

## Common Commands

### Using Make (Recommended)

```bash
make help          # Show all available commands
make start         # Start MongoDB + API
make stop          # Stop all services
make restart       # Restart all services
make logs          # View logs
make build         # Rebuild and start
make test          # Run tests
make dev           # Start MongoDB only (for local development)
make traefik       # Start with Traefik
make clean         # Remove all containers and volumes
```

### Using Docker Compose

```bash
docker compose up -d       # Start services
docker compose down        # Stop services
docker compose logs -f api # View logs
```

### Using Maven

```bash
./mvnw test         # Run tests
./mvnw clean package # Build JAR
```

---

## License

MIT License - see [LICENSE](LICENSE) file for details.

Made with Spring Boot 3.5.6 | Java 17 | MongoDB
