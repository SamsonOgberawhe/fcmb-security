# API Testing Examples

## Using cURL

### 1. Check Application Health (Public)

```bash
curl -X GET http://localhost:8080/api/public/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-02-13T10:30:00",
  "message": "Application is running"
}
```

---

### 2. Login as Regular User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwidXNlcklkIjoxLCJ1c2VybmFtZSI6InVzZXIiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaXNzIjoic2FtcGxlLWFwcGxpY2F0aW9uIiwiaWF0IjoxNzA3ODI4MDAwLCJleHAiOjE3MDc5MTQ0MDB9.xxx",
  "type": "Bearer",
  "userId": 1,
  "username": "user",
  "roles": ["ROLE_USER"]
}
```

---

### 3. Login as Admin User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfVVNFUiIsIlJPTEVfQURNSU4iXSwiaXNzIjoic2FtcGxlLWFwcGxpY2F0aW9uIiwiaWF0IjoxNzA3ODI4MDAwLCJleHAiOjE3MDc5MTQ0MDB9.xxx",
  "type": "Bearer",
  "userId": 2,
  "username": "admin",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

---

### 4. Get Current User Info (Requires Authentication)

**Save token from login response:**
```bash
TOKEN="<paste-your-token-here>"
```

**Make request:**
```bash
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "user",
  "email": "user@example.com",
  "roles": ["ROLE_USER"],
  "enabled": true,
  "createdAt": "2024-02-13T10:00:00"
}
```

---

### 5. Get All Users (Requires ROLE_ADMIN)

**With Admin Token:**
```bash
ADMIN_TOKEN="<paste-admin-token-here>"

curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "username": "user",
    "email": "user@example.com",
    "roles": ["ROLE_USER"],
    "enabled": true,
    "createdAt": "2024-02-13T10:00:00"
  },
  {
    "id": 2,
    "username": "admin",
    "email": "admin@example.com",
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "enabled": true,
    "createdAt": "2024-02-13T10:00:00"
  }
]
```

---

## Error Scenarios

### 1. Invalid Credentials (401 Unauthorized)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "wrongpassword"
  }'
```

**Expected Response:**
```json
{
  "timestamp": "2024-02-13T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials or authentication token",
  "path": "/api/auth/login"
}
```

---

### 2. Missing Token (401 Unauthorized)

```bash
curl -X GET http://localhost:8080/api/user/me
```

**Expected Response:**
```json
{
  "timestamp": "2024-02-13T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required to access this resource",
  "path": "/api/user/me"
}
```

---

### 3. Insufficient Permissions (403 Forbidden)

**Regular user trying to access admin endpoint:**
```bash
USER_TOKEN="<regular-user-token>"

curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected Response:**
```json
{
  "timestamp": "2024-02-13T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have sufficient permissions to access this resource",
  "path": "/api/admin/users"
}
```

---

## Using Postman

### Setup Environment Variables

Create a Postman environment with:
- `base_url`: `http://localhost:8080`
- `token`: (will be set automatically after login)

### Collection Structure

1. **Public**
   - GET Health Check: `{{base_url}}/api/public/health`

2. **Authentication**
   - POST Login User: `{{base_url}}/api/auth/login`
     - Body: `{"username": "user", "password": "password"}`
     - Test script: `pm.environment.set("token", pm.response.json().token);`
   
   - POST Login Admin: `{{base_url}}/api/auth/login`
     - Body: `{"username": "admin", "password": "admin123"}`
     - Test script: `pm.environment.set("token", pm.response.json().token);`

3. **User Endpoints**
   - GET Current User: `{{base_url}}/api/user/me`
     - Headers: `Authorization: Bearer {{token}}`

4. **Admin Endpoints**
   - GET All Users: `{{base_url}}/api/admin/users`
     - Headers: `Authorization: Bearer {{token}}`

---

## Complete Testing Script

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== Testing Spring Security System ==="
echo

# 1. Public endpoint
echo "1. Testing public endpoint..."
curl -X GET $BASE_URL/api/public/health
echo -e "\n"

# 2. Login as user
echo "2. Login as regular user..."
USER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}')
USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "User Token: ${USER_TOKEN:0:50}..."
echo

# 3. Login as admin
echo "3. Login as admin..."
ADMIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Admin Token: ${ADMIN_TOKEN:0:50}..."
echo

# 4. Get current user
echo "4. Get current user info..."
curl -X GET $BASE_URL/api/user/me \
  -H "Authorization: Bearer $USER_TOKEN"
echo -e "\n"

# 5. User tries to access admin endpoint (should fail)
echo "5. User trying to access admin endpoint (should fail with 403)..."
curl -X GET $BASE_URL/api/admin/users \
  -H "Authorization: Bearer $USER_TOKEN"
echo -e "\n"

# 6. Admin accessing admin endpoint
echo "6. Admin accessing admin endpoint..."
curl -X GET $BASE_URL/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo -e "\n"

echo "=== Testing Complete ==="
```

Save this as `test-api.sh`, make it executable with `chmod +x test-api.sh`, and run it with `./test-api.sh`.
