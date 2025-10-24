# Security Guide

## Security Features Implemented

### Authentication & Authorization

- **JWT Token-Based Authentication** - Stateless, secure token validation
- **Role-Based Access Control** - ADMIN and USER roles with granular permissions
- **Token Expiration** - 15-minute token validity period
- **Secure Password Hashing** - BCrypt with strength 12 (1 in 2^12 = 4096 iterations)

### Input Validation

- **Bean Validation** on all DTOs
- **MongoDB Injection Prevention** - Input sanitization and parameterized queries
- **Strong Password Policy** - Enforced on registration and updates
- **Email Format Validation** - RFC 5322 compliant

### API Security

- **Global Exception Handler** - No 5XX errors leak sensitive information
- **Rate Limiting** - 5 requests/minute on authentication endpoints
- **CORS with Whitelisted Origins** - Prevents unauthorized cross-origin access
- **Proper HTTP Status Codes** - Correct semantics for all responses

### Data Protection

- **Passwords Never Returned** - DTOs exclude password field
- **DTOs for API Responses** - Only safe data exposed
- **Environment-Based Secret Management** - No hardcoded credentials
- **No Sensitive Data in Logs** - Passwords and tokens filtered

---

## Password Requirements

Passwords must meet the following criteria:

- **Minimum 8 characters**
- **At least one uppercase letter** (A-Z)
- **At least one lowercase letter** (a-z)
- **At least one digit** (0-9)
- **At least one special character** (@$!%*?&)

### Valid Examples

```
Password123!
SecureP@ss1
MyP@ssw0rd
Test#1234
```

### Invalid Examples

```
password          # No uppercase, digit, or special char
Pass1!            # Too short (6 chars)
Password123       # No special character
test@123          # No uppercase
```

---

## Security Best Practices

### For Development

- Use `.env` file for local secrets (add to `.gitignore`)
- Never commit JWT secrets to version control
- Use strong, randomly generated secrets:
  ```bash
  openssl rand -base64 32
  ```
- Test authentication flows thoroughly
- Use HTTPs even in development when possible

### For Production

**REQUIRED:**
- [ ] Set `JWT_SECRET` environment variable (never hardcoded)
- [ ] Enable HTTPS/TLS (use reverse proxy like Traefik)
- [ ] Configure MongoDB authentication with strong passwords
- [ ] Set proper CORS origins (whitelist only trusted domains)
- [ ] Use MongoDB managed service (Atlas) or self-managed with backups

**Recommended:**
- [ ] Implement secret rotation schedule
- [ ] Monitor rate limiting logs for attacks
- [ ] Configure proper logging (exclude sensitive data)
- [ ] Set up monitoring and alerting
- [ ] Use Web Application Firewall (WAF)
- [ ] Implement DDoS protection
- [ ] Regular security audits
- [ ] Keep dependencies updated
- [ ] Use environment-specific profiles (dev, test, prod)
- [ ] Enable request logging and audit trails

---

## JWT Security

### Token Structure

JWT tokens consist of three parts separated by dots:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U
│ Header                               │ Payload                             │ Signature
```

### Configuration

**Token Details:**
- Algorithm: HS256 (HMAC with SHA-256)
- Expiration: 15 minutes
- Signing: Uses `JWT_SECRET` environment variable
- Validation: On every protected endpoint

### Best Practices

- Use strong, randomly generated secrets (256-bit)
- Rotate secrets periodically
- Never log or expose tokens
- Validate token expiration on every request
- Store tokens securely on client (HttpOnly cookies recommended)
- Use HTTPS always (prevents token interception)

---

## MongoDB Security

### Connection Security

**Local Development:**
```
mongodb://root:password@mongodb:27017/letsplay?authSource=admin
```

**Production (Recommended):**
```
mongodb+srv://user:password@cluster.mongodb.net/database
```

Use MongoDB Atlas with:
- IP Whitelist enabled
- Network access restrictions
- Database user with least privilege
- Connection encryption (TLS)

### Input Sanitization

All inputs are validated to prevent MongoDB injection:

```java
// Example: Email validation prevents injection
@Email
private String email;

// All fields validated before database operations
@NotBlank
@Size(min = 8)
private String password;
```

### Common Injection Attempts Prevented

```javascript
// Attempt: {"email": {"$ne": null}}
// Prevented: Validated as invalid email format

// Attempt: {"password": {"$gt": ""}}
// Prevented: Validated as invalid password format

// Attempt: db.users.drop(); //
// Prevented: String fields not executed as code
```

---

## Authentication Flow

### Registration

1. User submits name, email, password, optional role
2. Validation:
   - Email format check
   - Password strength check
   - Email uniqueness check
3. Password hashed with BCrypt (strength 12)
4. User stored in database
5. JWT token generated and returned

### Login

1. User submits email and password
2. User lookup by email
3. BCrypt password comparison
4. If match: JWT token generated with user ID and role
5. Token returned with 15-minute expiration
6. Rate limited to 5 requests per minute

### Token Validation

1. Token extracted from `Authorization: Bearer <token>` header
2. Signature verified using `JWT_SECRET`
3. Token expiration checked
4. User ID extracted and user loaded
5. Request processed with authenticated user context

---

## Role-Based Authorization

### Roles

**ROLE_ADMIN** - Full system access:
- View all users
- Manage all users
- Manage all products
- Bypass resource ownership checks

**ROLE_USER** - Limited access:
- View own profile
- Update own profile
- Delete own account
- Create products
- Update own products
- Delete own products
- View all public products

### Authorization Checks

```java
// User can only access their own data
GET /api/users/me            // Always allowed
GET /api/users/{otherId}     // Forbidden if not admin

// Product ownership enforced
PUT /api/products/{id}       // Owner or admin only
DELETE /api/products/{id}    // Owner or admin only
```

---

## Rate Limiting

### Configuration

- **Endpoint:** `/api/auth/login` and `/api/auth/register`
- **Limit:** 5 requests per minute per IP address
- **Response:** HTTP 429 (Too Many Requests)

### Usage

Rate limiting is automatic - exceeding the limit returns:

```json
{
  "status": 429,
  "message": "Too Many Requests - Rate limit exceeded",
  "timestamp": "2025-10-13T10:30:00Z"
}
```

### Configuration

Adjust in `.env`:
```
RATELIMIT_CAPACITY=5              # Max requests
RATELIMIT_REFILL_MINUTES=1        # Time window
```

---

## CORS Configuration

### Default Origins

Development:
```
http://localhost:3000
http://localhost:4200
http://localhost:5173
```

### Configure CORS Origins

Set in `.env`:
```
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
```

### Security Note

- Always use HTTPS in production
- Whitelist only trusted domains
- Never use `*` (wildcard) in production
- Include credentials header when needed

---

## Security Checklist

### Before Production

- [ ] Generate secure JWT secret (`openssl rand -base64 32`)
- [ ] Set all required environment variables
- [ ] Enable HTTPS/TLS certificates
- [ ] Configure MongoDB authentication
- [ ] Update CORS allowed origins
- [ ] Review rate limiting settings
- [ ] Test authentication flows
- [ ] Verify no sensitive data in logs
- [ ] Check password hashing strength
- [ ] Review exception handling (no stack traces exposed)
- [ ] Enable monitoring and alerting
- [ ] Plan secret rotation strategy
- [ ] Document security procedures
- [ ] Perform security audit
- [ ] Set up regular backup procedures

---

## Reporting Security Issues

If you discover a security vulnerability, please email security@letsplay.com with:

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if available)

Please do not publicly disclose security issues before they are fixed.

---

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [MongoDB Security](https://www.mongodb.com/docs/manual/security/)
