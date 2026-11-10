# API de Gestión de Franquicias

API REST reactiva para gestionar una red de franquicias, sus sucursales y productos.

Construida para el reto técnico de Nequi con **Spring WebFlux** (solo endpoints funcionales), **arquitectura hexagonal / limpia** (estilo scaffold de Bancolombia), **Resilience4j**, **MongoDB**, **Docker** y **Terraform modular** desplegado sobre **AWS ECS Fargate**, simulado en local con **MiniStack**.

## Tabla de contenido

1. [Arquitectura](#arquitectura)
2. [Modelo de datos](#modelo-de-datos)
3. [Endpoints de la API](#endpoints-de-la-api)
4. [Ejecutar localmente desde cero](#ejecutar-localmente-desde-cero)
5. [Pruebas y cobertura](#pruebas-y-cobertura)
6. [Docker](#docker)
7. [Despliegue con Terraform en MiniStack (AWS simulado)](#despliegue-con-terraform-en-ministack-aws-simulado)
8. [Patrones de resiliencia](#patrones-de-resiliencia)
9. [Estructura del proyecto](#estructura-del-proyecto)

---

## Arquitectura

El proyecto sigue el scaffold de arquitectura limpia de Bancolombia, como un build multi-módulo de Gradle:

```
franchise-api
├── domain
│   ├── model        -> Entidades, catálogo de errores de negocio, puertos de salida (gateways)
│   └── usecase      -> Reglas de negocio (sin frameworks, solo Reactor)
├── infrastructure
│   ├── driven-adapters
│   │   └── mongo-repository -> Adaptador de MongoDB + decorador de resiliencia
│   └── entry-points
│       └── reactive-web     -> RouterFunction + Handlers + DTOs + filtro de errores
├── applications
│   └── app-service  -> Arranque de Spring Boot, configuración de beans y de la aplicación
├── deployment       -> Dockerfile (multi-stage)
└── infra            -> Terraform modular (network, ecr, alb, secrets, ecs)
```

Regla de dependencias: `infrastructure -> domain` y `applications -> todo`.
El dominio tiene **cero** dependencias de Spring/MongoDB: solo Project Reactor y Lombok.

Decisiones clave:

* **Solo endpoints funcionales**: `RouterFunctions` + clases handler. No existe ningún `@RestController`.
* **Totalmente reactivo y no bloqueante** de extremo a extremo: Netty -> WebFlux -> Reactor -> driver Reactive Streams de MongoDB. Sin `.block()`, sin `try-catch` en los flujos, sin `java.util.stream` y sin `if` dentro de cadenas reactivas (las condiciones usan `filter` / `switchIfEmpty`).
* **Errores como datos**: los casos de uso emiten `DomainException` con un `ErrorCode`; un único `GlobalErrorFilter` (un `HandlerFilterFunction`) los traduce a respuestas HTTP usando `onErrorResume`.
* **Lombok `@Builder(toBuilder = true)`** en las entidades inmutables del dominio; **Java records** para todos los DTOs de request/response.

## Modelo de datos

Tres colecciones de MongoDB referenciadas por id (sin agregados embebidos, lo que mantiene cada actualización en O(1) y los flujos totalmente reactivos):

* `franchises` — `{ id, name }`
* `branches` — `{ id, franchiseId, name }`
* `products` — `{ id, branchId, name, stock }`

## Endpoints de la API

Ruta base: `/api/v1`

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/franchises` | Crear una franquicia |
| PATCH | `/franchises/{franchiseId}/name` | Actualizar el nombre de una franquicia |
| POST | `/franchises/{franchiseId}/branches` | Agregar una sucursal a una franquicia |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/name` | Actualizar el nombre de una sucursal |
| POST | `/franchises/{franchiseId}/branches/{branchId}/products` | Agregar un producto a una sucursal |
| DELETE | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar un producto |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar el stock de un producto |
| PATCH | `/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar el nombre de un producto |
| GET | `/franchises/{franchiseId}/products/top-stock` | Producto con mayor stock por sucursal |

Documentación interactiva (Swagger UI): `http://localhost:8080/swagger-ui.html`
OpenAPI en JSON: `http://localhost:8080/v3/api-docs`

### Ejemplos de peticiones

```bash
# 1. Crear una franquicia
curl -X POST http://localhost:8080/api/v1/franchises \
  -H "Content-Type: application/json" \
  -d '{"name": "Nequi Stores"}'
# -> 201 {"id":"<franchiseId>","name":"Nequi Stores"}

# 2. Agregar una sucursal
curl -X POST http://localhost:8080/api/v1/franchises/<franchiseId>/branches \
  -H "Content-Type: application/json" \
  -d '{"name": "Medellin Branch"}'

# 3. Agregar un producto
curl -X POST http://localhost:8080/api/v1/franchises/<franchiseId>/branches/<branchId>/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Coffee Bag 500g", "stock": 100}'

# 4. Modificar el stock
curl -X PATCH http://localhost:8080/api/v1/franchises/<franchiseId>/branches/<branchId>/products/<productId>/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 42}'

# 5. Producto con mayor stock por sucursal
curl http://localhost:8080/api/v1/franchises/<franchiseId>/products/top-stock
# -> 200 [{"branchId":"...","branchName":"Medellin Branch","product":{"id":"...","name":"Coffee Bag 500g","stock":42}}]

# Ejemplo de error (la franquicia no existe)
curl -X PATCH http://localhost:8080/api/v1/franchises/missing/name \
  -H "Content-Type: application/json" -d '{"name": "X"}'
# -> 404 {"code":"FRANCHISE_NOT_FOUND","message":"Franchise not found","timestamp":"..."}
```

## Ejecutar localmente desde cero

Requisitos previos: **JDK 17+** y **Docker** (para MongoDB). No se requiere Gradle (se incluye el wrapper).

```bash
# 1. Clonar el repositorio y entrar al proyecto
git clone <repository-url>
cd franchise-api

# 2. Levantar MongoDB
docker compose up -d mongo

# 3. Ejecutar la aplicación (por defecto usa mongodb://localhost:27017/franchises)
./gradlew :app-service:bootRun      # Windows: gradlew.bat :app-service:bootRun

# 4. Verificar
curl http://localhost:8080/actuator/health
# -> {"status":"UP"}
```

Para usar MongoDB Atlas en lugar de un MongoDB local, define `MONGODB_URI` antes de arrancar:

```bash
export MONGODB_URI="mongodb+srv://<user>:<password>@<cluster>/franchises"
```

## Pruebas y cobertura

```bash
# Pruebas unitarias + de integración (StepVerifier / WebTestClient, sin .block() en ningún lugar)
./gradlew test

# Reportes de cobertura con JaCoCo (por módulo)
./gradlew jacocoTestReport
# Reportes HTML: <module>/build/reports/jacoco/test/html/index.html
```

Estrategia de pruebas:

* Módulo `usecase`: pruebas unitarias de escenarios de negocio con JUnit 5 + Mockito + **StepVerifier** (caminos felices, fallos de validación, recursos no encontrados, verificación de pertenencia entre franquicias).
* Módulo `reactive-web`: pruebas de integración de router + handler + filtro de errores con **WebTestClient** enlazado al `RouterFunction` real.
* Módulo `mongo-repository`: pruebas de los tres adaptadores con **StepVerifier** y pruebas de `MongoResilience` con registros reales de Resilience4j (reintento con backoff, timeout sin reintento y apertura del circuit breaker).

## Docker

```bash
# Construir la imagen (multi-stage: compila con gradle dentro, runtime solo con JRE, usuario no root)
docker build -f deployment/Dockerfile -t franchise-api .

# Stack local completo (API + MongoDB)
docker compose up --build
```

## Despliegue con Terraform en MiniStack (AWS simulado)

El despliegue no usa una cuenta de AWS real. Usa [MiniStack](https://ministack.org/), un emulador local de servicios de AWS que corre en un solo contenedor (puerto `4566`). Los archivos de Terraform en `infra/` son los mismos que usaríamos en AWS: solo cambian el endpoint y las credenciales.

Arquitectura desplegada:

```
localhost:4566 (MiniStack)
  ALB (enruta por el header Host) -> Servicio ECS (tareas = contenedores del daemon Docker del host)
                                       |-- imagen localhost:4566/franchise-api-dev:latest
                                       |-- MONGODB_URI inyectada desde Secrets Manager
                                       '--> MongoDB (Atlas o el contenedor local)
```

### Requisitos

* Docker Desktop en ejecución.
* [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.6.
* [AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html), usada solo contra el endpoint de MiniStack.

### 1. Levantar MiniStack

```bash
docker compose up -d ministack
curl http://localhost:4566/_ministack/health
```

MiniStack necesita el socket de Docker montado porque ejecuta las tareas de ECS como contenedores reales.

### 2. Configurar credenciales ficticias

MiniStack no valida credenciales, así que cualquier valor sirve:

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
```

PowerShell:

```powershell
$env:AWS_ACCESS_KEY_ID="test"; $env:AWS_SECRET_ACCESS_KEY="test"; $env:AWS_DEFAULT_REGION="us-east-1"
```

### 3. Crear el estado remoto (una sola vez)

Crea el bucket de S3 y la tabla de bloqueo de DynamoDB referenciados en `infra/backend.tf`:

```bash
aws --endpoint-url http://localhost:4566 s3api create-bucket --bucket franchise-api-terraform-state
aws --endpoint-url http://localhost:4566 s3api put-bucket-versioning --bucket franchise-api-terraform-state \
  --versioning-configuration Status=Enabled
aws --endpoint-url http://localhost:4566 dynamodb create-table --table-name franchise-api-terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

### 4. Desplegar el entorno

```bash
cd infra
terraform init

# Los workspaces aíslan el estado por entorno (dev / staging / prod)
terraform workspace new dev        # o: terraform workspace select dev

# La URI de MongoDB nunca se versiona: pásala mediante una variable de entorno
export TF_VAR_mongodb_uri="mongodb+srv://<user>:<password>@<cluster>/franchises"

terraform plan  -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars
```

Para probar sin Atlas puedes usar el MongoDB del compose: `TF_VAR_mongodb_uri="mongodb://host.docker.internal:27017/franchises"` (con `docker compose up -d mongo`).

### 5. Construir y publicar la imagen

```bash
docker build -f deployment/Dockerfile -t franchise-api:local .
docker tag franchise-api:local localhost:4566/franchise-api-dev:latest
aws --endpoint-url http://localhost:4566 ecr get-login-password | docker login --username AWS --password-stdin localhost:4566
docker push localhost:4566/franchise-api-dev:latest

# Forzar un nuevo despliegue para que el servicio tome la imagen
aws --endpoint-url http://localhost:4566 ecs update-service \
  --cluster $(terraform output -raw ecs_cluster_name) \
  --service franchise-api-dev-service --force-new-deployment
```

Las tareas de ECS se arrancan solo si la imagen existe en el daemon Docker del host. Por eso `environments/dev.tfvars` define `container_image_override = "localhost:4566/franchise-api-dev:latest"` en lugar de la URI de ECR que devuelve `ecr_repository_url`. Si cambias el tag, actualiza ese valor.

#### Alternativa si no puedes descargar la imagen base

El `Dockerfile` usa `eclipse-temurin:17-jre-alpine` de Docker Hub. Si la descarga falla (por ejemplo, por una red que bloquea Docker Hub), compila el JAR en el host y empaquétalo sobre una imagen con JDK 17 que ya tengas local:

```bash
# 1. Compilar el JAR (requiere JDK 17 instalado en el host)
./gradlew :app-service:bootJar

# 2. Ver qué imágenes con JDK 17 ya tienes local
docker images | grep -E "temurin|jdk17|openjdk"

# 3. En una carpeta temporal FUERA del repositorio, crear un Dockerfile mínimo
mkdir -p /tmp/franchise-image && cd /tmp/franchise-image
cp <ruta-del-repo>/applications/app-service/build/libs/franchise-api.jar .
cat > Dockerfile <<'EOF'
FROM gradle:8.7-jdk17-alpine
WORKDIR /app
COPY franchise-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
EOF

# 4. Construir la imagen y continuar con el mismo flujo de este paso
docker build -t franchise-api:local .
```

Reemplaza `gradle:8.7-jdk17-alpine` por la imagen con JDK 17 que aparezca en el paso 2. Esta alternativa omite el usuario no root del `Dockerfile` del repositorio, así que es solo para ambientes de prueba.


### 6. Verificar

```bash
ALB=$(terraform output -raw alb_dns_name)
curl -H "Host: $ALB" http://localhost:4566/actuator/health
curl -H "Host: $ALB" -H "Content-Type: application/json" -d '{"name":"Franquicia demo"}' http://localhost:4566/api/v1/franchises
```

El health debe responder `{"status":"UP",...}` y el POST `201`. Los logs de la aplicación se consultan con `docker logs <id-del-contenedor>`, porque MiniStack no reenvía `awslogs` a CloudWatch.

### 7. Limpiar

```bash
terraform destroy -var-file=environments/dev.tfvars
docker compose down ministack
```

Para borrar todo el estado de MiniStack de una vez: `curl -X POST http://localhost:4566/_ministack/reset` (después hay que repetir el paso 3).

### Limitaciones de MiniStack

* **Auto Scaling desactivado en MiniStack (limitación).** MiniStack no implementa Application Auto Scaling para ECS, así que `dev.tfvars` define `enable_autoscaling = false`. Los entornos `staging` y `prod` están configurados para AWS real con `enable_autoscaling = true` (target y política de CPU al 60 %). Por eso **no se pueden aplicar en MiniStack**: el `apply` fallaría al crear el target. Su `plan` sí se puede generar para validar la configuración.
* **Los logs de `awslogs` no llegan a CloudWatch.** Se leen con `docker logs`.
* **El balanceo es simulado**: el target group reporta siempre sus objetivos como saludables y el ALB se enruta por el header `Host`.
* **Las tareas son contenedores del host**, no instancias aisladas en una VPC real, y los roles de IAM no se aplican.
* **Es un entorno de prueba**, no reemplaza una validación en una cuenta real de AWS antes de producción.
* Si MiniStack se reinicia sin un volumen de persistencia, el estado de Terraform puede perderse; en ese caso repite los pasos 3 y 4.

### Notas de diseño de IaC

* **Modular**: módulos `network`, `ecr`, `alb`, `secrets` y `ecs`, cada uno con sus propios `variables.tf` / `outputs.tf`. El `main.tf` raíz solo conecta los módulos entre sí.
* **Multi-entorno**: **workspaces** de Terraform + archivos `environments/*.tfvars` por entorno (distintos tamaños y número de réplicas por entorno). Los nombres de los recursos llevan el prefijo `franchise-api-<workspace>`.
* **Estado remoto**: backend en S3 con bloqueo en DynamoDB. En MiniStack ambos son simulados.
* **Credenciales**: ningún archivo del repositorio contiene credenciales. Terraform y AWS CLI las leen de `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`. En MiniStack cualquier valor sirve (`test`). En AWS real se usan un perfil o un rol.
* **IAM con mínimo privilegio**: el rol de ejecución puede leer **solo** el secreto de MongoDB y escribir **solo** en su propio grupo de logs; el rol de la tarea no tiene permisos.
* **Secretos**: la URI de MongoDB vive en Secrets Manager; ECS la inyecta al iniciar la tarea. Nunca aparece en texto plano en el código, en los archivos tfvars ni en la definición de la tarea. Sin embargo, el valor sí queda en el `terraform.tfstate`, así que en AWS real el estado debe estar cifrado y con acceso restringido.

## Patrones de resiliencia

Todas las operaciones sobre MongoDB pasan por `MongoResilience`, que aplica el pipeline completo (configurado en `application.yaml`):

| Patrón | Implementación | Configuración |
|--------|----------------|---------------|
| Timeout | `TimeLimiterOperator` de Resilience4j | 3 s por operación |
| Retry | `RetryOperator` de Resilience4j | 3 intentos, backoff exponencial (200 ms x2), solo ante fallos transitorios de conexión (`MongoSocketException`, `TransientDataAccessException`). Un timeout **no** se reintenta, para que el tiempo máximo de respuesta siga siendo el del TimeLimiter |
| Circuit Breaker | `CircuitBreakerOperator` de Resilience4j | se abre con 50 % de fallos sobre 10 llamadas, pasa a half-open tras 10 s |
| Backpressure | Nativo en WebFlux/Reactor | Señalización de demanda `request(n)` de Reactive Streams de extremo a extremo |

Cuando el circuito está abierto la API responde `503 CIRCUIT_OPEN`; ante un timeout responde `504 TIMEOUT`. Ambas respuestas las produce de forma reactiva `GlobalErrorFilter`.

## Estructura del proyecto

```
domain/model/..../model/
├── franchise/Franchise.java + gateways/FranchiseRepository.java
├── branch/Branch.java + gateways/BranchRepository.java
├── product/Product.java, TopStockProduct.java + gateways/ProductRepository.java
├── exception/ErrorCode.java, ErrorType.java, DomainException.java
└── validation/DomainValidator.java

domain/usecase/..../usecase/
├── franchise/FranchiseUseCase.java
├── branch/BranchUseCase.java
└── product/ProductUseCase.java

infrastructure/driven-adapters/mongo-repository/..../mongo/
├── document/   FranchiseDocument, BranchDocument, ProductDocument
├── repository/ Repositorios reactivos de Spring Data
├── mapper/     MongoMapper (documento <-> dominio)
├── resilience/ MongoResilience (timeout + retry + circuit breaker)
└── adapter/    Implementaciones de los puertos

infrastructure/entry-points/reactive-web/..../api/
├── RouterRest.java          (RouterFunction + anotaciones de OpenAPI)
├── handler/                 FranchiseHandler, BranchHandler, ProductHandler
├── dto/                     Java records (requests/responses)
├── mapper/ApiMapper.java
└── error/GlobalErrorFilter.java

applications/app-service/..../
├── MainApplication.java
└── config/ UseCasesConfig.java, OpenApiConfig.java
```
