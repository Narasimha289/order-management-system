# Order Management System

A backend REST API application built with Java and Spring Boot to manage products, orders, and users. The application uses PostgreSQL for data persistence and JWT-based authentication to protect API endpoints.

## Live Demo

- **Application:** https://order-management-system-f9wt.onrender.com
- **Swagger API Documentation:** https://order-management-system-f9wt.onrender.com/swagger-ui/index.html

## Features

- User registration and login
- JWT-based authentication and authorization
- Product management through REST APIs
- Order management through REST APIs
- PostgreSQL database integration
- Database schema versioning and migrations using Flyway
- Request validation
- API documentation using Swagger UI
- Password hashing with BCrypt
- Centralized application configuration through environment variables

## Technologies Used

- **Language:** Java 21
- **Framework:** Spring Boot 3.5
- **Database:** PostgreSQL
- **Persistence:** Spring Data JPA, Hibernate
- **Security:** Spring Security, JWT, BCrypt
- **Database Migrations:** Flyway
- **Validation:** Jakarta Bean Validation
- **Build Tool:** Maven
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Deployment:** Render
- **Database Hosting:** Neon PostgreSQL

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate a user |
| GET | `/api/products` | Retrieve all products |
| GET | `/api/products/{id}` | Retrieve a product by ID |
| POST | `/api/products` | Create a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

Product endpoints require authentication with a valid JWT token.

Explore the API and its request schemas through the live Swagger documentation linked above.

## Run Locally

### Prerequisites

- Java 21 or later
- PostgreSQL
- Git

### 1. Clone the repository

```bash
git clone https://github.com/Narasimha289/order-management-system.git
cd order-management-system
```

### 2. Configure environment variables

Set the following environment variables before starting the application:

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Secret key used to sign JWTs |

Example JDBC URL:

```text
jdbc:postgresql://localhost:5432/order_management_system
```

Configure the database credentials and create the database before running the application. Use a sufficiently long, secure JWT secret and never commit real credentials to GitHub.

### 3. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The application uses port `8080` by default unless a different port is supplied through the `PORT` environment variable.

### 4. Open Swagger UI

Once the application is running, visit:

```text
http://localhost:8080/swagger-ui/index.html
```

Use the authentication endpoints to register and log in. Supply the returned JWT when calling protected endpoints.

## Configuration and Security

- Database credentials and JWT signing secrets are supplied through environment variables.
- Passwords are hashed using BCrypt.
- Spring Security protects endpoints that require authentication.
- Flyway manages database migrations.
- The application is configured to validate the database schema against the entity mappings.

## Author

**Narasimha**

- GitHub: https://github.com/Narasimha289

