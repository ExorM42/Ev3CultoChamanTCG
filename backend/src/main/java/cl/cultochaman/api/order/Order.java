package cl.cultochaman.api.order;

import cl.cultochaman.api.user.UserAccount;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" es palabra reservada en SQL
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    private int subtotal;
    private int discount;
    private int total;
    private String coupon;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected Order() {}

    public Order(UserAccount user, int subtotal, int discount, int total, String coupon) {
        this.user = user; this.subtotal = subtotal; this.discount = discount;
        this.total = total; this.coupon = coupon;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public int getSubtotal() { return subtotal; }
    public int getDiscount() { return discount; }
    public int getTotal() { return total; }
    public String getCoupon() { return coupon; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
