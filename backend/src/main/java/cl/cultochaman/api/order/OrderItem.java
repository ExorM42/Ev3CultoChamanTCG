package cl.cultochaman.api.order;

import jakarta.persistence.*;

/** Línea de un pedido. Guarda una copia del nombre y precio al momento de la compra. */
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;
    @Column(nullable = false) private String productId;
    @Column(nullable = false) private String productName;
    private int unitPrice;
    private int quantity;

    protected OrderItem() {}

    public OrderItem(Order order, String productId, String productName, int unitPrice, int quantity) {
        this.order = order; this.productId = productId; this.productName = productName;
        this.unitPrice = unitPrice; this.quantity = quantity;
    }

    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
