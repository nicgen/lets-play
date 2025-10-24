# Testing Guide

## Overview

The project includes **17 comprehensive test files** covering all layers with **80%+ code coverage**.

---

## Running Tests

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=AuthControllerTest
```

### Run Tests with Coverage Report

```bash
./mvnw clean test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

---

## Test Structure

### Unit Tests (Service Layer)

**Run service tests:**
```bash
./mvnw test -Dtest=AuthServiceTest
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=ProductServiceTest
```

**Coverage:**
- Authentication logic
- Authorization checks
- CRUD operations
- Business rule enforcement

### Integration Tests (Controller Layer)

**Run controller tests:**
```bash
./mvnw test -Dtest=AuthControllerTest
./mvnw test -Dtest=UserControllerTest
./mvnw test -Dtest=ProductControllerTest
```

**Coverage:**
- HTTP request/response handling
- Authentication flow
- Authorization enforcement
- Input validation
- Error handling

### Additional Tests

```bash
# Repository tests
./mvnw test -Dtest=UserRepositoryTest
./mvnw test -Dtest=ProductRepositoryTest

# Security tests
./mvnw test -Dtest=JwtUtilTest

# Configuration tests
./mvnw test -Dtest=CorsConfigTest

# Exception handling tests
./mvnw test -Dtest=GlobalExceptionHandlerTest

# Rate limiting tests
./mvnw test -Dtest=RateLimitServiceTest
```

### Test Results Summary

| Category | Files | Tests | Status |
|----------|-------|-------|--------|
| Controller Tests | 3 | 44 | Pass |
| Service Tests | 3 | ~30 | Pass |
| Repository Tests | 2 | ~15 | Pass |
| Security Tests | 1 | ~10 | Pass |
| Configuration Tests | 2 | ~8 | Pass |
| Other Tests | 6 | ~20 | Pass |
| **TOTAL** | **17** | **~127** | **Pass** |

---

## Insomnia REST Client

**Recommended!** The project includes a comprehensive Insomnia collection with 31 pre-configured requests.

### Import the Collection

1. **Download Insomnia** from https://insomnia.rest/download
2. **Import Collection**: Open Insomnia → Create → Import From File
3. **Select File**: `insomnia_collection.json` in project root
4. **Done!** All 31 requests are ready to use

### Automatic Token Management

The collection features **automatic JWT token extraction** via after-response scripts:

- **Login User** request automatically saves token to `auth_token` environment variable
- **Login Admin** request automatically saves token to `admin_token` environment variable
- No manual copying needed - just login and start testing!

**How it works:**
```javascript
// After-response script (included in collection)
if (insomnia.response.code === 200) {
  const jsonBody = insomnia.response.json();
  if (jsonBody.token) {
    insomnia.environment.set("auth_token", jsonBody.token);
  }
}
```

### Quick Start Workflow

1. **Start the application** (see README for setup)
2. **Run `01 - Authentication → Register User`**
3. **Run `01 - Authentication → Login User`**
   - Token automatically saved to `auth_token`
4. **Test Endpoints** - All authenticated requests now work automatically
5. **Token Expired?** Just run Login again - token auto-updates!

### Collection Structure

- **01 - Authentication** (4 requests) - Register, Login for User & Admin
- **02 - Users** (7 requests) - Complete user management CRUD
- **03 - Products** (11 requests) - Product CRUD with public endpoints
- **04 - Test Scenarios** (8 requests) - Validation and error handling tests

---

## Manual API Testing with cURL

### 1. Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 2. Login and get token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 3. Create a product (use token from step 2)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token-here>" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99
  }'
```

### 4. Get all products (no auth needed)

```bash
curl http://localhost:8080/api/products
```

---

## Using Postman

The Insomnia collection can be converted to Postman format:

1. Export from Insomnia as Postman format
2. Import to Postman
3. Set environment variable `baseUrl` = `http://localhost:8080`
4. Manually copy tokens (Postman doesn't support Insomnia scripts)

**Note:** For best experience, use Insomnia with the included collection for automatic token management!

---

## API Documentation

For detailed API endpoint documentation, see [API.md](API.md)
