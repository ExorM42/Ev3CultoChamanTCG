package cl.cultochaman.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Productos")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) { this.products = products; }

    @Operation(summary = "Listar catálogo (público). Filtros opcionales: tipo y q (texto en el nombre)")
    @GetMapping("/products")
    public List<Product> all(@RequestParam(required = false) String tipo, @RequestParam(required = false) String q) {
        return products.findAll().stream()
                .filter(p -> tipo == null || tipo.isBlank() || p.getTipo().equalsIgnoreCase(tipo.trim()))
                .filter(p -> q == null || q.isBlank() || p.getNombre().toLowerCase().contains(q.trim().toLowerCase()))
                .toList();
    }

    @Operation(summary = "Obtener una carta por id (público)")
    @GetMapping("/products/{id}")
    public Product one(@PathVariable String id) { return find(id); }

    @Operation(summary = "Crear carta (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product p) {
        if (products.existsById(p.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El id ya existe");
        if (p.getStock() == null) p.setStock(Product.DEFAULT_STOCK);
        return products.save(p);
    }

    @Operation(summary = "Actualizar carta (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/admin/products/{id}")
    public Product update(@PathVariable String id, @Valid @RequestBody Product p) {
        Product current = find(id);
        current.update(p);
        return products.save(current);
    }

    @Operation(summary = "Eliminar carta (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/admin/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        if (!products.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        products.deleteById(id);
    }

    private Product find(String id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
}
