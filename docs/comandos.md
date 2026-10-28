# como-crear-un-repositorio

git clone repo.git

cd repo

git config --global user.name "nataliaRT12"

git config --global user.email "nart6006812@gmail.com"


code .

crear .gitignore

target/
*.class
*.iml
.idea/
.vscode/
*.log
.DS_Store

POM

mvn -B archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DgroupId=com.ejemplo.app -DartifactId=demo-maven -DinteractiveMode=false

si falla:

mvn archetype:generate "-DarchetypeGroupId=org.apache.maven.archetypes" "-DarchetypeArtifactId=maven-archetype-quickstart" "-DarchetypeVersion=1.4" "-DgroupId=edu.eci.dosw" "-DartifactId=demo-maven" "-DinteractiveMode=false" "-B"

#verificar
dir

Move-Item demo-maven\* .

Remove-Item demo-maven -Recurse

dir

cambiar contenido del pom:

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>edu.eci.dosw</groupId>
  <artifactId>demo-maven</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>demo-maven</name>
  <url>http://www.example.com</url>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.2</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-clean-plugin</artifactId>
        <version>3.1.0</version>
      </plugin>
      <plugin>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.0.2</version>
      </plugin>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.0</version>
      </plugin>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.0.2</version>
      </plugin>
      <plugin>
        <artifactId>maven-install-plugin</artifactId>
        <version>2.5.2</version>
      </plugin>
      <plugin>
        <artifactId>maven-deploy-plugin</artifactId>
        <version>2.8.2</version>
      </plugin>
      <plugin>
        <artifactId>maven-site-plugin</artifactId>
        <version>3.7.1</version>
      </plugin>
      <plugin>
        <artifactId>maven-project-info-reports-plugin</artifactId>
        <version>3.0.0</version>
      </plugin>
    </plugins>
  </build>
</project>

mvn clean compile

#por si necesito crear carpetas

mkdir diagrams
mkdir images
mkdir requerimientos
New-Item diagrams\.gitkeep -ItemType File
New-Item images\.gitkeep -ItemType File
New-Item requerimientos\.gitkeep -ItemType File

#estado
git status

git add .
git commit -m "feat: texto"
git push

commits estructura:

Prefix	Purpose
feat:	New functionality
fix:	Bug fix
docs:	Documentation changes
refactor:	Code restructuring
test:	Test-related changes
chore:	Configuration or setup
merge:	Merge-related commit

#crear develop

git checkout -b develop
git push -u origin develop

#crear ramas y subir cambios

git checkout develop
git checkout -b feature/texto

git add .
git commit -m "sfheue"
git push -u origin feature/texto

git checkout develop
git merge feature/texto
git push

#actualizar rama (develop)
git checkout develop
git pull origin develop

#Ver en qué rama estoy parada actualmente
git branch

#Ver estado del repo (cambios sin commitear, archivos nuevos, etc.)
git status

# ramas

#Ver todas las ramas locales
git branch

#Ver todas las ramas, incluyendo las remotas
git branch -a

#Crear una rama nueva (sin moverte a ella)
git branch nombre-rama

#Crear una rama nueva Y moverte a ella al mismo tiempo
git checkout -b nombre-rama

#Moverme a una rama que YA existe
git checkout nombre-rama

#Comando moderno equivalente a checkout (Git reciente)
git switch nombre-rama
git switch -c nombre-rama-nueva

#Renombrar la rama actual
git branch -m nuevo-nombre

#Eliminar una rama local (solo si ya fue mezclada)
git branch -d nombre-rama

#Eliminar una rama local a la fuerza (aunque no esté mezclada)
git branch -D nombre-rama

#Eliminar una rama remota
git push origin --delete nombre-rama

# merge

#1. Moverme a la rama destino (ej. develop)
git checkout develop

#2. Traer los últimos cambios de develop antes de fusionar
git pull

#3. Fusionar la rama de feature dentro de develop
git merge feature/nombre-rama

#Si hay conflictos, Git te avisa qué archivos los tienen.
#Los abres, resuelves manualmente las marcas <<<<<<< ======= >>>>>>>,
#guardas, y luego:
git add .
git commit -m "merge: resolviendo conflictos de feature/nombre-rama"

# guardar cambios

#Agregar un archivo específico al área de preparación (staging)
git add nombre-archivo.txt

#Agregar TODOS los cambios (nuevos, modificados, eliminados)
git add .

#Confirmar los cambios con un mensaje
git commit -m "feat: descripción del cambio"

#Agregar y confirmar en un solo paso (solo para archivos YA trackeados, no nuevos)
git commit -am "feat: descripción del cambio"

#Ver qué cambió exactamente en los archivos (antes de hacer add)
git diff

#Ver qué está en el área de preparación (después de add, antes de commit)
git diff --staged

# subir los cambios

#Subir cambios de la rama actual (si ya tiene upstream configurado)
git push

#Subir una rama nueva por primera vez y vincularla con el remoto
git push -u origin nombre-rama

#Traer y fusionar cambios de la rama remota
git pull

#Traer cambios remotos sin fusionarlos todavía (para revisar antes)
git fetch

#Traer todo (todas las ramas remotas)
git fetch --all



# general

#Ver en qué carpeta estoy (ruta actual)
pwd

#Ver historial de commits (resumido, con gráfico de ramas)
git log --oneline --graph --all

#Ver historial completo con detalles
git log

#Subir un nivel
cd ..

#Subir dos niveles
cd ..\..

#Ir directo a una ruta específica (sin importar dónde estés)
cd C:\Users\nart6\repo-prueba

#Ir a la carpeta de usuario (home)
cd ~

---------------------

# comandos-todo-fullstack
cd DOSW_LAB6_Rodriguez

#abrir en vscode

code .


# 0. antes de empezar

#abrir Docker Desktop y esperar a que diga Running
docker info

#versiones (java debe ser 21, el pom usa 21)
java -version

mvn -v

node -v

npm -v

docker --version

#ver si los puertos 8080, 5173 y 5432 estan ocupados
Get-NetTCPConnection -LocalPort 8080,5173,5432 -ErrorAction SilentlyContinue

#matar un proceso que use un puerto (cambiar 1234 por el PID)
Stop-Process -Id 1234 -Force

#instalar dependencias del front (solo la primera vez)
cd frontend

npm install

cd ..

#usar 4 terminales: T1 docker y sql / T2 backend / T3 frontend / T4 pruebas y curl
#en cada terminal nueva
cd C:\Users\nart6\DOSW_LAB6_Rodriguez


# 1. clonar el repo (solo si no esta en el pc)

git clone https://github.com/nataliaRT12/DOSW_LAB6_Rodriguez.git

cd DOSW_LAB6_Rodriguez

#revisar autor de los commits
git config user.name

git config user.email

#si hace falta configurarlos
git config --global user.name "nataliaRT12"

git config --global user.email "nart6006812@gmail.com"


# 2. postgresql en docker (T1)

#--- primera vez (si el contenedor NO existe) ---

#descargar imagen oficial
docker pull postgres:17-alpine

#crear volumen
docker volume create todo-postgres-data

#crear contenedor
docker run -d --name todo-postgres -e POSTGRES_DB=todo_db -e POSTGRES_USER=todo_user -e POSTGRES_PASSWORD=todo_password -p 5432:5432 -v todo-postgres-data:/var/lib/postgresql/data postgres:17-alpine

#copiar el script al contenedor
docker cp database\001_create_schema.sql todo-postgres:/001_create_schema.sql

#crear la tabla tasks
docker exec -i todo-postgres psql -U todo_user -d todo_db -f /001_create_schema.sql

#--- si el contenedor YA existe ---

#iniciar
docker start todo-postgres

#--- mostrar en el video ---

#imagen descargada
docker images

#contenedor corriendo
docker ps

#entrar a postgres
docker exec -it todo-postgres psql -U todo_user -d todo_db

#dentro de psql: ver tablas
\dt

#ver columnas de tasks
\d tasks

#ver las tareas
SELECT * FROM tasks;

#salir de psql
\q

#consulta en una sola linea (repetir despues de cada operacion en react)
docker exec -it todo-postgres psql -U todo_user -d todo_db -c "SELECT id, title, status, priority, due_date, created_at FROM tasks ORDER BY id;"

#ejecutar un script sql nuevo (ej. 002_nuevo.sql)
docker cp database\002_nuevo.sql todo-postgres:/002_nuevo.sql

docker exec -i todo-postgres psql -U todo_user -d todo_db -f /002_nuevo.sql

#dejar la tabla vacia
docker exec -it todo-postgres psql -U todo_user -d todo_db -c "TRUNCATE TABLE tasks RESTART IDENTITY;"


# 3. backend spring boot (T2)

cd backend

#ejecutar (http://localhost:8080)
mvn spring-boot:run

#si no hay maven instalado
.\mvnw.cmd spring-boot:run

#detener: Ctrl + C

#probar que responde (en T4)
curl.exe -i http://localhost:8080/api/v1/tasks


# 4. pruebas del backend (T4)

#detener el backend (Ctrl + C en T2) antes de usar clean, en windows puede fallar al borrar target/
cd backend

#pruebas
mvn clean test

#pruebas + reporte jacoco
mvn clean verify

#abrir reporte de cobertura
Start-Process target\site\jacoco\index.html

#una sola clase
mvn test -Dtest=TaskServiceTest

mvn test -Dtest=TaskControllerTest

#resultado esperado: BUILD SUCCESS

#volver a levantar el backend (T2)
mvn spring-boot:run


# 5. frontend react (T3)

cd frontend

#ejecutar (http://localhost:5173)
npm run dev

#abrir en el navegador
Start-Process http://localhost:5173

#detener: Ctrl + C


# 6. pruebas del frontend (T4)

cd frontend

npm test

#resultado esperado: 3 test files, 13 tests passed

#modo watch
npx vitest


# 7. crud desde react 

#todo se hace desde la interfaz, no con postman
#1 crear tarea (title, description, priority, due date) -> Save
#2 crear otra y ver la lista
#3 Edit -> cambiar datos -> Save
#4 Change status (Pending -> In progress -> Completed)
#5 Delete
#6 crear con title vacio -> mensaje de error
#7 F5 en el navegador -> las tareas siguen (vienen de la BD)

#despues de CADA paso consultar la BD (T1)
docker exec -it todo-postgres psql -U todo_user -d todo_db -c "SELECT id, title, status, priority, due_date FROM tasks ORDER BY id;"


# 8. probar la api con curl.exe (opcional)

#en powershell "curl" es otro comando, usar curl.exe y guardar el json en un archivo

#listar (200)
curl.exe -i http://localhost:8080/api/v1/tasks

#consultar una (200) cambiar el 1 por un id real
curl.exe -i http://localhost:8080/api/v1/tasks/1

#crear (201)
'{"title":"Tarea por API","description":"curl","priority":"HIGH","dueDate":"2026-10-30"}' | Set-Content -Encoding utf8 body.json

curl.exe -i -X POST http://localhost:8080/api/v1/tasks -H "Content-Type: application/json" --data-binary "@body.json"

#actualizar (200)
'{"title":"Editada","description":"ok","status":"IN_PROGRESS","priority":"LOW","dueDate":"2026-11-05"}' | Set-Content -Encoding utf8 body.json

curl.exe -i -X PUT http://localhost:8080/api/v1/tasks/1 -H "Content-Type: application/json" --data-binary "@body.json"

#eliminar (204)
curl.exe -i -X DELETE http://localhost:8080/api/v1/tasks/1

#no existe (404)
curl.exe -i http://localhost:8080/api/v1/tasks/99999

#datos invalidos (400)
'{"title":""}' | Set-Content -Encoding utf8 body.json

curl.exe -i -X POST http://localhost:8080/api/v1/tasks -H "Content-Type: application/json" --data-binary "@body.json"

#id invalido (400)
curl.exe -i http://localhost:8080/api/v1/tasks/abc

#borrar el archivo temporal
Remove-Item body.json

#version powershell (devuelve objetos)
Invoke-RestMethod http://localhost:8080/api/v1/tasks | Format-Table id, title, status, priority, dueDate


# 9. entrega inicial 

#revisar remoto 
git remote -v

#estado
git status

#copiar README de apoyo a la raiz (ajustar ruta de descargas)
Copy-Item $HOME\Downloads\README.md .\README.md -Force

#carpeta docs y diagramas C4
mkdir docs -ErrorAction SilentlyContinue

Copy-Item $HOME\Downloads\arquitectura-c4.md docs\ -Force

Copy-Item $HOME\Downloads\0*-*.png docs\ -Force

Copy-Item $HOME\Downloads\0*-*.svg docs\ -Force

#probar todo antes de subir
cd backend

mvn clean verify

cd ..\frontend

npm test

cd ..

#subir
git add .

git commit -m "docs: README de apoyo y diagramas C4 para el parcial"

git push



#o en el navegador (capturar con fecha y hora visibles)
Start-Process https://github.com/nataliaRT12/DOSW_LAB6_Rodriguez/commits/main

#repo privado + docente: GitHub > Settings > General > Change visibility > Private
#GitHub > Settings > Collaborators > Add people > rodrigogualtero92@gmail.com

#verificar visibilidad (si esta gh instalado)
gh repo view nataliaRT12/DOSW_LAB6_Rodriguez --json visibility,url

#invitaciones pendientes
gh api repos/nataliaRT12/DOSW_LAB6_Rodriguez/invitations --jq ".[] | .invitee.login"

#entregar en Teams: enlace del repo + captura de commits
#no se aceptan commits de preparacion despues de las 10:00


# 10. durante el parcial (desde las 10:10)

#traer cambios
git pull

#estado
git status

#orden: sql -> entity -> repository -> service -> controller -> taskApi.js -> useTasks -> componente

#si hay columna o tabla nueva, crear y ejecutar el sql ANTES de arrancar la api
docker cp database\002_nuevo.sql todo-postgres:/002_nuevo.sql

docker exec -i todo-postgres psql -U todo_user -d todo_db -f /002_nuevo.sql

docker exec -it todo-postgres psql -U todo_user -d todo_db -c "\d tasks"

#probar cada capa
cd backend

mvn clean test

cd ..\frontend

npm test

cd ..

#probar endpoint nuevo
curl.exe -i "http://localhost:8080/api/v1/tasks?status=PENDING&priority=HIGH"

curl.exe -i -X PATCH "http://localhost:8080/api/v1/tasks/1/status?status=COMPLETED"

#commits pequenos y frecuentes
git add .

git commit -m "feat: descripcion de lo implementado"

git push

#ver commits
git log --oneline --graph --all


# 11. git (resumen)

commits estructura:

Prefix	Purpose
feat:	New functionality
fix:	Bug fix
docs:	Documentation changes
refactor:	Code restructuring
test:	Test-related changes
chore:	Configuration or setup
merge:	Merge-related commit

#ver rama actual
git branch

#crear rama y moverse
git checkout -b feature/texto

#subir rama nueva
git push -u origin feature/texto

#volver a main y fusionar
git checkout main

git pull

git merge feature/texto

git push

#ver que cambio antes de add
git diff

#ver que esta en staging
git diff --staged


# 12. problemas comunes

#docker: error during connect -> abrir Docker Desktop

#back: Connection refused / Unable to determine Dialect -> contenedor apagado
docker start todo-postgres

#back: Schema-validation missing table/column -> falta ejecutar el sql (ver seccion 2)

#puerto 8080 ocupado
Get-NetTCPConnection -LocalPort 8080

Stop-Process -Id 1234 -Force

#puerto 5432 ocupado -> usar otro puerto en docker run (-p 5433:5432) y arrancar la api con
$env:DB_URL="jdbc:postgresql://localhost:5433/todo_db"

#java incorrecto (release version 21 not supported) -> instalar JDK 21 o poner java.version en 17 en backend\pom.xml

#mvn no se reconoce
.\mvnw.cmd -v

#npm: running scripts is disabled
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

#front: Failed to fetch -> backend apagado o abriste 127.0.0.1 en vez de localhost:5173

#mvn clean falla borrando target -> detener el backend (Ctrl + C) y reintentar

#detener y limpiar todo al terminar
docker stop todo-postgres

#borrar contenedor y datos (reinicio total, volver a seccion 2)
docker rm -f todo-postgres

docker volume rm todo-postgres-data
