package cl.cultochaman.api.order;

import cl.cultochaman.api.order.OrderDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @Operation(summary = "Crear pedido: valida stock, calcula el total en el servidor y descuenta inventario")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView create(@Valid @RequestBody CreateOrderRequest body, Authentication auth) {
        return service.create(auth.getName(), body);
    }

    @Operation(summary = "Mis pedidos")
    @GetMapping("/orders")
    public List<OrderView> mine(Authentication auth) { return service.mine(auth.getName()); }

    @Operation(summary = "Detalle de un pedido propio (ADMIN puede ver cualquiera)")
    @GetMapping("/orders/{id}")
    public OrderView one(@PathVariable Long id, Authentication auth) {
        return service.get(id, auth.getName(), isAdmin(auth));
    }

    @Operation(summary = "Cancelar un pedido propio (devuelve el stock)")
    @PostMapping("/orders/{id}/cancel")
    public OrderView cancel(@PathVariable Long id, Authentication auth) {
        return service.cancel(id, auth.getName(), isAdmin(auth));
    }

    @Operation(summary = "Listar todos los pedidos (ADMIN)")
    @GetMapping("/admin/orders")
    public List<OrderView> all() { return service.all(); }

    @Operation(summary = "Cambiar estado de un pedido: PAID, SHIPPED o CANCELLED (ADMIN)")
    @PutMapping("/admin/orders/{id}/status")
    public OrderView changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest body) {
        return service.changeStatus(id, body.status());
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
