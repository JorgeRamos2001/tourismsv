# TourismSV — Guía para el agente

Proyecto backend Spring Boot 4.0.7 / Java 21 para sistema turístico de El Salvador.

## Comandos esenciales

- `./mvnw compile` — compila
- `./mvnw test` — ejecuta tests
- `./mvnw test -Dtest=ClaseTest` — test individual
- `./mvnw spring-boot:run` — levanta servidor (requiere PostgreSQL y `.env`)
- `./mvnw verify` — compila + test + empaqueta

## Stack y dependencias clave

- Spring Boot 4.0.7, Java 21, PostgreSQL
- Spring Security + JWT (jjwt 0.13.0)
- Spring Data JPA (Hibernate), Flyway, Jakarta Validation
- SpringDoc OpenAPI 3.0.2
- Lombok 1.18.46 (annotation processor en pom.xml e IntelliJ)
- MapStruct **mencionado en PRD pero NO en pom.xml** — no agregar sin instrucción explícita
- Maven Wrapper 3.3.4 (mvnw), Maven 3.9.16 de distribución

## Arquitectura

- **Paquetes por feature** (`config/`, `controller/`, `service/`, `repository/`, `entity/`, `dto/request/`, `dto/response/`, `mapper/`, `exception/`, `security/`) bajo `com.tourismsv`
- **Solo existe** `TourismSvApplication.java` — el resto está por crear
- Base path: `/api/v1` (no configurado aún; definir con `server.servlet.context-path`)
- UUID como PK en todas las entidades
- Respuesta de error estándar: `{ timestamp, status, error, message, path }`
- Respuesta paginada: `{ content, page, size, totalElements, totalPages, last }`

## Base de datos

- Flyway gestiona el schema; JPA usa `ddl-auto: validate` (nunca crear tablas desde JPA)
- Migración actual: `V1__init_schema.sql` (7 tablas + refresh_tokens)
- Perfiles: `dev` (default), `test`, `prod`
- En test: `flyway.clean-disabled: false` (permite limpiar DB entre tests)
- En prod: `flyway.clean-disabled: true`

## Configuración

- `.env` (gitignored) con variables de entorno para desarrollo local
- `application.yaml` tiene `jobsportal` como DB name por defecto (copia de otro proyecto) — el `.env` lo sobreescribe a `tourismsv`
- CORS permite `http://localhost:5173` (Vite) y `http://localhost:3000` (React)
- JWT: access token 15 min, refresh token 7 días
- Mail SMTP configurado con Gmail (app password)

## CI/CD

- `.github/workflows/ci.yml`: build + test con PostgreSQL 16 (servicio container)
- Corre en PRs hacia `main` y pushes a `main`
- Valida commits con Conventional Commits (`commitlint.config.cjs`)
- Usa perfil `test` con variables de entorno específicas

## Convenciones de código

- **Commits:** Conventional Commits con tipos `feat|fix|docs|style|refactor|test|chore|perf|ci|build`, max header 100 chars, subject-case desactivado (permite español)
- **Lombok:** usar `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` en entidades y DTOs
- **Validación:** Jakarta Bean Validation en DTOs de request
- **Excepciones:** `@ControllerAdvice` global con formato de error estándar
- **PRD como fuente de verdad para naming** de columnas y entidades

## Roles del sistema

- `ADMIN` — acceso completo
- `TOURIST` — usuario registrado (reviews, likes, saves)
- Público (no autenticado) — solo lectura

## Endpoints planeados (PRD)

- `/api/v1/auth` — register, login, refresh, logout
- `/api/v1/users` — CRUD (ADMIN)
- `/api/v1/destination-types` — CRUD (ADMIN write, público read)
- `/api/v1/destinations` — CRUD + filtros + paginación
- `/api/v1/destinations/{id}/images` — galería
- `/api/v1/destinations/{id}/reviews` — reseñas
- `/api/v1/destinations/{id}/likes` — likes
- `/api/v1/destinations/{id}/saves` — guardados

## Notas importantes

- El proyecto está **recién inicializado** — no hay controllers, services, repositories, entities, DTOs, mappers, security config ni exception handlers
- No crear `README.md` ni archivos de documentación a menos que se solicite explícitamente
- No agregar MapStruct a menos que se pida
