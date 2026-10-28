# Informe de seguridad — App ToDo con Spring Security y JWT

## 1. Integrante

Natalia Andrea Rodriguez Torres

## 2. Enlaces

- Repositorio: https://github.com/nataliaRT12/DOSW_LAB7_Tovar_Solano_Rodriguez
- Rama del laboratorio: `feature/jwt-security`
- Video de demostración: _pendiente_

## 3. Resumen de la solución

La aplicación ToDo del laboratorio anterior se extendió con cuentas de usuario y tareas privadas. El backend en Spring Boot usa Spring Security con el soporte de OAuth2 Resource Server para validar JSON Web Tokens firmados con HS256 que emite el propio backend. Las contraseñas se guardan con BCrypt, cada tarea tiene un propietario (`user_id`) y todas las consultas de tareas filtran por el usuario autenticado. Se agregó un endpoint administrativo (`/api/v1/admin/summary`) exclusivo del rol `ADMIN`. El frontend en React incorpora registro, inicio de sesión y cierre de sesión, y conserva el token solo en memoria.

Los detalles de diseño (diagramas C4, de clases, entidad-relación y flujo JWT), el contrato de endpoints y las decisiones de seguridad están en el [README](../README.md).

## 4. Preguntas de comprensión

### 1. ¿Cuál es la diferencia entre autenticación y autorización? Dé un ejemplo real de ToDo.

La **autenticación** responde a *quién es* el usuario: verifica su identidad. La **autorización** responde a *qué puede hacer* ese usuario ya identificado.

En ToDo, la autenticación ocurre en `POST /api/v1/auth/login`: `AuthServiceImpl` busca el usuario por correo y compara la contraseña con `PasswordEncoder.matches`. Si es correcta, emite un JWT. En cada petición posterior, el `SecurityFilterChain` vuelve a autenticar al usuario validando ese token.

La autorización ocurre después y tiene dos niveles:

- **Por rol:** `SecurityConfig` exige `hasRole("ADMIN")` en `/api/v1/admin/**`. Ana está autenticada, pero al consultar `/admin/summary` recibe `403 Forbidden` porque su rol es `USER`.
- **Por propiedad:** Luis está autenticado y puede usar `/api/v1/tasks`, pero `TaskServiceImpl` solo le permite operar sobre las tareas cuyo `owner` es él. Cuando intenta modificar la tarea 4 de Ana, recibe `404`.

### 2. Explique cómo se genera un JWT, qué contienen sus partes y cómo verifica Spring Boot que no fue manipulado.

**Generación.** Después de un login válido, `TokenService.issue` construye un `JwtClaimsSet` y lo firma con `NimbusJwtEncoder` usando una llave HMAC (`SecretKey`) que se crea a partir de la variable `JWT_SECRET`. El resultado es una cadena con tres partes separadas por puntos, cada una codificada en Base64URL:

1. **Header:** el algoritmo de firma, `{"alg":"HS256"}`.
2. **Payload (claims):** `sub` (id del usuario), `iss` (`todo-api`), `iat` (fecha de emisión), `exp` (expiración, 30 minutos después), `email` y `roles` (por ejemplo `["USER"]`).
3. **Firma:** `HMAC-SHA256(base64url(header) + "." + base64url(payload), llave secreta)`.

**Verificación.** En cada petición con `Authorization: Bearer <token>`, el filtro del resource server llama a `NimbusJwtDecoder`, configurado en `SecurityConfig.jwtDecoder` con la misma llave. El decoder:

1. Recalcula la firma HMAC con la llave del servidor y la compara con la que trae el token. Si alguien cambió un solo carácter del header o del payload, la firma ya no coincide y el token se rechaza.
2. Verifica `exp` (y `nbf` si existe) con `JwtTimestampValidator`.
3. Verifica que `iss` sea `todo-api` y que exista `sub`.

Si alguna validación falla, `SecurityErrorHandler` responde `401`. Si todo es correcto, `JwtAuthenticationConverter` convierte el claim `roles` en las autoridades `ROLE_USER` o `ROLE_ADMIN`, y el controlador recibe el `Jwt` con `@AuthenticationPrincipal`.

Como generación y validación usan el mismo bean `SecretKey`, no puede haber desajustes de configuración entre ambas. Esto se comprueba en `TokenServiceTest`: un token firmado con otra llave, un token alterado, uno vencido y uno de otro emisor son rechazados.

### 3. ¿Por qué decodificar el payload de un JWT no equivale a validar su autenticidad?

El payload solo está **codificado** en Base64URL, no cifrado. Cualquiera puede decodificarlo (por ejemplo en jwt.io) y leer `sub`, `email` o `roles`. Decodificarlo no comprueba nada sobre quién lo creó.

Un atacante podría tomar un token, cambiar `"roles":["USER"]` por `"roles":["ADMIN"]` o `sub` por el id de otra persona, volver a codificarlo y enviarlo. Si el servidor solo decodificara, aceptaría esa identidad falsa. Lo que da autenticidad es la **firma**: solo quien tiene la llave secreta puede producir una firma válida para ese contenido. Por eso el backend siempre usa `JwtDecoder.decode`, que verifica la firma y las fechas antes de confiar en los claims.

En la prueba `SecurityIntegrationTest.pt08_tamperedTokenReturns401` se cambia un carácter del payload de un token real y la API responde `401`. En la verificación manual, el mismo caso aparece en la captura `11-sin-token-y-alterado.png`.

Por la misma razón, en el payload no se guardan datos sensibles: cualquiera que tenga el token puede leerlos.

### 4. ¿Qué sucede exactamente cuando un token vence y el usuario intenta consultar sus tareas?

1. React llama a `GET /api/v1/tasks` con el token vencido en la cabecera `Authorization`.
2. El `BearerTokenAuthenticationFilter` de Spring Security extrae el token y llama a `NimbusJwtDecoder`.
3. La firma es correcta, pero `JwtTimestampValidator` detecta que la fecha actual es posterior a `exp` (con una tolerancia de 60 segundos) y la validación falla.
4. La petición nunca llega a `TaskController`. `SecurityErrorHandler.commence` responde `401` con `{"status":401,"message":"Authentication required: missing, invalid or expired token"}`.
5. En el frontend, `taskApi` lanza un `ApiError` con `status = 401`. El hook `useTasks` lo detecta y llama a `expireSession()` del `AuthContext`.
6. `AuthContext` borra el token y el usuario del estado y guarda el aviso *"Your session expired. Please sign in again."*. Como ya no hay usuario, `App` muestra `AuthPage` con ese aviso, y las tareas desaparecen de la pantalla.
7. El usuario debe iniciar sesión otra vez para obtener un token nuevo, porque en esta entrega no hay refresh tokens.

Esto se prueba en `SecurityIntegrationTest.pt09_expiredTokenReturns401` (backend) y en `App.test.jsx` con el caso PT-17 (frontend).

### 5. ¿Por qué no se deben almacenar las contraseñas en texto plano? ¿Cómo verifican la contraseña durante el login?

Si las contraseñas se guardaran en texto plano, cualquier filtración de la base de datos (un respaldo expuesto, una inyección SQL o un acceso indebido de alguien con permisos) revelaría directamente las credenciales de todos los usuarios. Como muchas personas reutilizan sus contraseñas, el daño se extendería a otros servicios. Tampoco el administrador del sistema debería poder conocerlas.

En ToDo, `AuthServiceImpl.register` guarda `passwordEncoder.encode(password)` usando `BCryptPasswordEncoder`. BCrypt:

- genera una **sal aleatoria** por contraseña, así que dos usuarios con la misma contraseña tienen hashes distintos;
- es **lento a propósito** (factor de costo 10), lo que hace costosos los ataques de fuerza bruta;
- produce un valor como `$2a$10$...` que incluye el algoritmo, el costo y la sal.

Durante el login no se "descifra" nada, porque un hash no se puede revertir. `AuthServiceImpl.login` busca al usuario por correo y llama a `passwordEncoder.matches(contraseñaIngresada, hashGuardado)`. BCrypt toma la sal del hash guardado, vuelve a calcular el hash de la contraseña ingresada y compara los resultados. Si el correo no existe o la contraseña no coincide, se lanza la misma `InvalidCredentialsException` (`401`), para no revelar cuál de los dos datos era incorrecto.

La contraseña tampoco se devuelve nunca: `UserResponse` solo tiene `id`, `name`, `email` y `role`. La captura `06-bd-password-hash.png` muestra los hashes en PostgreSQL, y `AuthServiceTest` verifica que lo guardado empieza por `$2` y no es la contraseña original.

### 6. ¿Qué diferencia existe entre responder 401 Unauthorized y 403 Forbidden? Dé ejemplos de los endpoints implementados.

- **401 Unauthorized:** el servidor **no sabe quién eres** o no confía en tu credencial. La solución es autenticarse, es decir, iniciar sesión de nuevo.
- **403 Forbidden:** el servidor **sabe quién eres**, pero no tienes permiso para ese recurso. Volver a iniciar sesión con la misma cuenta no cambia nada.

Ejemplos en ToDo:

| Petición | Respuesta | Motivo |
| --- | --- | --- |
| `GET /api/v1/tasks` sin cabecera `Authorization` | `401` | No hay identidad |
| `GET /api/v1/tasks` con un token alterado, vencido o de otro emisor | `401` | La credencial no es válida |
| `POST /api/v1/auth/login` con contraseña incorrecta | `401` | Falló la autenticación |
| `GET /api/v1/admin/summary` con el token válido de Ana (`USER`) | `403` | Identidad válida, pero sin rol `ADMIN` |
| `GET /api/v1/admin/summary` con el token del administrador | `200` | Identidad y rol correctos |

En el frontend la diferencia también importa: ante un `401`, `useTasks` cierra la sesión y lleva al login; un `403` no debería cerrar la sesión, porque el usuario sigue autenticado.

### 7. ¿Cómo impidieron que un usuario editara una tarea de otro, incluso con un JWT válido?

El JWT de Luis es auténtico, así que pasa todos los filtros de Spring Security. La protección está en la capa de negocio y de datos:

1. **La identidad sale del token, no del cliente.** `TaskController` recibe `@AuthenticationPrincipal Jwt jwt` y obtiene el id del usuario de `jwt.getSubject()`. Ningún endpoint recibe el id del propietario en la URL ni en el cuerpo, así que el cliente no puede elegirlo.
2. **Todas las operaciones se filtran por propietario.** `TaskServiceImpl` nunca usa `findById(id)`. Usa `TaskRepository.findByIdAndOwnerId(id, ownerId)`, tanto para consultar como para actualizar o eliminar. Listar usa `findAllByOwnerId(ownerId)`.
3. **Una tarea ajena se trata como inexistente.** Si la tarea no existe o es de otro usuario, la consulta devuelve vacío y se lanza `TaskNotFoundException`, que responde `404`. Así no se revela que la tarea 4 existe y pertenece a otra persona.
4. **Al crear, el propietario lo asigna el servidor:** `entity.setOwner(userRepository.getReferenceById(ownerId))`.

Las pruebas `SecurityIntegrationTest.pt10`, `pt11` y `pt12` hacen que Luis intente consultar, modificar y eliminar una tarea de Ana con su propio token válido. Responden `404` y, además, verifican en la base de datos que la tarea de Ana sigue igual. Las capturas `10-luis-tarea-ana.png` y `15-bd-despues-del-ataque.png` muestran lo mismo en la verificación manual.

### 8. ¿Por qué ocultar botones en React no es suficiente para proteger los datos?

El frontend se ejecuta en el navegador del usuario, que es un entorno que el usuario controla por completo. Cualquier persona puede abrir las herramientas de desarrollador, leer el token que usa la aplicación y llamar directamente a la API con `curl`, Postman o PowerShell, sin pasar por la interfaz. También puede modificar el código JavaScript que se ejecuta en su navegador.

Si la única protección fuera no mostrar el botón "Delete" para las tareas ajenas, Luis podría enviar igualmente `DELETE /api/v1/tasks/4`. Por eso todas las reglas se aplican en el backend: el `SecurityFilterChain` exige el token, `hasRole("ADMIN")` protege el recurso administrativo y `TaskServiceImpl` filtra por propietario. La interfaz solo refleja esas reglas para dar una buena experiencia, pero no es la barrera de seguridad.

Esto se demostró en la Parte 8: con un cliente HTTP en PowerShell, sin usar React, Luis intentó modificar y eliminar la tarea de Ana y el servidor respondió `404`.

### 9. ¿Qué ventajas y limitaciones tuvo conservar el JWT en memoria en el frontend?

En `AuthContext` el token y el usuario viven en el estado de React (`useState`). No se escriben en `localStorage`, en `sessionStorage` ni en cookies. La prueba de `App.test.jsx` verifica que ambos almacenamientos quedan vacíos después del login.

**Ventajas**

- Un script malicioso inyectado en la página (XSS) no puede leer el token desde `localStorage`, que es el primer lugar donde se buscaría.
- El token desaparece al cerrar la pestaña o recargar, así que no queda olvidado en un computador compartido.
- El cierre de sesión es simple y completo: basta con limpiar el estado, y al desmontarse `TasksPage` también se borran las tareas del usuario anterior.

**Limitaciones**

- **Al recargar la página se pierde la sesión** y hay que volver a iniciar sesión. La sesión tampoco se comparte entre pestañas.
- Si la página tuviera una vulnerabilidad XSS, el script podría actuar mientras la pestaña está abierta (por ejemplo, haciendo peticiones con el token en memoria). Guardar en memoria reduce el riesgo, pero no lo elimina.
- Como no hay refresh token, al vencer los 30 minutos el usuario debe iniciar sesión de nuevo aunque esté usando la aplicación.

La alternativa habitual en producción es una cookie `HttpOnly`, `Secure` y `SameSite`, que JavaScript no puede leer, junto con protección CSRF y un refresh token. El laboratorio no la exigía.

### 10. ¿Qué limitación tiene el cierre de sesión sin revocación de tokens? ¿Qué solución propondría para un sistema real?

En ToDo, "Sign out" solo borra el token del estado de React. El servidor no guarda sesiones (`STATELESS`) ni lleva registro de los tokens emitidos, así que **el JWT sigue siendo válido hasta su `exp`**. Si alguien lo copió antes del cierre de sesión (por ejemplo, desde las herramientas del navegador, un proxy o un log), puede seguir usándolo hasta 30 minutos después. Lo mismo ocurre si se cambia el rol de un usuario: su token anterior conserva el rol viejo hasta que expire.

Para un sistema real propondría:

1. **Access tokens de vida corta** (5 a 15 minutos) junto con un **refresh token** guardado en una cookie `HttpOnly` y registrado en la base de datos. Al cerrar sesión se elimina o marca como revocado el refresh token, y el access token muere pronto por sí solo.
2. **Lista de revocación (denylist):** guardar el identificador del token (claim `jti`) en una tabla o en Redis al cerrar sesión, con caducidad igual a su `exp`. Un validador adicional en el `JwtDecoder` rechazaría los tokens de esa lista.
3. **Versión de token por usuario:** un campo `token_version` en `users` que se incluye en el JWT y se incrementa al cerrar sesión en todos los dispositivos, al cambiar la contraseña o al cambiar el rol. Si la versión del token no coincide con la de la base de datos, se rechaza.

Cualquiera de estas opciones reintroduce algo de estado en el servidor. Ese es el costo de poder revocar tokens antes de que expiren.

### 11. ¿Cuáles son las consecuencias de almacenar una llave de firma JWT dentro de GitHub?

Con HS256, la misma llave sirve para **firmar** y para **verificar**. Si la llave está en el repositorio, cualquiera que lo vea (o que vea su historial, aunque el archivo se borre después) puede:

- **Fabricar tokens válidos** para cualquier usuario, con cualquier `sub` y con `"roles":["ADMIN"]`, y el backend los aceptaría como auténticos.
- Hacerse pasar por cualquier persona, leer, modificar y eliminar todas las tareas y acceder al recurso administrativo, saltándose el login y BCrypt por completo.
- Hacerlo sin dejar rastro de un ataque, porque para el servidor son tokens legítimos.

Además, borrar la llave en un commit posterior no la elimina del historial de Git. Habría que **rotar la llave** de inmediato, lo que invalida todos los tokens emitidos, y limpiar el historial.

Por eso en ToDo la llave no está en el código:

- `application.properties` tiene `jwt.secret=${JWT_SECRET}` **sin valor por defecto**.
- El valor se lee de una variable de entorno o del archivo `backend/.env`, que está en `.gitignore`. En el repositorio solo existe `backend/.env.example`, con la variable vacía.
- `SecurityConfig.jwtSigningKey` impide arrancar con una llave de menos de 32 bytes.
- Las pruebas usan llaves propias, definidas en el perfil `test`, que no sirven para la aplicación real.

### 12. ¿Qué pruebas del equipo demuestran la seguridad del CRUD y no solamente su funcionamiento correcto?

Una prueba de funcionamiento verifica que el camino feliz funciona (crear una tarea devuelve `201`). Una prueba de seguridad verifica que **lo que no debería pasar, no pasa**, y que no hubo efectos secundarios. Las pruebas que cumplen ese rol son:

**Integración de punta a punta (`SecurityIntegrationTest`)**, con la aplicación completa, Spring Security real, JWT reales y base de datos:

- `pt06`: sin token, `401`.
- `pt08`: token con un carácter modificado, `401`.
- `pt09`: token firmado correctamente pero vencido, `401`.
- `pt10`, `pt11` y `pt12`: Luis, con su JWT válido, intenta consultar, modificar y eliminar una tarea de Ana. Responden `404` y **se verifica en la base de datos** que la tarea no cambió y sigue existiendo.
- `pt07`: con dos usuarios con tareas, cada uno recibe solo las suyas.
- `pt13`: el propietario de una tarea nueva es el usuario del token.
- `pt14` y `pt15`: `USER` recibe `403` y `ADMIN` recibe `200` en `/admin/summary`.
- `registerIgnoresAdminRoleSentByClient`: enviar `"role":"ADMIN"` en el registro no da privilegios.
- `pt01_pt03`: la contraseña se guarda como hash BCrypt y no aparece en la respuesta.

**Validación del token (`TokenServiceTest`):** rechazo de tokens alterados, vencidos, de otro emisor o firmados con otra llave, y rechazo de llaves de menos de 32 bytes.

**Reglas de propiedad (`TaskServiceTest`, `TaskRepositoryTest`):** las consultas filtran por propietario y una tarea ajena no se guarda ni se elimina (`verify(repository, never()).save(...)`).

**Frontend (`App.test.jsx`):** el token no queda en `localStorage` ni en `sessionStorage`; al cerrar sesión desaparecen las tareas (PT-16); un `401` lleva al login (PT-17); el siguiente usuario no ve las tareas del anterior (PT-18).

La matriz completa PT-01 a PT-18 está en el README. En total hay 89 pruebas en el backend y 34 en el frontend, con 95,3 % de cobertura de instrucciones. La cobertura por sí sola no demuestra seguridad: lo que la demuestra son estos casos de abuso explícitos.

## 5. Conclusiones

- Spring Security con OAuth2 Resource Server permitió validar JWT propios con poca configuración. La clave fue usar el mismo `SecretKey` para el `JwtEncoder` y el `JwtDecoder`, y agregar el validador del emisor.
- La autenticación sola no protege los datos: la regla más importante del laboratorio fue filtrar por propietario en el servicio y en el repositorio, tomando la identidad del token.
- Responder `404` ante tareas ajenas evita filtrar información sobre la existencia de registros de otros usuarios.
- Guardar el token en memoria es más seguro que `localStorage`, pero tiene un costo de usabilidad (se pierde al recargar), y el cierre de sesión sin revocación deja una ventana de riesgo de hasta 30 minutos.
- La migración `003` mostró que agregar una restricción `NOT NULL` a datos existentes requiere un paso intermedio: asignar primero un propietario a las tareas anteriores.
