# DOSW_LAB6_Rodriguez

# ToDo Full Stack

Laboratorio 6 · Desarrollo y Operaciones de Software (DOSW)
Escuela Colombiana de Ingeniería Julio Garavito
Docente: Rodrigo Gualtero

Aplicación web para gestionar tareas personales. Permite crear, consultar, editar, cambiar de estado y eliminar tareas desde una interfaz en React que consume una API REST en Spring Boot, con persistencia en PostgreSQL ejecutado en Docker.

## Integrante

Natalia Andrea Rodriguez Torres 

- Video de demostración: https://youtu.be/6RhqSoNWYNY?si=JfLxBJZMiZ0GHhpQ
- Informe del laboratorio: [todo-fullstack/docs/informe-laboratorio.md](todo-fullstack/docs/informe-laboratorio.md)
- Informe del laboratorio: [docs/informe-laboratorio.md](docs/informe-laboratorio.md)

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Front-end | React + Vite, Vitest, React Testing Library |
| Back-end | Java 21, Spring Boot 4, Maven, Spring Data JPA, Bean Validation |
| Base de datos | PostgreSQL 17 (imagen oficial `postgres:17-alpine` en Docker) |
| Pruebas back-end | JUnit 5, Mockito, AssertJ, MockMvc |
| Cobertura | JaCoCo |

## Arquitectura

```
React (componentes, hooks, taskApi.js)
        │  HTTP + JSON
        ▼
REST Controller  →  Service  →  Repository  →  Entity JPA
                                                    │
                                                    ▼
                                              PostgreSQL (Docker)
```

- **Controller:** expone `/api/v1/tasks`, valida la entrada con `@Valid` y devuelve los códigos HTTP.
- **Service:** contiene la lógica de negocio (valores por defecto, existencia de tareas, conversión Entity ↔ DTO).
- **Repository:** acceso a datos con `JpaRepository`.
- **Entity / DTO:** `TaskEntity` se mapea a la tabla `tasks`; los DTOs (`TaskCreateRequest`, `TaskUpdateRequest`, `TaskResponse`) definen lo que entra y sale de la API.
- **Manejo de errores:** `GlobalExceptionHandler` convierte las excepciones en respuestas 400 y 404 con formato `{ "status": ..., "message": ... }`.

## Estructura del repositorio

```
DOSW_LAB6_Rodriguez/
├── backend/        API Spring Boot
├── frontend/       Aplicación React + Vite
├── database/
│   └── 001_create_schema.sql
└── docs/
    ├── evidence/   Capturas de evidencia
    └── informe-laboratorio.md
```

## Modelo de datos

| Campo | Tipo | Obligatorio | Valor por defecto |
| --- | --- | --- | --- |
| `id` | Long | Sí | Generado |
| `title` | String (máx. 120) | Sí | |
| `description` | String (máx. 500) | No | |
| `status` | `PENDING`, `IN_PROGRESS`, `COMPLETED` | Sí | `PENDING` |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | Sí | `MEDIUM` |
| `dueDate` | LocalDate | No | |
| `createdAt` | LocalDateTime | Sí | Fecha y hora actual |

## Requisitos previos

- Docker Desktop
- Java 21
- Maven (o el wrapper `mvnw` incluido)
- Node.js y npm

## Cómo ejecutar el proyecto

Se necesitan tres terminales.

### 1. Base de datos (primera vez)

```
docker pull postgres:17-alpine
docker volume create todo-postgres-data
docker run -d --name todo-postgres -e POSTGRES_DB=todo_db -e POSTGRES_USER=todo_user -e POSTGRES_PASSWORD=todo_password -p 5432:5432 -v todo-postgres-data:/var/lib/postgresql/data postgres:17-alpine
docker cp database/001_create_schema.sql todo-postgres:/001_create_schema.sql
docker exec -i todo-postgres psql -U todo_user -d todo_db -f /001_create_schema.sql
```

Si el contenedor ya existe, basta con iniciarlo:

```
docker start todo-postgres
```

### 2. Back-end

```
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. La conexión a la base se configura en `application.properties` y puede sobrescribirse con las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.

### 3. Front-end

```
cd frontend
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

## API REST

Base: `http://localhost:8080/api/v1/tasks`

| Operación | Método | Endpoint | Códigos |
| --- | --- | --- | --- |
| Listar tareas | GET | `/api/v1/tasks` | 200 |
| Consultar tarea | GET | `/api/v1/tasks/{id}` | 200, 404 |
| Crear tarea | POST | `/api/v1/tasks` | 201, 400 |
| Actualizar tarea | PUT | `/api/v1/tasks/{id}` | 200, 400, 404 |
| Eliminar tarea | DELETE | `/api/v1/tasks/{id}` | 204, 404 |

Ejemplo de creación:

```
POST /api/v1/tasks
Content-Type: application/json

{
  "title": "Terminar laboratorio DOSW",
  "description": "Completar pruebas del backend",
  "priority": "HIGH",
  "dueDate": "2026-09-25"
}
```

Ejemplo de error:

```
{ "status": 404, "message": "Task with id 99 was not found" }
```
## Diagramas C4

Diagrama de contexto:

<img width="1175" height="633" alt="image" src="https://github.com/user-attachments/assets/447b47be-accd-4230-ac7b-a1eb5aeb5496" />

Link de donde fue realizado: https://lucid.app/lucidchart/9a371200-d70f-4c62-9cec-60442e0f5a12/edit?viewport_loc=-199%2C-183%2C2096%2C1159%2C0_0&invitationId=inv_71fcfe89-a623-4dda-9718-c33c5bb0b29f

Diagrama contenedores:

<img width="635" height="599" alt="image" src="https://github.com/user-attachments/assets/729b0049-bfa7-4ba8-829e-358d129a6e8b" />

Link de donde fue realizado: https://lucid.app/lucidchart/eca753a9-0322-4724-8531-a94fe01810a4/edit?viewport_loc=-137%2C-57%2C1669%2C1159%2C0_0&invitationId=inv_eb3c0ace-f843-47fd-b452-cfe4aa538766

Diagrama de componentes backend:

<img width="1451" height="564" alt="image" src="https://github.com/user-attachments/assets/189d333c-a161-4d0a-bdcd-232d9ef65b41" />

Diagrama de componentes frontend:

<img width="1088" height="507" alt="image" src="https://github.com/user-attachments/assets/325b6b12-8a15-4fa4-8e32-9f65023bdb1d" />

Link de donde fueron realizados: https://lucid.app/lucidchart/11e63f3c-bdc9-4a8b-8b8a-f71888644489/edit?viewport_loc=-18%2C296%2C3144%2C1738%2C0_0&invitationId=inv_3e241ff6-bb87-4592-b04d-54d4e4df1726

## Pruebas

Back-end (desde `backend`):

```
mvn clean test
mvn clean verify
```

`verify` genera además el reporte de cobertura en `target/site/jacoco/index.html`.

- `TaskServiceTest`: 9 pruebas del Service con el Repository simulado con Mockito.
- `TaskControllerTest`: 9 pruebas del Controller con MockMvc y el Service simulado.

Front-end (desde `frontend`):

```
npm test
```

Pruebas con Vitest y React Testing Library sobre `TaskForm`, `TaskList` y `TasksPage`, con las llamadas HTTP simuladas.

## Evidencias

Las capturas están en [docs/evidence](docs/evidence).

| N.º | Evidencia |
| --- | --- |
| 01 | PostgreSQL en Docker |
| 02 | Tabla `tasks` |
| 03 | Pruebas del Service |
| 04 | Pruebas del Controller |
| 05 | Reporte JaCoCo |
| 06 | API funcionando |
| 07 | Aplicación React |
| 08 | Creación desde React |
| 09 | Edición desde React |
| 10 | Eliminación desde React |
| 11 | Pruebas del front-end |

## Detener los servicios

```
docker stop todo-postgres
```

Los datos se conservan en el volumen `todo-postgres-data`. Para eliminar también los datos:

```
docker rm -f todo-postgres
docker volume rm todo-postgres-data
```
