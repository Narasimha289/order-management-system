# E-commerce Order Management System

A Java-based backend application for managing users and products, built with Spring Boot and PostgreSQL.

## Project Status

**In Progress** — developed as a Java Developer portfolio project.

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Data Access:** Spring Data JPA
- **Database Migrations:** Flyway
- **Build Tool:** Maven
- **Security:** Spring Security, JWT, BCrypt

## Implemented Features

- User registration and login API components
- Product management API components
- Service and repository layers
- DTOs for request and response handling
- JWT-based authentication configuration
- Centralized exception handling
- Versioned database schema migrations using Flyway

## Prerequisites

- Java 21
- PostgreSQL
- Git

## Local Setup

1. Clone this repository:

   ```bash
   git clone https://github.com/Narasimha289/order-management-system.git
   cd order-management-system
   ```

2. Create a PostgreSQL database named `order_management_system`.

3. Configure the following environment variables in your run environment:

   - `DB_URL` — JDBC URL for your PostgreSQL database
   - `DB_USERNAME` — PostgreSQL username
   - `DB_PASSWORD` — PostgreSQL password
   - `JWT_SECRET` — secret used by the application's JWT implementation

4. Run the application using the Maven Wrapper:

   **Windows PowerShell:**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux/macOS:**

   ```bash
   ./mvnw spring-boot:run
   ```

## Database Migrations

Flyway manages the database schema migrations located in `src/main/resources/db/migration`.

## Future Improvements

- Expand automated tests
- Add further order-management functionality
- Improve API documentation

## Author

Narasimha Reddy Kodekond

GitHub: [Narasimha289](https://github.com/Narasimha289)
