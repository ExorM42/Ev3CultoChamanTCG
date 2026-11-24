package cl.cultochaman.api.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "products")
public class Product {
    public static final int DEFAULT_STOCK = 20;

    @NotBlank @Id @Column(length = 80) private String id;
    @NotBlank @Column(nullable = false) private String nombre;
    @NotBlank @Column(nullable = false) private String tipo;
    @NotBlank @Column(nullable = false) private String atributo;
    @NotBlank @Column(length = 1000, nullable = false) private String descripcion;
    @Min(0) private int precio;
    @NotBlank @Column(length = 1000, nullable = false) private String imagen;
    /** Unidades disponibles. Si el cliente no lo envía, se conserva (PUT) o se asigna el valor por defecto (POST). */
    @Min(0) @ColumnDefault("20") @Column(nullable = false) private Integer stock;

    protected Product() {}

    public Product(String id, String nombre, String tipo, String atributo, String descripcion, int precio, String imagen) {
        this(id, nombre, tipo, atributo, descripcion, precio, imagen, DEFAULT_STOCK);
    }

    public Product(String id, String nombre, String tipo, String atributo, String descripcion,
                   int precio, String imagen, Integer stock) {
        this.id = id; this.nombre = nombre; this.tipo = tipo; this.atributo = atributo;
        this.descripcion = descripcion; this.precio = precio; this.imagen = imagen; this.stock = stock;
    }

    @PrePersist
    void defaults() { if (stock == null) stock = DEFAULT_STOCK; }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getAtributo() { return atributo; }
    public String getDescripcion() { return descripcion; }
    public int getPrecio() { return precio; }
    public String getImagen() { return imagen; }
    public Integer getStock() { return stock; }

    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setAtributo(String atributo) { this.atributo = atributo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(int precio) { this.precio = precio; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setStock(Integer stock) { this.stock = stock; }

    public void update(Product p) {
        nombre = p.nombre; tipo = p.tipo; atributo = p.atributo;
        descripcion = p.descripcion; precio = p.precio; imagen = p.imagen;
        if (p.stock != null) stock = p.stock;
    }

    // Lógica de inventario usada por los pedidos (sin forma de getter/setter para que Jackson no la serialice)
    public boolean canSell(int quantity) { return stock != null && stock >= quantity; }
    public void takeStock(int quantity) { stock = stock - quantity; }
    public void returnStock(int quantity) { stock = (stock == null ? 0 : stock) + quantity; }
}
