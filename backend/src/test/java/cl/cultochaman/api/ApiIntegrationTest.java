package cl.cultochaman.api;

import cl.cultochaman.api.product.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:culto-integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.email=admin@test.culto",
        "app.admin.password=Admin123!"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void registrationSessionRolesAndProductCrudWorkAcrossTheApi() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "alumno-" + suffix + "@example.cl";

        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

        register(email);
        String userToken = login(email, "ClaveSegura1!").get("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));

        Product product = new Product("integration-" + suffix, "Carta de integración", "Agua",
                "Carta ex - HP 200", "Registro creado desde MockMvc", 15000, "https://example.cl/card.png");
        mvc.perform(post("/api/admin/products").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(product)))
                .andExpect(status().isForbidden());

        String adminToken = login("admin@test.culto", "Admin123!").get("token").asText();
        mvc.perform(post("/api/admin/products").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(product)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("Carta de integración"));
        product.setNombre("Carta actualizada");
        mvc.perform(put("/api/admin/products/" + product.getId()).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(product)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Carta actualizada"));
        mvc.perform(delete("/api/admin/products/" + product.getId()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + product.getId())).andExpect(status().isNotFound());
    }

    @Test
    void refreshTokenRenewsSessionAndCannotBeUsedAsAccessToken() throws Exception {
        String email = "refresh-" + UUID.randomUUID().toString().substring(0, 8) + "@example.cl";
        register(email);
        JsonNode session = login(email, "ClaveSegura1!");
        String refresh = session.get("refreshToken").asText();

        // un refresh token NO sirve como access token
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        // un access token NO sirve para renovar
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", session.get("token").asText()))))
                .andExpect(status().isUnauthorized());

        // el refresh token entrega un access token nuevo y funcional
        String body = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String renewed = json.readTree(body).get("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + renewed))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void orderCalculatesTotalOnServerChecksStockAndRestoresItOnCancel() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "comprador-" + suffix + "@example.cl";
        register(email);
        String userToken = login(email, "ClaveSegura1!").get("token").asText();
        String adminToken = login("admin@test.culto", "Admin123!").get("token").asText();

        String id = "stock-" + suffix;
        Product product = new Product(id, "Carta con stock", "Fuego", "HP 100", "Prueba de inventario",
                10000, "https://example.cl/c.png", 3);
        mvc.perform(post("/api/admin/products").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(product)))
                .andExpect(status().isCreated());

        // sin sesión no se puede comprar
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orderJson(id, 1, null))).andExpect(status().isUnauthorized());

        // más unidades que el stock -> 409
        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(orderJson(id, 5, null)))
                .andExpect(status().isConflict());

        // cupón inválido -> 400
        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON).content(orderJson(id, 1, "NOEXISTE")))
                .andExpect(status().isBadRequest());

        // compra válida con cupón: 2 x 10000 = 20000, descuento 2000, total 18000
        String created = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(orderJson(id, 2, "pokemon10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(20000))
                .andExpect(jsonPath("$.discount").value(2000))
                .andExpect(jsonPath("$.total").value(18000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(created).get("id").asLong();

        mvc.perform(get("/api/products/" + id)).andExpect(jsonPath("$.stock").value(1));
        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(orderId));
        mvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // el admin avanza el estado; un salto inválido es 409
        mvc.perform(put("/api/admin/orders/" + orderId + "/status").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/admin/orders/" + orderId + "/status").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));

        // cancelar devuelve el stock
        mvc.perform(post("/api/orders/" + orderId + "/cancel").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/api/products/" + id)).andExpect(jsonPath("$.stock").value(3));
    }

    private void register(String email) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "fullName", "Alumno de Prueba", "email", email,
                                "password", "ClaveSegura1!", "phone", "912345678",
                                "region", "Metropolitana", "commune", "Santiago"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"));
    }

    private JsonNode login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private String orderJson(String productId, int quantity, String coupon) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("items", List.of(Map.of("productId", productId, "quantity", quantity)));
        if (coupon != null) body.put("coupon", coupon);
        return json.writeValueAsString(body);
    }
}
