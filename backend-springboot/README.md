# CityVibe — Backend (Spring Boot + MySQL)

REST API built with **Spring Boot 3.2 (Java 17)** and **Spring Data JPA / Hibernate**
over **MySQL**.

## Entity: `Event`
`id, title, category, description, imageUrl, dateTime, duration, venueName,
venueAddress, organizer, price`

## Endpoints
| Method | Path                    | Description                              |
|--------|-------------------------|------------------------------------------|
| GET    | `/api/events`           | List events. Optional `?category=` filter |
| GET    | `/api/events/{id}`      | Single event (404 if missing)            |

Categories seeded: `Music`, `Comedy`, `Meetup`, `Gathering` (8 sample events).
On first startup `DataSeeder` populates the DB if it is empty.

## Configure (env vars, with sensible defaults)
- `DB_URL`      (default `jdbc:mysql://localhost:3306/cityvibe?...`)
- `DB_USERNAME` (default `cityvibe`)
- `DB_PASSWORD` (default `cityvibe123`)
- `SERVER_PORT` / `--server.port` (default `8080`; this environment runs on `8090`)

## Run locally
```bash
# 1. Create the database
mysql -u root -e "CREATE DATABASE cityvibe; \
  CREATE USER 'cityvibe'@'localhost' IDENTIFIED BY 'cityvibe123'; \
  GRANT ALL ON cityvibe.* TO 'cityvibe'@'localhost';"

# 2. Run
./mvnw spring-boot:run        # or: mvn spring-boot:run
# or build a jar:
mvn -DskipTests clean package && java -jar target/cityvibe-backend-1.0.0.jar
```

## In this preview environment
- MySQL data is stored persistently at `/app/data/mysql`.
- `scripts/start-mysql.sh` and `scripts/start-springboot.sh` are supervisor-managed
  and self-install the runtimes if the container is recycled.
