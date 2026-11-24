// URL de la API (Spring Boot en el puerto 8081).
// - En tu PC: http://localhost:8081/api
// - Desplegado en la EC2: usa la misma IP/host desde el que se abrió la página, puerto 8081.
// Se puede editar en dist/config.js sin volver a compilar. Si pones Nginx delante, usa "/api".
window.CULTO_API_URL =
  ["localhost", "127.0.0.1"].includes(window.location.hostname)
    ? "http://localhost:8081/api"
    : `http://${window.location.hostname}:8081/api`;
