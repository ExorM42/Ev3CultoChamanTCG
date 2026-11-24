# Guía de presentación individual — Parcial 3

Guion sugerido de 8 a 10 minutos. Cada integrante debe ejecutar una parte y poder explicar el flujo completo. La pauta asigna 40% al encargo grupal y 60% a la presentación individual.

## 1. Modelo, persistencia y lógica (1.5 min)

Abre `backend/src/main/java/cl/cultochaman/api/product/Product.java`, `UserAccount.java`, sus repositorios y `SeedData.java`. Explica que JPA mapea cada entidad a H2, el id de producto es clave primaria y el correo es único. `SeedData` carga ocho cartas únicamente si el catálogo está vacío. Contraseñas se guardan con BCrypt; nunca se devuelve `passwordHash` en perfiles.

## 2. API REST y Swagger (1.5 min)

Abre `ProductController` y `AuthController`. En `http://localhost:8081/swagger-ui.html`, muestra lectura de productos, registro/login y CRUD protegido. Explica códigos 201, 204, 404 y conflicto de email/id. Inicia sesión como administrador y demuestra `POST`, `PUT` o `DELETE` sobre un producto; luego verifica la tabla H2.

## 3. Integración y flujo de datos (1.5 min)

En el navegador, explica `Catalog` → `GET /api/products` → controlador → repositorio JPA → H2 → JSON → React. Para una escritura: formulario del panel → `fetch` con `Authorization: Bearer <JWT>` → filtro JWT → autorización `ROLE_ADMIN` → JPA → respuesta → refresco del catálogo. Muestra `docs/INTEGRACION.md` y Swagger.

## 4. JWT y roles (1.5 min)

Explica `JwtService` y `JwtAuthenticationFilter`: token firmado con clave secreta, sujeto de email, claim de rol y expiración de una hora. `SecurityConfig` hace stateless la API. El registro solo crea `USER`; el administrador de demo se crea al inicio con variables configurables. Distingue autenticación (quién es) de autorización (qué puede hacer).

## 5. Sesión frontend (1 min)

En `src/services/auth.js`, muestra que login persiste token/perfil en `sessionStorage`, `restoreSession` valida `/auth/me` al recargar y logout elimina la sesión. El token no persiste al cerrar la pestaña. Indica el límite: sessionStorage es accesible al JavaScript de la página, así que producción debe mitigar XSS y valorar cookies HttpOnly/Secure.

## 6. Restricción de interfaz y servidor (1 min)

Muestra que `App.jsx` ofrece `/admin` y el enlace Administrar solo al rol ADMIN; `AdminProducts.jsx` agrega el token a cada mutación. Explica que esto mejora la experiencia, pero la barrera real es Spring Security: un usuario normal recibe 403 al llamar esas rutas directamente.

## Demostración recomendada

1. Arranca backend y frontend en terminales separadas.
2. Carga catálogo con API; filtra y agrega un producto.
3. Registra usuario y verifica que no se le da rol de administrador.
4. Inicia sesión de usuario normal, recarga para demostrar restauración y prueba `/admin`/API ADMIN denegada.
5. Inicia sesión como administrador, crea o edita carta en panel y comprueba Swagger y H2.
6. Cierra sesión y muestra que se elimina el token.

## Preparación de entrega

Captura el catálogo conectado, registro, sesión restaurada, CRUD admin, Swagger con una respuesta y registros de H2. Completa los nombres, sección y docente de los integrantes en el documento que use el equipo. Publicar el repositorio y cargar el material al aula son pasos externos a este workspace.
