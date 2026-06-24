# PRD — Sistema Turístico El Salvador

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Estado:** Borrador  
**Autor:** (Tu nombre)

---

## 1. Visión General del Proyecto

### 1.1 Descripción

El Sistema Turístico El Salvador es una API REST Backend que permite a los usuarios descubrir, explorar y guardar destinos turísticos dentro del país. El sistema centraliza la información de destinos, permite la interacción social entre usuarios mediante reseñas y "me gusta", y provee herramientas de administración para gestionar el contenido de la plataforma.

El proyecto está orientado a ser un ejercicio académico práctico que abarca autenticación con JWT, diseño de APIs REST, manejo de roles, y buenas prácticas con Spring Boot 4.

### 1.2 Motivación

El Salvador cuenta con una gran diversidad de atractivos turísticos (playas, volcanes, sitios arqueológicos, pueblos coloniales) que no siempre están bien documentados en plataformas digitales accesibles. Este proyecto busca construir el backend de una plataforma que sirva como base para exponer esa información de manera estructurada y con funcionalidades sociales.

### 1.3 Objetivos del Proyecto

- Implementar una API REST segura con autenticación basada en JWT.
- Practicar el diseño de bases de datos relacionales con PostgreSQL.
- Aplicar principios de arquitectura limpia con Spring Boot 4 y Java 21.
- Modelar relaciones entre entidades: usuarios, destinos, reseñas, likes y guardados.
- Implementar control de acceso por roles (ADMIN y TOURIST).

---

## 2. Stack Tecnológico

| Componente         | Tecnología                          |
|--------------------|-------------------------------------|
| Lenguaje           | Java 21                             |
| Framework          | Spring Boot 4.0.7                   |
| Seguridad          | Spring Security + JWT               |
| Persistencia       | Spring Data JPA (Hibernate)         |
| Base de datos      | PostgreSQL                          |
| Validaciones       | Jakarta Bean Validation             |
| Mapeo de objetos   | MapStruct                           |
| Documentación API  | SpringDoc OpenAPI (Swagger UI)      |
| Build tool         | Maven                               |
| Control de versiones | Git + GitHub                      |

---

## 3. Alcance del Sistema

### 3.1 Dentro del alcance (IN SCOPE)

- Registro y autenticación de usuarios con JWT + Refresh Token.
- CRUD de tipos de destino (solo ADMIN).
- CRUD de destinos turísticos (solo ADMIN).
- Consulta pública de destinos (sin autenticación o con autenticación).
- Reseñas de destinos (crear, editar, eliminar).
- Sistema de "me gusta" (like/unlike) a destinos.
- Sistema de guardados (save/unsave) de destinos.
- Gestión de usuarios (listar, cambiar estado, eliminar) por ADMIN.
- Filtrado de destinos por tipo, ciudad y estado.
- Paginación y ordenamiento en listados.

### 3.2 Fuera del alcance (OUT OF SCOPE)

- Frontend / interfaz de usuario.
- Carga y almacenamiento de imágenes en servidor (los campos `url_avatar` y `url_banner` se gestionan con URLs externas; el cliente es responsable de subir la imagen a un servicio externo y proveer la URL).
- Integración con pasarelas de pago.
- Sistema de reservas.
- Notificaciones en tiempo real.
- Soporte multi-idioma.

---

## 4. Roles de Usuario

| Rol      | Descripción                                                                 |
|----------|-----------------------------------------------------------------------------|
| `ADMIN`  | Gestiona tipos de destino, destinos, y usuarios. Acceso total al sistema.  |
| `TOURIST`| Usuario registrado. Puede reseñar, dar like y guardar destinos.            |
| Público  | Sin registro. Solo puede consultar destinos y reseñas.                     |

---

## 5. Modelo de Datos

### 5.1 Diagrama de Entidades (resumen)

```
users ──< destination_review >── destination
users ──< destination_like    >── destination
users ──< destination_save    >── destination
users ──< refresh_token
destination_type ──< destination
destination ──< destination_image
```

### 5.2 Descripción de entidades

#### `users`
| Campo        | Tipo          | Descripción                              |
|--------------|---------------|------------------------------------------|
| id           | UUID (PK)     | Identificador único                      |
| name         | VARCHAR(100)  | Nombre completo del usuario              |
| email        | VARCHAR(100)  | Correo electrónico único                 |
| password     | VARCHAR(100)  | Contraseña encriptada (BCrypt)           |
| url_avatar   | VARCHAR(255)  | URL de imagen de perfil (opcional)       |
| role         | VARCHAR(100)  | `ADMIN` o `TOURIST`                      |
| state        | VARCHAR(100)  | `ACTIVE`, `INACTIVE`, `BANNED`           |
| created_at   | TIMESTAMP     | Fecha de registro                        |

#### `destination_type`
| Campo        | Tipo          | Descripción                              |
|--------------|---------------|------------------------------------------|
| id           | UUID (PK)     | Identificador único                      |
| name         | VARCHAR(255)  | Nombre del tipo (ej. Playa, Volcán)      |
| description  | TEXT          | Descripción del tipo de destino          |
| created_at   | TIMESTAMP     | Fecha de creación                        |

#### `destination`
| Campo        | Tipo            | Descripción                             |
|--------------|-----------------|-----------------------------------------|
| id           | UUID (PK)       | Identificador único                     |
| type_id      | UUID (FK)       | Referencia a `destination_type`         |
| name         | VARCHAR(255)    | Nombre del destino                      |
| description  | TEXT            | Descripción del destino                 |
| url_banner   | VARCHAR(255)    | URL de imagen principal del destino (opcional) |
| country      | VARCHAR(255)    | País (por defecto "El Salvador")        |
| city         | VARCHAR(255)    | Ciudad o municipio                      |
| latitude     | DECIMAL(10,2)   | Coordenada geográfica                   |
| longitude    | DECIMAL(10,2)   | Coordenada geográfica                   |
| state        | VARCHAR(100)    | `ACTIVE`, `INACTIVE`, `DRAFT`           |
| created_at   | TIMESTAMP       | Fecha de creación                       |

#### `destination_image`
| Campo          | Tipo          | Descripción                                  |
|----------------|---------------|----------------------------------------------|
| id             | UUID (PK)     | Identificador único                          |
| destination_id | UUID (FK)     | Referencia al destino al que pertenece       |
| url_image      | VARCHAR(255)  | URL de la imagen de la galería               |

> Nota: un destino puede tener múltiples imágenes en su galería. El campo `url_banner` en `destination` representa la imagen principal/portada; `destination_image` representa las imágenes adicionales.

#### `destination_review`
| Campo          | Tipo       | Descripción                              |
|----------------|------------|------------------------------------------|
| id             | UUID (PK)  | Identificador único                      |
| user_id        | UUID (FK)  | Usuario que escribe la reseña            |
| destination_id | UUID (FK)  | Destino al que pertenece la reseña       |
| content        | TEXT       | Texto de la reseña                       |
| value          | INTEGER    | Calificación del 1 al 5                  |
| created_at     | TIMESTAMP  | Fecha de publicación                     |

#### `destination_like`
| Campo          | Tipo       | Descripción                       |
|----------------|------------|-----------------------------------|
| id             | UUID (PK)  | Identificador único               |
| user_id        | UUID (FK)  | Usuario que da el like            |
| destination_id | UUID (FK)  | Destino que recibe el like        |
| created_at     | TIMESTAMP  | Fecha del like                    |

> Restricción: un usuario solo puede dar like una vez al mismo destino.

#### `destination_save`
| Campo          | Tipo       | Descripción                       |
|----------------|------------|-----------------------------------|
| id             | UUID (PK)  | Identificador único               |
| user_id        | UUID (FK)  | Usuario que guarda el destino     |
| destination_id | UUID (FK)  | Destino guardado                  |
| created_at     | TIMESTAMP  | Fecha del guardado                |

> Restricción: un usuario solo puede guardar un destino una vez.

#### `refresh_token`
| Campo      | Tipo          | Descripción                          |
|------------|---------------|--------------------------------------|
| id         | UUID (PK)     | Identificador único                  |
| user_id    | UUID (FK)     | Usuario propietario del token        |
| token      | VARCHAR(255)  | Valor del refresh token (único)      |
| expires_at | TIMESTAMP     | Fecha de expiración                  |
| created_at | TIMESTAMP     | Fecha de creación                    |

---

## 6. Requerimientos Funcionales

### RF-01 — Autenticación

| ID     | Requerimiento                                                                  |
|--------|--------------------------------------------------------------------------------|
| RF-01A | El sistema debe permitir el registro de nuevos usuarios con rol `TOURIST`.    |
| RF-01B | El sistema debe autenticar usuarios y retornar un JWT de acceso y un refresh token. |
| RF-01C | El sistema debe permitir renovar el access token usando un refresh token válido. |
| RF-01D | El sistema debe invalidar el refresh token al hacer logout.                    |
| RF-01E | Las contraseñas deben almacenarse encriptadas con BCrypt.                      |

---

### RF-02 — Gestión de Usuarios

| ID     | Requerimiento                                                                 |
|--------|-------------------------------------------------------------------------------|
| RF-02A | Un ADMIN puede listar todos los usuarios con paginación.                      |
| RF-02B | Un ADMIN puede cambiar el estado de un usuario (`ACTIVE`, `INACTIVE`, `BANNED`). |
| RF-02C | Un ADMIN puede eliminar un usuario.                                           |
| RF-02D | Un usuario autenticado puede ver y editar su propio perfil.                   |
| RF-02E | Un usuario puede actualizar su `url_avatar`.                                  |

---

### RF-03 — Tipos de Destino

| ID     | Requerimiento                                                               |
|--------|-----------------------------------------------------------------------------|
| RF-03A | Un ADMIN puede crear un tipo de destino.                                    |
| RF-03B | Un ADMIN puede editar un tipo de destino.                                   |
| RF-03C | Un ADMIN puede eliminar un tipo de destino (si no tiene destinos asociados).|
| RF-03D | Cualquier usuario (incluso sin autenticación) puede listar los tipos.       |

---

### RF-04 — Destinos Turísticos

| ID     | Requerimiento                                                                  |
|--------|--------------------------------------------------------------------------------|
| RF-04A | Un ADMIN puede crear un destino turístico.                                     |
| RF-04B | Un ADMIN puede editar un destino.                                              |
| RF-04C | Un ADMIN puede cambiar el estado de un destino (`ACTIVE`, `INACTIVE`, `DRAFT`). |
| RF-04D | Un ADMIN puede eliminar un destino.                                            |
| RF-04E | Cualquier usuario puede listar destinos en estado `ACTIVE`.                   |
| RF-04F | Cualquier usuario puede ver el detalle de un destino.                          |
| RF-04G | El listado de destinos debe permitir filtrar por `type_id`, `city` y `state`. |
| RF-04H | El listado de destinos debe incluir el conteo de likes, guardados y calificación promedio. |

---

### RF-05 — Reseñas

| ID     | Requerimiento                                                                 |
|--------|-------------------------------------------------------------------------------|
| RF-05A | Un usuario autenticado con rol `TOURIST` puede crear una reseña para un destino. |
| RF-05B | Un usuario solo puede tener una reseña por destino.                           |
| RF-05C | El autor de una reseña puede editarla.                                        |
| RF-05D | El autor de una reseña o un ADMIN puede eliminarla.                           |
| RF-05E | Cualquier usuario puede listar las reseñas de un destino.                     |
| RF-05F | El campo `value` debe ser un entero entre 1 y 5.                             |

---

### RF-06 — Me Gusta (Likes)

| ID     | Requerimiento                                                              |
|--------|----------------------------------------------------------------------------|
| RF-06A | Un usuario autenticado puede dar like a un destino.                       |
| RF-06B | Un usuario puede retirar su like de un destino.                           |
| RF-06C | Un usuario no puede dar like más de una vez al mismo destino.             |
| RF-06D | El detalle de un destino debe indicar si el usuario autenticado ya dio like. |

---

### RF-07 — Guardados (Saves)

| ID     | Requerimiento                                                                |
|--------|------------------------------------------------------------------------------|
| RF-07A | Un usuario autenticado puede guardar un destino.                            |
| RF-07B | Un usuario puede quitar un destino de sus guardados.                        |
| RF-07C | Un usuario no puede guardar el mismo destino más de una vez.                |
| RF-07D | Un usuario puede listar sus destinos guardados.                             |
| RF-07E | El detalle de un destino debe indicar si el usuario autenticado ya lo guardó. |

---

### RF-08 — Imágenes de Destino

| ID     | Requerimiento                                                                      |
|--------|------------------------------------------------------------------------------------|
| RF-08A | Un ADMIN puede agregar una o más imágenes a la galería de un destino.             |
| RF-08B | Un ADMIN puede eliminar una imagen específica de la galería de un destino.         |
| RF-08C | Cualquier usuario puede listar las imágenes de la galería de un destino.           |
| RF-08D | El detalle de un destino debe incluir su lista de imágenes de galería.            |

---

## 7. Requerimientos No Funcionales

| ID    | Requerimiento                                                                         |
|-------|--------------------------------------------------------------------------------------|
| RNF-01 | La API debe seguir los principios REST: uso correcto de verbos HTTP y códigos de respuesta. |
| RNF-02 | Todos los endpoints protegidos deben validar el JWT en el header `Authorization: Bearer {token}`. |
| RNF-03 | La API debe documentarse automáticamente con Swagger UI en `/swagger-ui.html`.       |
| RNF-04 | Las respuestas de error deben tener un formato estándar con `timestamp`, `status`, `message` y `path`. |
| RNF-05 | Los listados deben soportar paginación con los parámetros `page`, `size` y `sort`.   |
| RNF-06 | Las entidades deben usar UUIDs como identificadores primarios.                        |
| RNF-07 | El código debe seguir convenciones de Java y estar organizado por capas: `controller`, `service`, `repository`, `dto`, `entity`, `mapper`, `exception`. |
| RNF-08 | El sistema debe manejar excepciones globalmente con `@ControllerAdvice`.             |
| RNF-09 | Los campos de entrada deben validarse con anotaciones de Jakarta Bean Validation.    |

---

## 8. Diseño de la API REST

### Base URL
```
/api/v1
```

---

### 8.1 Autenticación — `/api/v1/auth`

| Método | Endpoint                  | Acceso  | Descripción                                   |
|--------|---------------------------|---------|-----------------------------------------------|
| POST   | `/auth/register`          | Público | Registrar un nuevo usuario con rol TOURIST.   |
| POST   | `/auth/login`             | Público | Iniciar sesión, retorna access + refresh token. |
| POST   | `/auth/refresh-token`     | Público | Renovar access token con refresh token válido. |
| POST   | `/auth/logout`            | Auth    | Invalidar el refresh token del usuario.       |

**Ejemplo Request — POST `/auth/register`:**
```json
{
  "name": "María Martínez",
  "email": "maria@example.com",
  "password": "SecurePass123!",
  "url_avatar": "https://cdn.example.com/avatars/maria.jpg"
}
```

**Ejemplo Response — POST `/auth/login`:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "a1b2c3d4-...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

---

### 8.2 Usuarios — `/api/v1/users`

| Método | Endpoint                   | Acceso       | Descripción                              |
|--------|----------------------------|--------------|------------------------------------------|
| GET    | `/users`                   | ADMIN        | Listar todos los usuarios (paginado).    |
| GET    | `/users/{id}`              | ADMIN / Owner| Ver perfil de usuario.                   |
| PUT    | `/users/{id}`              | Owner        | Actualizar perfil propio.                |
| PATCH  | `/users/{id}/state`        | ADMIN        | Cambiar estado del usuario.              |
| DELETE | `/users/{id}`              | ADMIN        | Eliminar usuario.                        |
| GET    | `/users/{id}/saves`        | Owner / ADMIN| Listar destinos guardados del usuario.   |
| GET    | `/users/{id}/likes`        | Owner / ADMIN| Listar destinos con like del usuario.    |

---

### 8.3 Tipos de Destino — `/api/v1/destination-types`

| Método | Endpoint                         | Acceso  | Descripción                          |
|--------|----------------------------------|---------|--------------------------------------|
| GET    | `/destination-types`             | Público | Listar todos los tipos de destino.   |
| GET    | `/destination-types/{id}`        | Público | Ver detalle de un tipo.              |
| POST   | `/destination-types`             | ADMIN   | Crear un tipo de destino.            |
| PUT    | `/destination-types/{id}`        | ADMIN   | Actualizar un tipo de destino.       |
| DELETE | `/destination-types/{id}`        | ADMIN   | Eliminar un tipo de destino.         |

---

### 8.4 Destinos — `/api/v1/destinations`

| Método | Endpoint                          | Acceso  | Descripción                                          |
|--------|-----------------------------------|---------|------------------------------------------------------|
| GET    | `/destinations`                   | Público | Listar destinos activos (paginado, filtrable).       |
| GET    | `/destinations/{id}`              | Público | Ver detalle de un destino.                           |
| POST   | `/destinations`                   | ADMIN   | Crear un destino.                                    |
| PUT    | `/destinations/{id}`              | ADMIN   | Actualizar un destino.                               |
| PATCH  | `/destinations/{id}/state`        | ADMIN   | Cambiar estado del destino.                          |
| DELETE | `/destinations/{id}`              | ADMIN   | Eliminar un destino.                                 |

**Query params para GET `/destinations`:**
```
?page=0&size=10&sort=name,asc&typeId={uuid}&city=Suchitoto&state=ACTIVE
```

---

### 8.5 Imágenes de Destino — `/api/v1/destinations/{destinationId}/images`

| Método | Endpoint                                        | Acceso  | Descripción                              |
|--------|-------------------------------------------------|---------|------------------------------------------|
| GET    | `/destinations/{destinationId}/images`         | Público | Listar imágenes de la galería.           |
| POST   | `/destinations/{destinationId}/images`         | ADMIN   | Agregar una imagen a la galería.         |
| DELETE | `/destinations/{destinationId}/images/{imageId}` | ADMIN | Eliminar una imagen de la galería.       |

**Ejemplo Request — POST `/destinations/{destinationId}/images`:**
```json
{
  "url_image": "https://cdn.example.com/destinations/lago-coatepeque-2.jpg"
}
```

---

### 8.6 Reseñas — `/api/v1/destinations/{destinationId}/reviews`

| Método | Endpoint                                  | Acceso         | Descripción                          |
|--------|-------------------------------------------|----------------|--------------------------------------|
| GET    | `/destinations/{destinationId}/reviews`  | Público        | Listar reseñas del destino.          |
| POST   | `/destinations/{destinationId}/reviews`  | TOURIST        | Crear reseña.                        |
| PUT    | `/destinations/{destinationId}/reviews/{reviewId}` | Owner | Actualizar reseña propia.    |
| DELETE | `/destinations/{destinationId}/reviews/{reviewId}` | Owner / ADMIN | Eliminar reseña. |

---

### 8.7 Likes — `/api/v1/destinations/{destinationId}/likes`

| Método | Endpoint                                 | Acceso  | Descripción                    |
|--------|------------------------------------------|---------|--------------------------------|
| POST   | `/destinations/{destinationId}/likes`   | TOURIST | Dar like a un destino.         |
| DELETE | `/destinations/{destinationId}/likes`   | TOURIST | Quitar like de un destino.     |

---

### 8.8 Guardados — `/api/v1/destinations/{destinationId}/saves`

| Método | Endpoint                                 | Acceso  | Descripción                        |
|--------|------------------------------------------|---------|-----------------------------------|
| POST   | `/destinations/{destinationId}/saves`   | TOURIST | Guardar un destino.               |
| DELETE | `/destinations/{destinationId}/saves`   | TOURIST | Quitar un destino de guardados.   |

---

## 9. Formato Estándar de Respuestas

### Respuesta exitosa (lista paginada)
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

### Respuesta de error estándar
```json
{
  "timestamp": "2026-06-23T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Destino con id '3fa85f64-...' no encontrado.",
  "path": "/api/v1/destinations/3fa85f64-..."
}
```

### Códigos de respuesta HTTP esperados

| Código | Uso                                                    |
|--------|--------------------------------------------------------|
| 200    | OK — Lectura o actualización exitosa.                 |
| 201    | Created — Recurso creado exitosamente.                |
| 204    | No Content — Eliminación exitosa.                     |
| 400    | Bad Request — Validación fallida o datos inválidos.   |
| 401    | Unauthorized — Token ausente o inválido.              |
| 403    | Forbidden — Sin permisos para la acción.              |
| 404    | Not Found — Recurso no existe.                        |
| 409    | Conflict — Duplicado (ej. like o guardado repetido).  |
| 500    | Internal Server Error — Error inesperado del servidor.|

---

## 10. Seguridad

### 10.1 JWT

- **Access Token:** vida útil corta (recomendado: 1 hora). Firmado con clave secreta (HS256 o RS256).
- **Refresh Token:** vida útil larga (recomendado: 7 días). Almacenado en BD, invalidable manualmente.
- Al hacer logout, el refresh token se elimina o se marca como expirado en la tabla `refresh_token`.

### 10.2 Control de Acceso por Rol

- Implementar con Spring Security usando `@PreAuthorize` o configuración en `SecurityFilterChain`.
- Un usuario con rol `TOURIST` no puede acceder a endpoints de administración.
- Un usuario solo puede editar/eliminar sus propios recursos (reseñas, perfil), excepto el ADMIN.

### 10.3 Validaciones de Seguridad

- No exponer contraseñas en ninguna respuesta.
- Sanitizar entradas para prevenir inyección SQL (JPA parametriza las consultas).
- Implementar manejo de expiración del token y respuesta 401 clara.

---

## 11. Estructura de Paquetes Sugerida

```
src/main/java/com/tuapp/turistico/
│
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── DestinationTypeController.java
│   ├── DestinationController.java
│   └── ReviewController.java
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── DestinationTypeService.java
│   ├── DestinationService.java
│   ├── ReviewService.java
│   ├── LikeService.java
│   └── SaveService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── DestinationTypeRepository.java
│   ├── DestinationRepository.java
│   ├── DestinationImageRepository.java
│   ├── DestinationReviewRepository.java
│   ├── DestinationLikeRepository.java
│   ├── DestinationSaveRepository.java
│   └── RefreshTokenRepository.java
│
├── entity/
│   ├── User.java
│   ├── DestinationType.java
│   ├── Destination.java
│   ├── DestinationImage.java
│   ├── DestinationReview.java
│   ├── DestinationLike.java
│   ├── DestinationSave.java
│   └── RefreshToken.java
│
├── dto/
│   ├── request/
│   └── response/
│
├── mapper/
│   └── (MapStruct mappers)
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── UnauthorizedException.java
│
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
│
└── TuristicoApplication.java
```

---

## 12. Plan de Desarrollo por Fases

### Fase 1 — Fundamentos (Semana 1-2)
- [ ] Configurar proyecto Spring Boot con dependencias base.
- [ ] Configurar base de datos PostgreSQL y `application.yml`.
- [ ] Crear entidades JPA y relaciones.
- [ ] Implementar migraciones con Flyway o Liquibase.

### Fase 2 — Autenticación (Semana 2-3)
- [ ] Implementar registro de usuario.
- [ ] Implementar login con generación de JWT.
- [ ] Implementar refresh token.
- [ ] Implementar logout.
- [ ] Configurar Spring Security y filtros JWT.

### Fase 3 — Tipos y Destinos (Semana 3-4)
- [ ] CRUD de tipos de destino.
- [ ] CRUD de destinos con paginación y filtros.
- [ ] Implementar cambio de estado de destinos.
- [ ] Gestión de imágenes de galería por destino (agregar y eliminar).

### Fase 4 — Interacciones Sociales (Semana 4-5)
- [ ] Sistema de reseñas (crear, editar, eliminar).
- [ ] Sistema de likes (dar y retirar).
- [ ] Sistema de guardados (guardar y quitar).
- [ ] Listar destinos guardados y con like por usuario.

### Fase 5 — Gestión y Pulido (Semana 5-6)
- [ ] Endpoints de gestión de usuarios (ADMIN).
- [ ] Manejo global de excepciones.
- [ ] Documentación con Swagger / OpenAPI.
- [ ] Pruebas de los endpoints principales.
- [ ] Refactoring y revisión de código.

---

## 13. Criterios de Aceptación

| Módulo             | Criterio                                                                   |
|--------------------|----------------------------------------------------------------------------|
| Auth               | Un usuario puede registrarse, loguearse, renovar token y hacer logout.    |
| Seguridad          | Endpoints protegidos retornan 401 sin token y 403 sin permisos.           |
| Destinos           | Se puede paginar, filtrar y ordenar la lista de destinos.                 |
| Reseñas            | Un usuario no puede crear dos reseñas para el mismo destino.              |
| Likes/Saves        | Se retorna 409 si el usuario intenta dar like/guardar dos veces.          |
| Errores            | Todas las excepciones retornan el formato de error estándar definido.     |
| Documentación      | La API es navegable desde Swagger UI sin configuración adicional.         |

---

## 14. Preguntas Abiertas y Decisiones Pendientes

| # | Pregunta                                                                 | Estado    |
|---|--------------------------------------------------------------------------|-----------|
| 1 | ¿Se requiere algún endpoint para buscar destinos por nombre (text search)? | Pendiente |
| 2 | ¿Los destinos tendrán imágenes? ¿Se almacenarán como URLs externas?      | ✅ Resuelto — campo `url_banner` (VARCHAR 255, opcional) con URL externa. |
| 3 | ¿El rol ADMIN se crea manualmente en BD o habrá un endpoint de registro admin? | Pendiente |
| 4 | ¿Se necesita un endpoint para estadísticas (top destinos, más guardados)? | Pendiente |
| 5 | ¿Se requiere que los destinos sean solo de El Salvador o también internacionales? | Pendiente |

---

*Documento generado como guía de desarrollo para proyecto académico.*
