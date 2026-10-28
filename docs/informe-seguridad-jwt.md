# Informe de seguridad — App ToDo con Spring Security y JWT

## 1. Integrantes del equipo

| Nombre | Correo institucional | Usuario de GitHub |
|---|---|---|
| Mariana Tovar | mariana.tovar-c@mail.escuelaing.edu.co | marix201 |
| Paula Andrea Solano Morales | paula.solano-m@mail.escuelaing.edu.co | AndreaSolano23 |
| Natalia Andrea Rodríguez Torres | natalia.rodriguez-t@mail.escuelaing.edu.co | nataliaRT12 |

## 2. Enlaces

- Repositorio: https://github.com/nataliaRT12/DOSW_LAB7_Tovar_Solano_Rodriguez
- Video de demostración: _pendiente_

## 3. Resumen de la solución

La aplicación ToDo del laboratorio anterior se extendió con cuentas de usuario y tareas privadas. El backend en Spring Boot usa Spring Security como servidor de recursos OAuth2 para validar los JSON Web Tokens que él mismo emite, firmados con HS256. Las contraseñas se guardan con BCrypt, cada tarea tiene un propietario y todas las consultas de tareas se limitan al usuario que hace la petición. Además, se agregó un resumen administrativo que solo puede consultar el rol ADMIN. En el frontend se añadieron las vistas de registro, inicio y cierre de sesión, y el token se conserva únicamente en memoria.

Los diagramas, el contrato de la API y las decisiones de seguridad se encuentran en el [README](../README.md).

## 4. Preguntas de comprensión

### 1. ¿Cuál es la diferencia entre autenticación y autorización? Dé un ejemplo real de ToDo.

La autenticación consiste en comprobar quién es el usuario; la autorización, en decidir qué puede hacer una vez sabemos quién es.

En ToDo la autenticación ocurre al iniciar sesión: el servicio de autenticación busca al usuario por su correo y compara la contraseña ingresada con el hash guardado. Si coinciden, entrega un JWT, y a partir de ahí cada petición se autentica validando ese token.

La autorización tiene dos niveles. El primero es por rol: el resumen administrativo exige el rol ADMIN, así que Ana, aunque haya iniciado sesión correctamente, recibe un 403 al intentar consultarlo porque su rol es USER. El segundo es por propiedad: Luis puede usar el CRUD de tareas, pero el servicio solo le deja trabajar con las tareas que le pertenecen, de modo que cuando intenta modificar una tarea de Ana recibe un 404.

### 2. Explique cómo se genera un JWT, qué contienen sus partes y cómo verifica Spring Boot que no fue manipulado.

Cuando el inicio de sesión es correcto, el servicio de tokens arma el contenido del JWT y lo firma con una llave secreta que se lee de la variable de entorno JWT_SECRET. El resultado son tres partes codificadas en Base64URL y separadas por puntos:

1. El encabezado, que indica el algoritmo de firma (HS256).
2. El cuerpo, con los datos del usuario: su identificador como sujeto (sub), el emisor (todo-api), la fecha de emisión, la fecha de expiración —30 minutos después—, el correo y los roles.
3. La firma, que se obtiene aplicando HMAC-SHA256 sobre el encabezado y el cuerpo con la llave secreta.

En cada petición protegida, Spring Security toma el token de la cabecera Authorization y lo valida con un decodificador configurado con la misma llave. Primero vuelve a calcular la firma y la compara con la que trae el token: si alguien cambió aunque sea un carácter del encabezado o del cuerpo, ya no coinciden. Después revisa que el token no haya vencido, que el emisor sea el esperado y que tenga sujeto. Si algo falla, la respuesta es 401. Si todo está bien, los roles del token se convierten en los permisos ROLE_USER o ROLE_ADMIN y el controlador recibe la identidad del usuario.

Como el token se crea y se valida con la misma llave, no hay riesgo de que las dos partes queden configuradas de forma distinta. Las pruebas del servicio de tokens lo confirman: rechazan tokens alterados, vencidos, de otro emisor o firmados con otra llave.

### 3. ¿Por qué decodificar el payload de un JWT no equivale a validar su autenticidad?

Porque el cuerpo de un JWT solo está codificado, no cifrado. Cualquier persona puede decodificarlo, por ejemplo en jwt.io, y leer el identificador, el correo o los roles. Decodificar no dice nada sobre quién creó el token.

Si el servidor se limitara a decodificar, un atacante podría cambiar su rol de USER a ADMIN o poner el identificador de otra persona, volver a codificar el token y enviarlo. Lo que garantiza que el contenido es legítimo es la firma, porque solo quien tiene la llave secreta puede producir una firma válida para ese contenido. Por eso el backend siempre verifica la firma y las fechas antes de confiar en los datos.

En las pruebas de integración se modifica un carácter de un token real y la API responde 401. Lo mismo se ve en la verificación manual del laboratorio. Por esta misma razón, en el token no se guarda información sensible.

### 4. ¿Qué sucede exactamente cuando un token vence y el usuario intenta consultar sus tareas?

1. React pide la lista de tareas enviando el token vencido.
2. Spring Security intenta validarlo. La firma es correcta, pero la fecha actual ya pasó la expiración (se admite un margen de 60 segundos), así que la validación falla.
3. La petición no llega al controlador de tareas. El servidor responde 401 con un mensaje en JSON que indica que la autenticación es necesaria.
4. En el frontend, el hook de tareas detecta el 401 y le pide al contexto de autenticación que expire la sesión.
5. El contexto borra el token y los datos del usuario y guarda el aviso "Your session expired. Please sign in again.". Como ya no hay usuario, la aplicación vuelve a la pantalla de inicio de sesión con ese mensaje y las tareas dejan de mostrarse.
6. El usuario tiene que iniciar sesión otra vez para recibir un token nuevo, porque en esta entrega no se usan refresh tokens.

Este comportamiento está cubierto por una prueba de integración del backend y por la prueba PT-17 del frontend.

### 5. ¿Por qué no se deben almacenar las contraseñas en texto plano? ¿Cómo verifican la contraseña durante el login?

Si las contraseñas estuvieran en texto plano, cualquier filtración de la base de datos —un respaldo expuesto, una inyección SQL o alguien con más permisos de los que debería— dejaría a la vista las credenciales de todos los usuarios. Como mucha gente repite contraseñas, el problema se extendería a otros servicios. Además, ni siquiera quien administra el sistema debería poder conocerlas.

En ToDo, al registrar a un usuario se guarda el resultado de BCrypt en lugar de la contraseña. BCrypt agrega una sal aleatoria a cada contraseña, de modo que dos personas con la misma clave tienen hashes distintos; además es lento a propósito, lo que encarece los ataques de fuerza bruta. El valor guardado tiene la forma $2a$10$... e incluye el algoritmo, el costo y la sal.

Durante el inicio de sesión no se descifra nada, porque un hash no se puede revertir. El servicio busca al usuario por su correo y le pide a BCrypt que compare la contraseña ingresada con el hash guardado: BCrypt toma la sal del hash, calcula de nuevo el hash de lo que escribió el usuario y compara ambos resultados. Si el correo no existe o la contraseña no coincide, la respuesta es la misma (401), para no revelar cuál de los dos datos estaba mal.

La contraseña tampoco aparece nunca en las respuestas de la API, que solo devuelven el identificador, el nombre, el correo y el rol. En la evidencia del laboratorio se ven los hashes guardados en PostgreSQL, y las pruebas comprueban que lo almacenado no es la contraseña original.

### 6. ¿Qué diferencia existe entre responder 401 Unauthorized y 403 Forbidden? Dé ejemplos de los endpoints implementados.

Un 401 significa que el servidor no sabe quién es el usuario o no confía en su credencial; la solución es autenticarse de nuevo. Un 403 significa que el servidor sí sabe quién es, pero esa persona no tiene permiso para el recurso; volver a iniciar sesión con la misma cuenta no cambia nada.

| Petición | Respuesta | Motivo |
| --- | --- | --- |
| Consultar las tareas sin enviar token | 401 | No hay identidad |
| Consultar las tareas con un token alterado, vencido o de otro emisor | 401 | La credencial no es válida |
| Iniciar sesión con una contraseña incorrecta | 401 | Falló la autenticación |
| Consultar el resumen administrativo con el token de Ana (USER) | 403 | La identidad es válida, pero no tiene el rol ADMIN |
| Consultar el resumen administrativo con el token del administrador | 200 | Identidad y rol correctos |

En el frontend la diferencia también importa: ante un 401 la aplicación cierra la sesión y lleva al inicio de sesión, mientras que un 403 no debería cerrarla, porque el usuario sigue autenticado.

### 7. ¿Cómo impidieron que un usuario editara una tarea de otro, incluso con un JWT válido?

El token de Luis es auténtico, así que pasa todos los filtros de seguridad. La protección está en la lógica de negocio y en el acceso a datos:

1. La identidad se toma del token, no de lo que envía el cliente. El controlador de tareas obtiene el identificador del usuario a partir del sujeto del JWT, y ningún endpoint recibe el propietario en la URL ni en el cuerpo de la petición.
2. Todas las operaciones filtran por propietario. El servicio de tareas nunca busca una tarea solo por su identificador: siempre busca por identificador y propietario a la vez, tanto para consultar como para editar o eliminar. Para listar, trae únicamente las tareas del usuario.
3. Una tarea ajena se trata como si no existiera. Si la búsqueda no encuentra nada, ya sea porque la tarea no existe o porque es de otra persona, se responde 404. Así Luis no puede saber que la tarea existe ni a quién pertenece.
4. Al crear una tarea, el propietario lo asigna el servidor a partir del token.

En las pruebas de integración, Luis intenta consultar, modificar y eliminar una tarea de Ana con su propio token válido; las tres operaciones responden 404 y luego se comprueba en la base de datos que la tarea sigue igual. La verificación manual muestra lo mismo.

### 8. ¿Por qué ocultar botones en React no es suficiente para proteger los datos?

Porque el frontend se ejecuta en el navegador del usuario, y ese es un entorno que el usuario controla por completo. Cualquiera puede abrir las herramientas de desarrollador, copiar el token que usa la aplicación y llamar a la API directamente con curl, Postman o PowerShell, sin pasar por la interfaz. También puede modificar el JavaScript que corre en su navegador.

Si la única protección fuera esconder el botón de eliminar en las tareas ajenas, Luis podría enviar la petición de borrado de todas formas. Por eso todas las reglas se aplican en el backend: Spring Security exige el token, el resumen administrativo exige el rol ADMIN y el servicio de tareas filtra por propietario. La interfaz solo refleja esas reglas para que la experiencia sea clara, pero no es lo que protege los datos.

En la verificación manual se comprobó justamente esto: desde PowerShell, sin usar React, Luis intentó modificar y eliminar una tarea de Ana y el servidor lo rechazó.

### 9. ¿Qué ventajas y limitaciones tuvo conservar el JWT en memoria en el frontend?

El token y los datos del usuario se guardan en el estado de React, dentro del contexto de autenticación. No se escriben en localStorage, en sessionStorage ni en cookies, y hay una prueba que verifica que ambos almacenamientos del navegador quedan vacíos después de iniciar sesión.

Ventajas:

- Un script malicioso inyectado en la página no encuentra el token guardado en el navegador, que es el primer lugar donde se buscaría.
- El token desaparece al cerrar la pestaña o recargar, así que no queda olvidado en un computador compartido.
- Cerrar sesión es sencillo y completo: basta con limpiar el estado, y al salir de la vista de tareas también se borran las tareas del usuario anterior.

Limitaciones:

- Al recargar la página se pierde la sesión y hay que volver a iniciar sesión. Tampoco se comparte entre pestañas.
- Si la página tuviera una vulnerabilidad XSS, un script podría hacer peticiones con el token mientras la pestaña esté abierta. Guardarlo en memoria reduce el riesgo, pero no lo elimina.
- Como no hay refresh token, cuando pasan los 30 minutos el usuario debe iniciar sesión otra vez aunque esté usando la aplicación.

En producción lo habitual sería usar una cookie HttpOnly, Secure y SameSite, que JavaScript no puede leer, junto con protección CSRF y un refresh token. El laboratorio no lo pedía.

### 10. ¿Qué limitación tiene el cierre de sesión sin revocación de tokens? ¿Qué solución propondría para un sistema real?

En ToDo, cerrar sesión solo borra el token del estado de React. El servidor no guarda sesiones ni lleva registro de los tokens que emite, así que el JWT sigue siendo válido hasta que vence. Si alguien lo copió antes de que el usuario cerrara sesión, por ejemplo desde las herramientas del navegador, un proxy o un log, puede seguir usándolo hasta 30 minutos después. Pasa lo mismo si se le cambia el rol a un usuario: su token anterior conserva el rol viejo hasta que expira.

Para un sistema real propondría alguna de estas opciones:

1. Tokens de acceso de vida corta, de 5 a 15 minutos, acompañados de un refresh token guardado en una cookie HttpOnly y registrado en la base de datos. Al cerrar sesión se revoca el refresh token y el token de acceso deja de servir muy pronto.
2. Una lista de tokens revocados: al cerrar sesión se guarda el identificador del token (el claim jti) en una tabla o en Redis hasta su fecha de expiración, y el validador rechaza los tokens que estén en esa lista.
3. Una versión de token por usuario: un campo en la tabla de usuarios que viaja dentro del JWT y se incrementa al cerrar sesión en todos los dispositivos, al cambiar la contraseña o al cambiar el rol. Si la versión del token no coincide con la de la base de datos, se rechaza.

Cualquiera de estas opciones vuelve a guardar algo de estado en el servidor; ese es el precio de poder invalidar tokens antes de que venzan.

### 11. ¿Cuáles son las consecuencias de almacenar una llave de firma JWT dentro de GitHub?

Con HS256 la misma llave sirve para firmar y para verificar. Si la llave está en el repositorio, cualquiera que pueda verlo, o que vea el historial aunque el archivo se haya borrado después, puede:

- Fabricar tokens válidos para cualquier usuario, con cualquier identificador y con el rol ADMIN, y el backend los aceptaría como auténticos.
- Hacerse pasar por cualquier persona, leer, modificar y eliminar todas las tareas y entrar al resumen administrativo, sin pasar por el inicio de sesión ni por BCrypt.
- Hacerlo sin dejar señales de ataque, porque para el servidor serían tokens legítimos.

Borrar la llave en un commit posterior no la quita del historial de Git. Habría que cambiarla de inmediato, lo que invalida todos los tokens emitidos, y limpiar el historial.

Por eso en ToDo la llave no está en el código. La configuración la toma de la variable de entorno JWT_SECRET, sin ningún valor por defecto, y en local se define en un archivo backend/.env que Git ignora; en el repositorio solo existe un archivo de ejemplo con la variable vacía. Además, la aplicación no arranca si la llave tiene menos de 32 bytes, y las pruebas usan llaves propias que no sirven para la aplicación real.

### 12. ¿Qué pruebas del equipo demuestran la seguridad del CRUD y no solamente su funcionamiento correcto?

Una prueba de funcionamiento verifica que el camino normal sale bien, por ejemplo que crear una tarea devuelve 201. Una prueba de seguridad verifica que lo que no debería pasar no pasa, y que no deja efectos secundarios. En el proyecto, las pruebas que cumplen ese papel son:

- Las pruebas de integración, que levantan la aplicación completa con Spring Security, tokens reales y base de datos. Comprueban que sin token, con un token alterado o con uno vencido la respuesta es 401; que Luis, con su propio token válido, no puede consultar, modificar ni eliminar una tarea de Ana (404) y que la tarea queda intacta en la base de datos; que cada usuario solo recibe sus tareas; que una tarea nueva queda a nombre del usuario del token; que un USER recibe 403 y un ADMIN 200 en el resumen administrativo; que enviar el rol ADMIN en el registro no da privilegios, y que la contraseña se guarda como hash y no aparece en la respuesta.
- Las pruebas del servicio de tokens, que rechazan tokens alterados, vencidos, de otro emisor o firmados con otra llave, y llaves demasiado cortas.
- Las pruebas del servicio y del repositorio de tareas, que verifican el filtro por propietario y que una tarea ajena no se guarda ni se elimina.
- Las pruebas del frontend, que verifican que el token no queda en el almacenamiento del navegador, que al cerrar sesión desaparecen las tareas, que un 401 lleva al inicio de sesión y que el siguiente usuario no ve las tareas del anterior.

La matriz completa PT-01 a PT-18 está en el README. En total hay 89 pruebas en el backend y 34 en el frontend, con un 95,3 % de cobertura de instrucciones. Aun así, la cobertura por sí sola no demuestra seguridad: lo que la demuestra son estos casos de abuso probados de forma explícita.

## 5. Conclusiones

- Spring Security como servidor de recursos permitió validar tokens propios con poca configuración. Lo importante fue usar la misma llave para generarlos y validarlos, y comprobar también el emisor.
- La autenticación por sí sola no protege los datos. La regla más importante del laboratorio fue filtrar por propietario en el servicio y en el repositorio, tomando siempre la identidad del token.
- Responder 404 ante tareas ajenas evita revelar que existen registros de otros usuarios.
- Guardar el token en memoria es más seguro que usar localStorage, pero tiene un costo de usabilidad, porque la sesión se pierde al recargar. Además, cerrar sesión sin revocar el token deja una ventana de riesgo de hasta 30 minutos.
- La migración de la base de datos mostró que para volver obligatoria una columna en una tabla con datos hay que dar un paso intermedio: asignar primero un propietario a las tareas que ya existían.
