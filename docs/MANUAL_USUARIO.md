# Manual de usuario

## Iniciar la aplicación

1. Instala Java 17, Maven 3.9+ y Node.js 20+.
2. En una terminal abre `backend/` y ejecuta `mvn spring-boot:run`.
3. En otra terminal, desde la raíz, ejecuta `npm ci` y `npm run dev`.
4. Abre `http://127.0.0.1:5173`. La primera carga crea el catálogo de demostración en H2.

## Crear cuenta e iniciar sesión

Desde **Crear cuenta**, ingresa nombre, correo, contraseña y región/comuna. La contraseña debe tener entre 8 y 128 caracteres, mayúscula, minúscula, número y símbolo. La cuenta queda guardada en el servidor. Luego inicia sesión con el mismo correo y contraseña. El encabezado mostrará el nombre; el estado de sesión se conserva mientras viva la pestaña y se valida al recargar.

Al cerrar sesión, el token se elimina del almacenamiento de sesión. Las cuentas de la versión previa guardadas solo en este navegador no se importan.

## Catálogo y carrito

Busca por nombre, filtra por tipo, agrega cartas y cambia las cantidades en el carrito. El carrito y cupón son funciones de demostración del navegador. Confirmar compra no procesa cobros, despacho ni stock.

## Administrar el catálogo

El administrador inicial local es `admin@cultochaman.cl` / `Admin123!`. Inicia sesión y abre **Administrar** en la navegación. El formulario permite crear, editar y eliminar cartas. Cada acción se valida también en el backend; un usuario normal recibe 403 aunque intente llamar directamente a las rutas protegidas.

Para Swagger, abre `http://localhost:8081/swagger-ui.html`; ejecuta login, copia el token y autoriza las operaciones ADMIN. La consola de base local está en `http://localhost:8081/h2-console` con JDBC URL `jdbc:h2:file:./data/cultochaman`, usuario `sa` y contraseña vacía.

## Capturas de referencia

Estas capturas del frontend documentan la interfaz visual previa. Para la entrega final, tomar capturas nuevas con el backend iniciado de: catálogo cargado desde API, registro, login, panel ADMIN con CRUD, Swagger mostrando rutas y respuesta, y H2 con tablas/registros.

- Catálogo escritorio: [catalogo-escritorio.png](capturas/catalogo-escritorio.png)
- Catálogo móvil: [catalogo-movil.png](capturas/catalogo-movil.png)
- Carrito: [carrito.png](capturas/carrito.png)

## Problemas habituales

- **No conecta con el servidor:** revisa que Spring Boot siga ejecutándose en puerto 8081 y que el navegador permita el origen local configurado.
- **No aparece Administrar:** verifica que ingresaste con la cuenta ADMIN semilla y que el login devuelve rol `ADMIN`.
- **Token rechazado:** cierra sesión e inicia de nuevo; el token vence al cabo de una hora.
- **La cuenta ya existe:** usa otro correo o inicia sesión; no se crea una segunda cuenta con el mismo email.
