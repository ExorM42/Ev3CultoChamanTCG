package cl.cultochaman.api.order;

import cl.cultochaman.api.order.OrderDtos.*;
import cl.cultochaman.api.product.Product;
import cl.cultochaman.api.product.ProductRepository;
import cl.cultochaman.api.user.UserAccount;
import cl.cultochaman.api.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica de negocio de la tienda:
 *  - el precio SIEMPRE se calcula en el servidor (el cliente solo envía id y cantidad)
 *  - valida existencia y stock, y descuenta inventario de forma atómica
 *  - cupón POKEMON10 = 10 % (mismo criterio que el carrito del frontend)
 *  - máquina de estados del pedido; al cancelar se devuelve el stock
 */
@Service
public class OrderService {
    public static final String COUPON = "POKEMON10";

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    @Transactional
    public OrderView create(String email, CreateOrderRequest request) {
        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida."));

        // Une líneas repetidas del mismo producto
        Map<String, Integer> wanted = new LinkedHashMap<>();
        for (ItemRequest item : request.items()) wanted.merge(item.productId(), item.quantity(), Integer::sum);
        for (Map.Entry<String, Integer> e : wanted.entrySet()) {
            if (e.getValue() > 99)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 99 unidades por carta.");
        }

        Map<String, Product> found = products.findAllByIdForUpdate(wanted.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        String couponUsed = null;
        if (request.coupon() != null && !request.coupon().isBlank()) {
            if (!COUPON.equalsIgnoreCase(request.coupon().trim()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cupón inválido. Verifica el código.");
            couponUsed = COUPON;
        }

        int subtotal = 0;
        for (Map.Entry<String, Integer> e : wanted.entrySet()) {
            Product p = found.get(e.getKey());
            if (p == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La carta no existe: " + e.getKey());
            if (!p.canSell(e.getValue()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + p.getNombre() + " (disponible: " + p.getStock() + ").");
            subtotal += p.getPrecio() * e.getValue();
        }
        int discount = couponUsed == null ? 0 : (int) Math.round(subtotal * 0.1);
        Order order = new Order(user, subtotal, discount, subtotal - discount, couponUsed);

        for (Map.Entry<String, Integer> e : wanted.entrySet()) {
            Product p = found.get(e.getKey());
            p.takeStock(e.getValue());
            order.getItems().add(new OrderItem(order, p.getId(), p.getNombre(), p.getPrecio(), e.getValue()));
        }
        return view(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderView> mine(String email) {
        return orders.findByUserEmailOrderByCreatedAtDesc(email).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderView> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public OrderView get(Long id, String email, boolean admin) {
        return view(findVisible(id, email, admin));
    }

    /** El cliente solo puede cancelar sus propios pedidos; ADMIN cualquiera. */
    @Transactional
    public OrderView cancel(Long id, String email, boolean admin) {
        return view(move(findVisible(id, email, admin), OrderStatus.CANCELLED));
    }

    @Transactional
    public OrderView changeStatus(Long id, OrderStatus next) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        return view(move(order, next));
    }

    private Order move(Order order, OrderStatus next) {
        if (!order.getStatus().canMoveTo(next))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + order.getStatus() + " a " + next + ".");
        if (next == OrderStatus.CANCELLED) {
            List<String> ids = order.getItems().stream().map(OrderItem::getProductId).toList();
            Map<String, Product> byId = products.findAllByIdForUpdate(ids).stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
            for (OrderItem item : order.getItems()) {
                Product p = byId.get(item.getProductId());
                if (p != null) p.returnStock(item.getQuantity()); // si la carta fue eliminada, se omite
            }
        }
        order.setStatus(next);
        return orders.save(order);
    }

    private Order findVisible(Long id, String email, boolean admin) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        if (!admin && !order.getUser().getEmail().equals(email))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"); // no revela que existe
        return order;
    }

    private OrderView view(Order o) {
        List<ItemView> items = o.getItems().stream()
                .map(i -> new ItemView(i.getProductId(), i.getProductName(), i.getUnitPrice(),
                        i.getQuantity(), i.getUnitPrice() * i.getQuantity()))
                .toList();
        return new OrderView(o.getId(), o.getUser().getEmail(), o.getStatus().name(), o.getSubtotal(),
                o.getDiscount(), o.getTotal(), o.getCoupon(), o.getCreatedAt(), items);
    }
}
