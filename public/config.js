// URL de la API Spring Boot.
// En desarrollo local se utiliza localhost; el sitio desplegado usa la EC2 indicada.
// Para usar Nginx como proxy en el mismo dominio, reemplaza la URL por "/api".
window.CULTO_API_URL =
  ["localhost", "127.0.0.1"].includes(window.location.hostname)
    ? "http://localhost:8081/api"
    : "http://100.52.124.23:8081/api";
