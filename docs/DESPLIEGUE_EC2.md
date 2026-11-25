# Desplegar el backend en AWS EC2 (Git + Maven + nohup)

Sigue la guía del profesor (*Guia_EC2_SpringBoot_git_nohup*), adaptada a este proyecto:

| Dato | Valor en este proyecto |
| --- | --- |
| Java | 17 (Amazon Corretto) |
| Puerto del backend | **8081** |
| Carpeta del backend dentro del repo | `backend/` |
| JAR generado | `backend/target/cultochaman-api-0.0.1-SNAPSHOT.jar` |
| Base de datos | **H2 local** (archivo en `~/data/`), no hay que instalar nada |
| Endpoints | `/api/products`, `/api/auth/login`, `/api/orders`, Swagger en `/swagger-ui.html` |

## 1. Crear la instancia EC2

AWS → EC2 → **Launch instance**: nombre `ea3-microservicios`, AMI **Amazon Linux 2023**, tipo `t2.micro`, key pair **vockey**.

Reglas de entrada (inbound):

| Tipo | Puerto | Origen |
| --- | --- | --- |
| SSH | 22 | My IP |
| Custom TCP | 8081 | Anywhere (`0.0.0.0/0`) |
| HTTP (opcional) | 80 | Anywhere |

## 2. Elastic IP

EC2 → Elastic IPs → *Allocate* → *Associate* con tu instancia. La IP pública configurada para este proyecto es `100.52.124.23`.

## 3. Conectarte

- **Desde el navegador (más fácil):** EC2 → Instances → tu instancia → **Connect** → *EC2 Instance Connect* → **Connect**.
- **Por SSH desde CMD de Windows:** descarga el `.pem` en *AWS Details → Download PEM* y ejecuta:

```
cd %USERPROFILE%\Downloads
icacls labsuser.pem /inheritance:r
icacls labsuser.pem /grant:r "%USERNAME%:R"
ssh -i labsuser.pem ec2-user@100.52.124.23
```
(`chmod 400` no existe en Windows; los dos `icacls` hacen lo mismo. No escribas los `< >`.)

## 4. Instalar Java, Git y Maven

```bash
sudo dnf update -y
sudo dnf install git -y
sudo dnf install java-17-amazon-corretto -y
sudo dnf install maven -y
java -version
mvn -version
git --version
```

## 5. Clonar y compilar

```bash
cd ~
git clone https://github.com/tuusuario/CultoChamanTCGFullStack.git
cd CultoChamanTCGFullStack/backend
mvn clean package -DskipTests
ls target
```

Debes ver `cultochaman-api-0.0.1-SNAPSHOT.jar`. (Si tu repositorio contiene solo el backend, omite la carpeta intermedia y entra directo a `backend`.)

### Base de datos

No necesitas instalar nada: el backend usa **H2** y crea el archivo `~/data/cultochaman.mv.db` solo, con las tablas y las 8 cartas iniciales. Los datos se conservan al reiniciar la app y se pierden si terminas la instancia.

## 6. Levantar con nohup

Antes de lanzar el JAR, define las variables (cambia las claves; sin ellas se usan valores de desarrollo públicos):

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_EMAIL=admin@cultochaman.cl
export ADMIN_PASSWORD='Admin_2026!'
export FRONTEND_ORIGIN=http://100.52.124.23
export SPRING_H2_CONSOLE_ENABLED=false

cd ~/CultoChamanTCGFullStack/backend/target
nohup java -jar cultochaman-api-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

- `FRONTEND_ORIGIN` es el origen desde donde está abierto el frontend (varios, separados por coma). Para Postman no hace falta.
- Si cierras la sesión y vuelves, tienes que volver a exportar `JWT_SECRET`; con otro valor las sesiones anteriores dejan de valer.

Comprobar:

```bash
ps aux | grep java
tail -f ~/CultoChamanTCGFullStack/backend/target/app.log     # Ctrl+C para salir (no detiene la app)
```
Busca `Tomcat started on port 8081`.

El frontend publicado queda en `http://100.52.124.23` y la API en `http://100.52.124.23:8081/api`.

## 7. Probar la API (Postman o navegador)

| Método | URL | Body |
| --- | --- | --- |
| GET | `http://100.52.124.23:8081/api/products` | — |
| GET | `http://100.52.124.23:8081/api/products/pikachu-v` | — |
| POST | `http://100.52.124.23:8081/api/auth/login` | `{"email":"admin@cultochaman.cl","password":"Admin_2026!"}` |
| POST | `http://100.52.124.23:8081/api/admin/products` | JSON del producto, header `Authorization: Bearer <token>` |
| PUT | `http://100.52.124.23:8081/api/admin/products/{id}` | JSON, con token |
| DELETE | `http://100.52.124.23:8081/api/admin/products/{id}` | con token |
| POST | `http://100.52.124.23:8081/api/orders` | `{"items":[{"productId":"pikachu-v","quantity":1}]}` con token |

Swagger: `http://100.52.124.23:8081/swagger-ui.html` (o `/swagger-ui/index.html`). Haz login, copia el campo `token` y úsalo en **Authorize**.

## 8. Actualizar después de un cambio

```bash
cd ~/CultoChamanTCGFullStack
git pull
cd backend
mvn clean package -DskipTests
ps aux | grep java            # anota el PID
kill -9 <PID>
cd target
nohup java -jar cultochaman-api-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```
(Recuerda tener las variables `export` de la sección 6 en esa sesión.)

## 9. Comandos útiles

| Acción | Comando |
| --- | --- |
| Ver procesos Java | `ps aux \| grep java` |
| Últimas líneas del log | `tail -n 50 app.log` |
| Seguir el log | `tail -f app.log` |
| Matar un proceso | `kill -9 <PID>` |
| Liberar un puerto | `sudo lsof -i :8081` y luego `kill -9 <PID>` |

## 10. Cerrar el laboratorio

Al terminar: *Instance state* → **Stop** (conserva el disco y los datos) o **Terminate** (borra todo). Libera la Elastic IP si ya no la usas.

## Problemas frecuentes

| Síntoma | Qué revisar |
| --- | --- |
| `Connection timed out` en Postman | Security Group con el puerto **8081** abierto; Elastic IP correcta. |
| `Permission denied (publickey)` | Ruta del `.pem`, `icacls`, usuario `ec2-user`. |
| El JAR no aparece | `mvn clean package` falló; revisa el error de compilación. |
| `mvn` se cierra con `Killed` | La `t2.micro` se quedó sin RAM compilando: compila en tu PC y sube el JAR con `scp`. |
| Puerto en uso | Otro Java sigue vivo: `kill -9` del PID anterior. |
| La app muere al cerrar SSH | Faltó `nohup` o el `&` final. |
| Tras reiniciar, las sesiones no valen | Cambió el `JWT_SECRET`; vuelve a iniciar sesión. |

## Opcional (no requerido por la guía)

Los archivos de `deploy/` permiten usar MySQL (perfil `prod`), Nginx, un servicio `systemd` o Docker. Para MySQL: define `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL=jdbc:mysql://localhost:3306/cultochaman?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`, `DATABASE_USER` y `DATABASE_PASSWORD`. Con Nginx como proxy, edita `dist/config.js` para usar `"/api"`.
