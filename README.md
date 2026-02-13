# Fcmb Security

A production-ready authentication and authorization system built with Spring Boot 3.5+ and Java 17.

## 📋 Project Structure

```
fcmb-security/
├── core-security-starter/          # Reusable Spring Boot Starter
│   ├── src/main/java/com/security/core/
│   │   ├── config/                 # Auto-configuration
│   │   ├── filter/                 # JWT Authentication Filter
│   │   ├── security/               # Security handlers
│   │   ├── util/                   # JWT utilities
│   │   ├── dto/                    # Common DTOs
│   │   └── exception/              # Global exception handler
│   └── src/main/resources/
│       └── META-INF/spring.factories
└── sample-application/             # Sample consuming application
    └── src/main/java/com/security/sample/
        ├── controller/             # REST Controllers
        ├── service/                # Business logic
        ├── entity/                 # JPA Entities
        ├── repository/             # Data repositories
        └── dto/                    # Data transfer objects
```

## ✨ Features

### Core Security Starter

1. **Auto-Configuration**
   - Automatic Spring Security setup
   - JWT utility bean registration
   - Security filter chain configuration
   - Exception handlers

2. **Authentication**
   - Username/password authentication
   - BCrypt password hashing
   - JWT token generation with claims (userId, username, roles, expiry)
   - Signed JWT tokens using HMAC-SHA

3. **Authorization**
   - Role-based access control
   - Method-level security (@PreAuthorize)
   - URL-level security

4. **Cross-Cutting Concerns**
   - JWT authentication filter
   - Token validation and parsing
   - 401 Unauthorized handler
   - 403 Forbidden handler
   - Standardized error response format
   - Request logging with user context
   - Configurable properties

### Sample Application

Demonstrates three endpoint types:
- `/api/public/health` - Public endpoint (no authentication)
- `/api/user/me` - Requires authentication
- `/api/admin/users` - Requires ROLE_ADMIN

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build the Project

```bash
# Navigate to project root
cd fcmb-security

# Build all modules
mvn clean install
```

### Run the Sample Application

```bash
cd sample-application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📝 API Documentation

### Test Users

The application initializes with two test users:

| Username | Password   | Roles              |
|----------|------------|--------------------|
| user     | password   | ROLE_USER          |
| admin    | admin123   | ROLE_USER, ROLE_ADMIN |

### Endpoints

#### 1. Public Health Check
```bash
GET /api/public/health

# Response
{
  "status": "UP",
  "timestamp": "2024-02-13T10:30:00",
  "message": "Application is running"
}
```

#### 2. Login (Obtain JWT Token)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 2,
  "username": "admin",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

#### 3. Get Current User (Requires Authentication)
```bash
GET /api/user/me
Authorization: Bearer <your-jwt-token>

# Response
{
  "id": 2,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "enabled": true,
  "createdAt": "2024-02-13T10:00:00"
}
```

#### 4. Get All Users (Requires ROLE_ADMIN)
```bash
GET /api/admin/users
Authorization: Bearer <your-jwt-token>

# Response
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

## 🧪 Testing with cURL

### 1. Public Endpoint
```bash
curl -X GET http://localhost:8080/api/public/health
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 3. Access Protected Endpoint
```bash
# Replace <TOKEN> with actual token from login response
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Access Admin Endpoint
```bash
# Replace <TOKEN> with admin user token
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <TOKEN>"
```

## ⚙️ Configuration

### JWT Configuration Properties

Configure in `application.yml`:

```yaml
security:
  jwt:
    secret-key: "YourSecretKeyHere"
    expiration-ms: 86400000        # 24 hours in milliseconds
    issuer: "your-app-name"
    header-name: "Authorization"
    token-prefix: "Bearer "
    enable-logging: true
```

### Property Descriptions

| Property | Description | Default |
|----------|-------------|---------|
| `secret-key` | Secret key for JWT signing | (must be set) |
| `expiration-ms` | Token expiration time in milliseconds | 86400000 (24h) |
| `issuer` | JWT issuer claim | spring-security-starter |
| `header-name` | HTTP header for token | Authorization |
| `token-prefix` | Token prefix | Bearer  |
| `enable-logging` | Enable authentication logging | true |

## 🔒 Security Features

### Password Security
- BCrypt hashing with default strength of 10
- Automatic password encoding on user creation

### JWT Token Security
- HMAC-SHA256 signing algorithm
- Configurable expiration time
- Claims: userId, username, roles, issuer, issued at, expiry
- Token validation on every request

### Error Handling
- 401 Unauthorized: Invalid credentials or missing token
- 403 Forbidden: Insufficient permissions
- Standardized error response format

### Request Logging
When enabled, logs include:
- Username
- User ID
- HTTP method
- Request URI

## 🏗️ Architecture Highlights

### Separation of Concerns
- **Core Starter**: Contains all cross-cutting security concerns
- **Sample App**: Contains only business logic, no security implementation

### Auto-Configuration
- Uses Spring Boot's auto-configuration mechanism
- Registers beans conditionally using `@ConditionalOnMissingBean`
- Discovered via `META-INF/spring.factories`

### Reusability
- Core starter can be included in any Spring Boot 3.x application
- Zero security code in consuming applications
- Consistent security behavior across microservices

## 📦 Using Core Security Starter in Your Project

### Add Dependency

```xml
<dependency>
    <groupId>com.security</groupId>
    <artifactId>core-security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configure Properties

```yaml
security:
  jwt:
    secret-key: "your-production-secret-key"
    expiration-ms: 3600000  # 1 hour
```

### Implement UserDetailsService

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // Load user from your database
        // Return UserDetails with authorities
    }
}
```

### Use JWT Utility

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final JwtUtil jwtUtil;
    
    public String authenticate(String username, List<GrantedAuthority> authorities) {
        return jwtUtil.generateToken(userId, username, authorities);
    }
}
```

## 🔧 Customization

### Override Security Configuration

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) {
        // Your custom security configuration
        return http.build();
    }
}
```

### Custom Exception Handling

The global exception handler can be extended:

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        // Custom handling
    }
}
```

## 🎯 Best Practices Implemented

1. **Stateless Authentication**: JWT-based, no server-side session
2. **Password Security**: BCrypt with salt
3. **Role-Based Authorization**: Flexible permission system
4. **Separation of Concerns**: Security logic isolated in starter
5. **Configuration over Code**: Externalized configuration
6. **Comprehensive Logging**: Audit trail of authentication events
7. **Standardized Errors**: Consistent API error responses
8. **Method Security**: Fine-grained access control

## 📄 License

This project is for educational and demonstration purposes.

## 👥 Support

For issues or questions, please refer to the documentation or create an issue in the repository.
