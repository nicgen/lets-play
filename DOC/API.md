# API Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

All endpoints except `GET /api/products` and `GET /api/products/{id}` require authentication via JWT token.

**Header Format:**
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Register New User

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123!",
  "role": "ROLE_USER"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "Password123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
}
```

**Rate Limiting:** Limited to 5 requests per minute per IP. Returns `429 Too Many Requests` when exceeded.

---

## User Endpoints

### Get All Users (Admin Only)

```http
GET /api/users
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
]
```

### Get Current User

```http
GET /api/users/me
Authorization: Bearer <token>
```

### Get User by ID

```http
GET /api/users/{id}
Authorization: Bearer <token>
```

### Update User

```http
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Updated",
  "email": "john.updated@example.com"
}
```

**Authorization:**
- User can update their own profile
- Admin can update any user

### Delete User

```http
DELETE /api/users/{id}
Authorization: Bearer <token>
```

**Response:** 204 No Content

**Authorization:**
- User can delete their own account
- Admin can delete any user

---

## Product Endpoints

### Get All Products (Public)

```http
GET /api/products
```

**No authentication required!**

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439012",
    "name": "Product Name",
    "description": "Product description",
    "price": 99.99,
    "userId": "507f1f77bcf86cd799439011"
  }
]
```

### Get Product by ID (Public)

```http
GET /api/products/{id}
```

**No authentication required!**

### Get Products by User ID

```http
GET /api/products/user/{userId}
Authorization: Bearer <token>
```

### Create Product

```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Product",
  "description": "Product description",
  "price": 49.99
}
```

**Response (201 Created):**
```json
{
  "id": "507f1f77bcf86cd799439013",
  "name": "New Product",
  "description": "Product description",
  "price": 49.99,
  "userId": "507f1f77bcf86cd799439011"
}
```

**Note:** `userId` is automatically set to the authenticated user.

### Update Product

```http
PUT /api/products/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Product",
  "description": "Updated description",
  "price": 149.99
}
```

**Authorization:**
- Product owner can update
- Admin can update any product

### Delete Product

```http
DELETE /api/products/{id}
Authorization: Bearer <token>
```

**Response:** 204 No Content

**Authorization:**
- Product owner can delete
- Admin can delete any product

---

## Error Responses

All errors return a consistent JSON format:

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2025-10-13T10:30:00Z"
}
```

**HTTP Status Codes:**
- `200` - OK (successful GET, PUT, DELETE)
- `201` - Created (successful POST)
- `204` - No Content (successful DELETE)
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `429` - Too Many Requests (rate limit exceeded)

**Note:** No 5XX errors - all exceptions handled gracefully!

---

## Testing with Insomnia

The project includes a comprehensive Insomnia collection (`insomnia_collection.json`) with 31 pre-configured requests covering all endpoints.

See [TESTING.md](TESTING.md) for detailed setup instructions.
