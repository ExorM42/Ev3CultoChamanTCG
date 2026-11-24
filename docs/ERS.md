# Especificación de requisitos — Evaluación Parcial 3

Proyecto: Culto Chamán TCG Store. Asignatura DSY1104, Desarrollo Fullstack II. La pauta PDF distingue los criterios técnicos del encargo grupal (40%) y la presentación individual (60%). Este documento describe la solución local y sus límites; debe completarse con autoría, sección y docente antes de entregar.

## Requisitos y evidencia

| Indicador | Requisito | Evidencia implementada |
| --- | --- | --- |
| IE3.1.1 | Backend conectado a base de datos, modelo y lógica de negocio. | Entidades `Product`/`UserAccount`, repositorios JPA, H2 y `SeedData`; email único y roles asignados por backend. |
| IE3.2.1 | API Spring Boot CRUD documentada en Swagger. | CRUD de productos y consulta/eliminación de usuarios ADMIN; Swagger UI y OpenAPI. |
| IE3.2.2 | Integración de frontend y backend mediante API REST. | Cliente `fetch` para catálogo, registro, login, perfil y mutaciones admin. |
| IE3.3.1 | Autenticación backend con roles y JWT. | BCrypt, emisión/validación JWT, filtro stateless y roles `USER`/`ADMIN`. |
| IE3.3.2 | Sesión de frontend persistente en recarga. | JWT y perfil en `sessionStorage`, restauración consultando `/auth/me`, logout elimina token. |
| IE3.3.3 | Restringir interfaces y acciones frontend. | Ruta y navegación `/admin` según rol; además el servidor aplica control de acceso a endpoints. |
| IE3.1.2–IE3.3.6 | Explicar individualmente modelo, CRUD, integración, auth, sesión y restricciones. | `docs/GUIA_PRESENTACION.md`; defensa y capturas actuales deben ser realizadas por cada estudiante/equipo. |

## Arquitectura y reglas

React sirve interfaz y envía JSON a Spring Boot. Spring Security filtra bearer JWT antes de controladores; JPA accede a H2. Catálogo de lectura público. Escrituras de producto y gestión de cuentas solo ADMIN. Registro no acepta rol del cliente y crea siempre `USER`. Precios provienen del catálogo en servidor. Email se normaliza a minúsculas y se restringe a único. BCrypt evita guardar contraseñas legibles.

## Criterios de operación

- API local por defecto: `http://localhost:8081`; frontend: `http://127.0.0.1:5173`.
- Backend requiere Java 17 y Maven; frontend Node 20+ y npm.
- Datos demo persistentes en H2; cuenta inicial configurable mediante entorno.
- Token JWT expira en 60 minutos; al expirar el frontend elimina sesión al validar `/api/auth/me`.
- El panel requiere rol `ADMIN` y cada operación se valida nuevamente en servidor.
- Las compras siguen como simulación frontend; no se creó persistencia de pedidos, stock ni pagos.

## Trazabilidad de entrega

La rúbrica solicita enlace público GitHub, frontend comprimido, backend comprimido, manual con pantallazos y documento de integración. El repositorio contiene ambos proyectos y documentos (`MANUAL_USUARIO.md`, `INTEGRACION.md`). La captura nueva conectada al backend, los comprimidos, publicación GitHub y carga al aula requieren ser generados/subidos por el equipo al preparar su entrega final.
