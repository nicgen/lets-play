# Configuration Guide

## Environment Variables

### Required Variables

| Variable | Example | Description |
|----------|---------|-------------|
| `JWT_SECRET` | `dGVzdFNlY3JldEtleUZvckpXVFRva2VuVmFsaWRhdGlvbjEyMzQ1Njc4OTA=` | JWT signing secret (256-bit, base64 encoded) |

### Database Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATA_MONGODB_URI` | `mongodb://root:password@mongodb:27017/letsplay?authSource=admin` | MongoDB connection string |
| `SPRING_DATA_MONGODB_DATABASE` | `letsplay` | Database name |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:4200` | Comma-separated allowed CORS origins |
| `RATELIMIT_CAPACITY` | `5` | Max requests per rate limit window |
| `RATELIMIT_REFILL_MINUTES` | `1` | Rate limit time window in minutes |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (dev/test/prod) |

### Traefik Variables

| Variable | Example | Description |
|----------|---------|-------------|
| `DOMAIN_API` | `api.yourdomain.com` | Domain for Traefik routing (only needed with Traefik) |

---

## Setting Environment Variables

### Option 1: .env File (Development)

Create `.env` in project root:

```bash
# Copy from template
cp .env.example .env

# Edit with your values
nano .env  # or vim .env
```

**Development Example:**
```bash
JWT_SECRET=dGVzdFNlY3JldEtleUZvckpXVFRva2VuVmFsaWRhdGlvbjEyMzQ1Njc4OTA=
SPRING_DATA_MONGODB_URI=mongodb://root:password@mongodb:27017/letsplay?authSource=admin
SPRING_DATA_MONGODB_DATABASE=letsplay
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
SPRING_PROFILES_ACTIVE=dev
```

### Option 2: Environment Variables (Production)

```bash
export JWT_SECRET=$(openssl rand -base64 32)
export SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/letsplay
export SPRING_DATA_MONGODB_DATABASE=letsplay
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
export SPRING_PROFILES_ACTIVE=prod
```

### Option 3: Docker Compose (Recommended)

Variables defined in `.env` are automatically passed to containers via `docker-compose.yml`:

```bash
docker compose up -d
```

The compose file loads `.env` automatically.

### Option 4: Java Command Line

```bash
java -Dspring.datasource.url="mongodb://..." \
     -DJWT_SECRET="your-secret" \
     -jar api-0.0.1-SNAPSHOT.jar
```

---

## Application Profiles

Spring Boot supports multiple profiles for different environments.

### Available Profiles

| Profile | File | Environment | Use Case |
|---------|------|-------------|----------|
| **default** | `application.properties` | All | Base configuration |
| **dev** | `application-dev.properties` | Development | Local development |
| **test** | `application-test.properties` | Testing | Integration tests |
| **prod** | `application-prod.properties` | Production | Production deployment |

### Activate Profile

**Via environment variable:**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

**Via .env file:**
```bash
SPRING_PROFILES_ACTIVE=prod
```

**Via Docker Compose:**
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

**Via Java command:**
```bash
java -Dspring.profiles.active=prod -jar api-0.0.1-SNAPSHOT.jar
```

**Via Maven:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Configuration Files

### application.properties (Base Configuration)

Contains shared settings for all profiles:

```properties
# Server
server.port=8080

# Spring Boot
spring.application.name=api

# Logging
logging.level.root=INFO
logging.level.com.letsplay=DEBUG

# Reverse proxy headers
server.forward-headers-strategy=framework
server.tomcat.remoteip.remote-ip-header=X-Forwarded-For
server.tomcat.remoteip.protocol-header=X-Forwarded-Proto
```

### application-dev.properties (Development)

Development-specific settings:

```properties
# Logging
logging.level.root=DEBUG
logging.level.com.letsplay=DEBUG

# MongoDB (defaults to localhost)
# Override in .env if needed

# Spring Boot
spring.jpa.show-sql=false
```

### application-test.properties (Testing)

Test environment configuration:

```properties
# In-memory MongoDB for tests
# Configured via test dependencies

# Logging
logging.level.root=WARN
```

### application-prod.properties (Production)

Production-optimized settings:

```properties
# Logging (minimal)
logging.level.root=WARN
logging.level.com.letsplay=INFO

# Performance tuning
spring.jpa.show-sql=false
spring.h2.console.enabled=false
```

---

## MongoDB Connection Strings

### Local MongoDB

```
mongodb://root:password@localhost:27017/letsplay?authSource=admin
```

**Components:**
- `root` - Username
- `password` - Password
- `localhost:27017` - Host and port
- `letsplay` - Database name
- `authSource=admin` - Authentication database

### MongoDB Atlas (Cloud)

```
mongodb+srv://user:password@cluster-name.mongodb.net/letsplay?retryWrites=true&w=majority
```

**Setup:**
1. Create MongoDB Atlas account
2. Create cluster
3. Create database user
4. Get connection string from Atlas UI
5. Replace `user` and `password` with your credentials

### Docker MongoDB

When using docker-compose:

```
mongodb://root:password@mongodb:27017/letsplay?authSource=admin
```

**Note:** `mongodb` is the service name in docker-compose.yml

---

## CORS Configuration

### What is CORS?

Cross-Origin Resource Sharing (CORS) allows requests from approved domains.

### Allowed Origins

**Development:**
```
http://localhost:3000      # React dev server
http://localhost:4200      # Angular dev server
http://localhost:5173      # Vite dev server
http://localhost           # Local frontend
```

**Production:**
```
https://yourdomain.com     # Main domain
https://app.yourdomain.com # App subdomain
```

### Configure CORS Origins

Set in `.env`:

```bash
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

Multiple origins separated by commas (no spaces).

### Important Security Notes

- Never use `*` (wildcard) in production
- Always use HTTPS in production
- Whitelist only trusted domains
- Avoid localhost origins in production

---

## Rate Limiting Configuration

### What is Rate Limiting?

Prevents brute force attacks by limiting requests per time window.

### Default Configuration

- **Endpoint:** `/api/auth/login` and `/api/auth/register`
- **Limit:** 5 requests per minute per IP
- **Response:** HTTP 429 (Too Many Requests)

### Adjust Rate Limiting

Set in `.env`:

```bash
RATELIMIT_CAPACITY=10         # Max requests
RATELIMIT_REFILL_MINUTES=1    # Time window (1 minute)
```

**Examples:**

**Strict (Recommended for high-security):**
```bash
RATELIMIT_CAPACITY=3
RATELIMIT_REFILL_MINUTES=5
```

**Relaxed (For testing):**
```bash
RATELIMIT_CAPACITY=100
RATELIMIT_REFILL_MINUTES=1
```

### Response When Rate Limited

```json
{
  "status": 429,
  "message": "Too Many Requests - Rate limit exceeded",
  "timestamp": "2025-10-13T10:30:00Z"
}
```

---

## MongoDB with DataGrip

### Connection Details

- **Host:** `localhost`
- **Port:** `27017`
- **Database:** `letsplay`
- **Username:** `root`
- **Password:** `password`
- **Auth Database:** `admin`

### Setup Method 1: User & Password (Recommended)

1. **Create New Data Source** → MongoDB
2. **General Tab:**
   - Host: `localhost`
   - Port: `27017`
   - User: `root`
   - Password: `password`
   - Authentication database: `admin`
   - Database: `letsplay`
3. **Test Connection**
4. **Apply** and **OK**

### Setup Method 2: Connection URL

1. **Create New Data Source** → MongoDB
2. **General Tab:**
   - URL: `mongodb://root:password@localhost:27017/letsplay?authSource=admin`
   - Authentication: No auth (credentials in URL)
3. **Test Connection**
4. **Apply** and **OK**

### Viewing Data in DataGrip

1. **Expand** connection in Database Explorer
2. **Expand** `letsplay` database
3. **View collections:**
   - `users` - User accounts
   - `products` - Product listings
4. **Click** collection to view documents
5. **Right-click** → Query Console for MongoDB queries

### Example MongoDB Queries

```javascript
// Find all users
db.users.find()

// Find all products
db.products.find()

// Count documents
db.users.countDocuments()
db.products.countDocuments()

// Find by email
db.users.find({ email: "user@example.com" })

// Find products by user
db.products.find({ userId: ObjectId("507f1f77bcf86cd799439011") })

// Update user
db.users.updateOne(
  { _id: ObjectId("507f1f77bcf86cd799439011") },
  { $set: { name: "Updated Name" } }
)

// Delete product
db.products.deleteOne(
  { _id: ObjectId("507f1f77bcf86cd799439012") }
)
```

**MongoDB Note:** Uses **collections** (not tables) and **documents** (not rows).

---

## Java Heap Memory Configuration

### For Docker

Edit `docker-compose.yml`:

```yaml
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

**Options:**
- `-Xmx512m` - Maximum heap size (512 MB)
- `-Xms256m` - Initial heap size (256 MB)

### For JAR Execution

```bash
java -Xmx512m -Xms256m -jar api-0.0.1-SNAPSHOT.jar
```

### Recommended Heap Sizes

| Environment | Heap Size | Note |
|-------------|-----------|------|
| **Development** | 256M | Local testing |
| **Testing** | 512M | CI/CD pipelines |
| **Production** | 1G+ | Based on load |

---

## Generating JWT Secret

### Secure Secret Generation

```bash
# Generate 256-bit base64-encoded secret
openssl rand -base64 32
```

**Output example:**
```
dGVzdFNlY3JldEtleUZvckpXVFRva2VuVmFsaWRhdGlvbjEyMzQ1Njc4OTA=
```

### Add to .env

```bash
JWT_SECRET=dGVzdFNlY3JldEtleUZvckpXVFRva2VuVmFsaWRhdGlvbjEyMzQ1Njc4OTA=
```

### Security Recommendations

- Generate new secret for each environment (dev, test, prod)
- Never commit secrets to git
- Rotate secrets periodically (quarterly recommended)
- Store in secure vault (not plain text)
- Use strong secrets (256-bit minimum)

---

## Logging Configuration

### Default Logging Level

| Package | Level | Purpose |
|---------|-------|---------|
| `root` | INFO | System logs |
| `com.letsplay` | DEBUG | Application logs |
| `org.springframework` | INFO | Framework logs |
| `org.mongodb` | INFO | Database logs |

### Adjust Logging

In `.env` or `application.properties`:

```properties
logging.level.root=INFO
logging.level.com.letsplay=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Log Output

Logs print to console by default. Configure file output in `application.properties`:

```properties
logging.file.name=logs/app.log
logging.file.max-size=10MB
logging.file.max-history=10
```

### Sensitive Data in Logs

Passwords and tokens are automatically excluded from logs:
- Passwords never logged
- JWT tokens filtered
- Database credentials sanitized

---

## Configuration Checklist

### Development Setup

- [ ] Create `.env` file from `.env.example`
- [ ] Generate JWT secret: `openssl rand -base64 32`
- [ ] Set `SPRING_PROFILES_ACTIVE=dev`
- [ ] Verify MongoDB connection string
- [ ] Configure CORS for local development
- [ ] Run tests: `./mvnw test`

### Production Setup

- [ ] Generate secure JWT secret
- [ ] Use managed MongoDB (Atlas) or secure self-hosted
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure CORS with production domains
- [ ] Enable HTTPS/TLS
- [ ] Set appropriate rate limits
- [ ] Configure monitoring and logging
- [ ] Plan backup strategy
- [ ] Document all environment variables

