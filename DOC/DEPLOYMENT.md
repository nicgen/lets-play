# Deployment Guide

## Production Deployment Checklist

### Pre-Deployment

- [ ] Generate production JWT secret (`openssl rand -base64 32`)
- [ ] Set all required environment variables
- [ ] Configure MongoDB connection (use managed service)
- [ ] Update CORS allowed origins
- [ ] Review rate limiting settings
- [ ] Enable HTTPS/TLS certificates
- [ ] Configure MongoDB authentication with strong passwords
- [ ] Test all endpoints in staging environment
- [ ] Verify MongoDB indexes are created
- [ ] Review and optimize application.properties for production
- [ ] Set up monitoring and alerting
- [ ] Plan backup and disaster recovery procedures
- [ ] Document deployment process
- [ ] Create runbook for common issues

---

## Docker Deployment

### Quick Start with Docker Compose

#### 1. Copy and configure environment file

```bash
cp .env.example .env
```

Edit `.env` and set the required variables:

```bash
JWT_SECRET=<your-generated-secret>
SPRING_DATA_MONGODB_URI=mongodb://root:password@mongodb:27017/letsplay?authSource=admin
SPRING_DATA_MONGODB_DATABASE=letsplay
CORS_ALLOWED_ORIGINS=https://yourdomain.com
SPRING_PROFILES_ACTIVE=prod
```

#### 2. Start all services

```bash
docker compose up -d
```

#### 3. Check status

```bash
docker compose ps
docker compose logs -f api
```

#### 4. Verify health

```bash
curl http://localhost:8080/api/products
```

### Docker Compose Includes

- **Spring Boot API** - Multi-stage Maven build
- **MongoDB** - With authentication and volume persistence
- **Health Checks** - For both services
- **Automatic Restart** - Unless manually stopped

### Docker Compose Commands

```bash
# Start services in background
docker compose up -d

# Stop services (preserve data)
docker compose down

# Stop and remove volumes (DELETE DATA)
docker compose down -v

# View logs
docker compose logs -f api

# View specific container logs
docker compose logs api

# Execute command in container
docker compose exec api /bin/bash

# Restart service
docker compose restart api
```

---

## Deploying with Traefik Reverse Proxy

### Prerequisites

- Traefik instance running with Let's Encrypt configured
- See [Traefik Setup](https://github.com/nicgen/Traefik-Reverse-Proxy)
- External Docker network created: `docker network create traefik_net`
- Domain DNS configured and pointing to your server

### Configuration

#### 1. Update .env

```bash
DOMAIN_API=api.yourdomain.com
JWT_SECRET=<your-generated-secret>
SPRING_DATA_MONGODB_URI=mongodb://root:password@mongodb:27017/letsplay?authSource=admin
SPRING_DATA_MONGODB_DATABASE=letsplay
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

#### 2. Deploy with Traefik override

```bash
docker compose -f docker-compose.yml -f docker-compose.traefik.yml up -d
```

#### 3. Verify deployment

```bash
# Check container is running
docker compose ps

# View logs
docker compose logs -f api

# Test HTTPS access
curl https://api.yourdomain.com/api/products
```

### What Traefik Provides

- **Automatic SSL Certificates** - Via Let's Encrypt
- **HTTP to HTTPS Redirect** - Automatic
- **Load Balancing** - If multiple instances
- **Reverse Proxy** - Routing based on domain
- **Certificate Renewal** - Automatic

### Traefik Configuration Details

The following labels are configured in docker-compose.traefik.yml:

```yaml
labels:
  # Enable Traefik for this service
  - "traefik.enable=true"
  - "traefik.docker.network=traefik_net"

  # HTTP Router (redirect to HTTPS)
  - "traefik.http.routers.letsplay-http.rule=Host(`${DOMAIN_API}`)"
  - "traefik.http.routers.letsplay-http.entrypoints=web"
  - "traefik.http.routers.letsplay-http.middlewares=redirect-to-https"

  # HTTPS Router with Let's Encrypt
  - "traefik.http.routers.letsplay-https.rule=Host(`${DOMAIN_API}`)"
  - "traefik.http.routers.letsplay-https.entrypoints=websecure"
  - "traefik.http.routers.letsplay-https.tls=true"
  - "traefik.http.routers.letsplay-https.tls.certresolver=cloudflare"

  # Service port
  - "traefik.http.services.letsplay.loadbalancer.server.port=8080"
```

---

## Traditional JAR Deployment

### Build Production JAR

```bash
./mvnw clean package -DskipTests
```

JAR location: `target/api-0.0.1-SNAPSHOT.jar`

### Set Environment Variables

```bash
export JWT_SECRET=<production-secret>
export SPRING_DATA_MONGODB_URI=<production-mongodb-uri>
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
export SPRING_PROFILES_ACTIVE=prod
```

### Run Application

```bash
java -jar target/api-0.0.1-SNAPSHOT.jar
```

### Verify Deployment

```bash
# Check health
curl http://localhost:8080/actuator/health

# Test API
curl http://localhost:8080/api/products
```

### Process Management

Use a process manager for production:

**systemd service example:**
```ini
[Unit]
Description=Let's Play API
After=network.target

[Service]
Type=simple
User=api
WorkingDirectory=/opt/letsplay
Environment="JWT_SECRET=your-secret"
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java -jar api-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

---

## Cloud Deployment Options

### Heroku (Easiest)

```bash
# Install Heroku CLI
curl https://cli.heroku.com/install.sh | sh

# Login
heroku login

# Create app
heroku create lets-play-api

# Set environment variables
heroku config:set JWT_SECRET=<secret>
heroku config:set SPRING_PROFILES_ACTIVE=prod

# Deploy
git push heroku main

# View logs
heroku logs -f
```

### AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init -p java-17 lets-play-api

# Create environment
eb create production

# Deploy
eb deploy

# View logs
eb logs
```

### Google Cloud Run

```bash
# Build and push image
gcloud builds submit --tag gcr.io/PROJECT-ID/lets-play-api

# Deploy
gcloud run deploy lets-play-api \
  --image gcr.io/PROJECT-ID/lets-play-api \
  --platform managed \
  --region us-central1
```

### Azure App Service

```bash
# Create resource group
az group create --name lets-play --location eastus

# Create app service plan
az appservice plan create --name lets-play-plan \
  --resource-group lets-play --sku B1 --is-linux

# Create web app
az webapp create --resource-group lets-play \
  --plan lets-play-plan --name lets-play-api \
  --runtime "java:17-java17"

# Deploy JAR
az webapp up --name lets-play-api --jar-path target/api-0.0.1-SNAPSHOT.jar
```

### DigitalOcean App Platform

```bash
# Install doctl
cd ~
wget https://github.com/digitalocean/doctl/releases/download/v1.89.0/doctl-1.89.0-linux-amd64.tar.gz
tar xf ~/doctl-1.89.0-linux-amd64.tar.gz
mv ~/doctl /usr/local/bin

# Create app.yaml with your config
# Deploy
doctl apps create --spec app.yaml
```

### Kubernetes

```bash
# Build image
docker build -t lets-play-api:1.0.0 .

# Push to registry
docker push your-registry/lets-play-api:1.0.0

# Create deployment
kubectl create deployment lets-play --image=your-registry/lets-play-api:1.0.0

# Expose service
kubectl expose deployment lets-play --port=80 --target-port=8080 --type=LoadBalancer

# Check status
kubectl get deployments
kubectl get services
```

---

## Post-Deployment

### Verification Checklist

- [ ] Application starts successfully
- [ ] Health check endpoint responds (`/actuator/health`)
- [ ] Authentication flow works (register → login)
- [ ] Database connection established
- [ ] CORS configuration correct
- [ ] Rate limiting active
- [ ] HTTPS working (if applicable)
- [ ] Logs don't contain sensitive data

### Production Maintenance

- [ ] Monitor application logs
- [ ] Verify all endpoints respond correctly
- [ ] Set up automated backups
- [ ] Configure monitoring and alerting
- [ ] Document deployment process
- [ ] Create runbook for common issues
- [ ] Monitor resource usage (CPU, memory, disk)
- [ ] Track error rates and response times
- [ ] Regular security updates
- [ ] Periodic backup restoration tests

### Monitoring Setup

Monitor these key metrics:

- **Uptime** - Service availability
- **Response Time** - Latency of requests
- **Error Rate** - 4XX and 5XX errors
- **Database Latency** - MongoDB response times
- **Rate Limiting** - Number of rate-limited requests
- **Authentication Failures** - Failed login attempts

### Backup Procedures

#### MongoDB Backup

```bash
# Full backup
mongodump --uri="mongodb://root:password@localhost:27017/letsplay" \
  --out=/backup/letsplay-$(date +%Y%m%d)

# Backup to archive
tar -czf /backup/letsplay-$(date +%Y%m%d).tar.gz /backup/letsplay-$(date +%Y%m%d)

# Schedule with cron (daily at 2 AM)
0 2 * * * mongodump --uri="mongodb://root:password@localhost:27017/letsplay" --out=/backup/letsplay-$(date +\%Y\%m\%d)
```

#### Restore from Backup

```bash
mongorestore --uri="mongodb://root:password@localhost:27017/letsplay" \
  /backup/letsplay-YYYYMMDD
```

---

## Troubleshooting

### Common Issues

#### Application won't start

```bash
# Check logs
docker compose logs api

# Check environment variables
docker compose config | grep -A 5 'environment:'

# Verify MongoDB connection
docker compose logs mongodb
```

#### Database connection error

```bash
# Test MongoDB connectivity
docker compose exec mongodb mongosh -u root -p password

# Check MongoDB logs
docker compose logs mongodb
```

#### High memory usage

```bash
# Check container memory
docker stats

# Increase Java heap in Docker
# Edit docker-compose.yml JAVA_OPTS environment variable
```

#### Rate limiting too strict

Adjust in `.env`:
```
RATELIMIT_CAPACITY=10
RATELIMIT_REFILL_MINUTES=1
```

---

## Performance Optimization

### Database Indexing

Ensure MongoDB indexes are created:

```javascript
// Create indexes for common queries
db.users.createIndex({ "email": 1 })
db.products.createIndex({ "userId": 1 })
db.products.createIndex({ "name": "text" })
```

### Caching

Consider adding Redis for caching:

```yaml
cache:
  image: redis:alpine
  ports:
    - "6379:6379"
```

### Connection Pooling

MongoDB driver uses connection pooling automatically. Adjust pool settings if needed in `application.properties`:

```properties
spring.data.mongodb.pool.size=10
spring.data.mongodb.pool.min=5
```

---

## Security in Production

See [SECURITY.md](SECURITY.md) for comprehensive security guidelines including:
- JWT secret management
- HTTPS/TLS configuration
- Database authentication
- CORS setup
- Rate limiting

---

## Disaster Recovery

### Plan Components

1. **Backup Strategy** - Daily automated backups with off-site storage
2. **Recovery Procedures** - Tested procedures for data restoration
3. **Failover Plan** - Secondary instance setup
4. **Communication Plan** - Stakeholder notification procedures
5. **Testing** - Regular backup restoration tests

### RTO/RPO Goals

- **RTO (Recovery Time Objective):** < 1 hour
- **RPO (Recovery Point Objective):** < 1 hour
