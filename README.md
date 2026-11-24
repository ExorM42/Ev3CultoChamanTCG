# Culto Chamán TCG Store — Evaluación Parcial 3

Tienda React conectada a una API REST Spring Boot. El catálogo se persiste en H2/JPA; registro e inicio de sesión usan BCrypt y JWT con roles `USER` y `ADMIN`. El catálogo es público y las mutaciones quedan protegidas para administración.

## Requisitos

- Node.js 20+ y npm para el frontend.
- Java 17 y Maven 3.9+ para el backend.

## Ejecutar localmente

Terminal 1, backend:

```sh
cd backend
mvn spring-boot:run
```

Terminal 2, frontend desde la raíz:

```sh
npm ci
npm run dev
```

Abre `http://127.0.0.1:5173`. Swagger UI: `http://localhost:8081/swagger-ui.html`; OpenAPI JSON: `http://localhost:8081/v3/api-docs`. H2 Console: `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/cultochaman`, usuario `sa`, contraseña vacía en desarrollo).

La base se crea en `backend/data/` al iniciar. En el primer arranque se cargan las ocho cartas del frontend y un administrador de demostración. Credenciales iniciales locales: `admin@cultochaman.cl` / `Admin123!`. Cámbialas con `ADMIN_EMAIL` y `ADMIN_PASSWORD`; antes de desplegar, configura también un `JWT_SECRET` aleatorio de al menos 32 bytes, `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` y `FRONTEND_ORIGIN`. El perfil H2 y la consola están habilitados para la demostración local, no como configuración de producción.

## Funcionalidades

- Catálogo filtrable que consulta `GET /api/products`; conserva un catálogo de demostración si la API no responde.
- Registro persistido en base de datos, contraseñas con BCrypt e inicio de sesión con JWT de una hora.
- Sesión en `sessionStorage`, validada en cada carga con `GET /api/auth/me`; cerrar sesión elimina token y perfil.
- Panel `/admin` y enlaces de administración visibles solo para `ADMIN`; el servidor además valida el rol en cada operación.
- CRUD de cartas mediante Spring Data JPA y Swagger UI.
- Carrito, precios CLP, filtros y cupón existentes.

## Estructura

- `src/`: SPA React, almacenamiento del carrito, sesión y cliente HTTP.
- `backend/src/main/java/cl/cultochaman/api/`: controladores, entidades JPA, repositorios, JWT, seguridad y carga inicial.
- `docs/INTEGRACION.md`: endpoints, arquitectura, modelo y flujo frontend/backend.
- `docs/MANUAL_USUARIO.md`: guía operativa y capturas de referencia.
- `docs/GUIA_PRESENTACION.md`: pauta para preparar una defensa individual.
- `docs/ERS.md`: trazabilidad de requisitos de la evaluación.

La versión anterior guardaba cuentas localmente. Ya no se consultan ni se migran; los usuarios deben registrarse nuevamente en la API.

Pruebas de integración backend: `cd backend && mvn test`. `ApiIntegrationTest` ejecuta solicitudes MockMvc contra la aplicación y H2 en memoria, incluyendo registro, emisión/restauración JWT, restricción del rol USER y ciclo CRUD ADMIN. No se ejecutaron en este entorno porque Maven no está instalado.

## Despliegue

Perfil `prod` con MySQL y guía paso a paso para AWS EC2 (incluye RDS, Nginx, systemd y HTTPS) en `docs/DESPLIEGUE_EC2.md`; archivos en `deploy/`.
