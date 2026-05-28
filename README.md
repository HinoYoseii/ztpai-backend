# Projekt — Spring Boot REST API

A Spring Boot REST API with JWT-based authentication, built on Java 21 and backed by PostgreSQL.

---

## Prerequisites

Make sure the following are installed before running the project:

- [Java 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/download.cgi) *(or use the included Maven wrapper)*
- [PostgreSQL 14+](https://www.postgresql.org/download/)
- [Git](https://git-scm.com/)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/HinoYoseii/ztpai-backend.git
cd ztpai-backend
```

### 2. Set up the database

Open your PostgreSQL client (e.g. `pgadmin`) and create the database:

```sql
CREATE DATABASE ztpai;
```

Make sure a user `postgres` exists with the password `haslo`, or update the credentials in the next step.

### 3. Configure the application

Open `src/main/resources/application.properties` and adjust the following values to match your local environment:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ztpai
spring.datasource.username=postgres
spring.datasource.password=haslo

jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

---

## Running the Application

### Using IntelliJ IDEA
Open cloned directory as project in IntelliJ IDEA. Reload All Maven Projects if necessary. Run using ```ProjektApplication.java``` file.

### Using the Maven wrapper

```bash
./mvnw spring-boot:run
```

### Using system Maven

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080** by default.

> You can test the endpoints directly or run the dedicated [frontend](https://github.com/HinoYoseii/ztpai-frontend).


## Running Tests

### Using IntelliJ IDEA
1. Open cloned directory in IDE. 
2. Reload All Maven Projects if necessary. 
3. Run the application. 
4. Run tests using ```UserServiceTests.java``` file.

### Using the Maven wrapper
```bash
./mvnw test
```