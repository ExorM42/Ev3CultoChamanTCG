package cl.cultochaman.api.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record ItemRequest(@NotBlank String productId, @Min(1) @Max(99) int quantity) {}
    public record CreateOrderRequest(@NotEmpty @Valid List<ItemRequest> items, String coupon) {}
    public record StatusRequest(@NotNull OrderStatus status) {}

    public record ItemView(String productId, String productName, int unitPrice, int quantity, int lineTotal) {}
    public record OrderView(Long id, String userEmail, String status, int subtotal, int discount,
                            int total, String coupon, Instant createdAt, List<ItemView> items) {}
}
