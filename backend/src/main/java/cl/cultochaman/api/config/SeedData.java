package cl.cultochaman.api.config;

import cl.cultochaman.api.product.Product;
import cl.cultochaman.api.product.ProductRepository;
import cl.cultochaman.api.user.UserAccount;
import cl.cultochaman.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(ProductRepository products, UserRepository users, PasswordEncoder encoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                    new Product("charizard-ex", "Charizard ex", "Fuego", "Carta ultra rara - HP 330", "Poder ofensivo alto para mazos agresivos de tipo fuego.", 45990, "https://images.pokemontcg.io/sv3pt5/6_hires.png", 10),
                    new Product("pikachu-v", "Pikachu V", "Eléctrico", "Carta V - HP 190", "Carta veloz y fácil de combinar con energía eléctrica.", 18990, "https://images.pokemontcg.io/swsh4/43_hires.png", 25),
                    new Product("mewtwo-ex", "Mewtwo ex", "Psíquico", "Carta ex - HP 230", "Ataques psíquicos con gran presión para partidas largas.", 34990, "https://images.pokemontcg.io/sv4/58_hires.png", 12),
                    new Product("lucario-vstar", "Lucario VSTAR", "Lucha", "Carta VSTAR - HP 270", "Excelente opción para jugadores que buscan contraataque.", 29990, "https://images.pokemontcg.io/swshp/SWSH214_hires.png", 15),
                    new Product("greninja-ex", "Greninja ex", "Agua", "Carta ex - HP 310", "Ideal para mazos técnicos con control del banco rival.", 32990, "https://images.pokemontcg.io/sv6/106_hires.png", 8),
                    new Product("gardevoir-ex", "Gardevoir ex", "Psíquico", "Carta ex - HP 310", "Soporte potente para acelerar energía psíquica.", 27990, "https://images.pokemontcg.io/sv1/86_hires.png", 14),
                    new Product("snorlax", "Snorlax", "Normal", "Carta holo - HP 150", "Defensa sólida y estilo clásico para coleccionistas.", 12990, "https://images.pokemontcg.io/pgo/55_hires.png", 30),
                    new Product("eevee", "Eevee", "Normal", "Carta promocional - HP 60", "Favorita para completar líneas evolutivas y colecciones.", 6990, "https://images.pokemontcg.io/swshp/SWSH190_hires.png", 50)));
            }
            String email = adminEmail.trim().toLowerCase();
            if (!users.existsByEmail(email)) {
                users.save(new UserAccount("Administrador Culto Chamán", email,
                        encoder.encode(adminPassword), null, null, null, "ADMIN"));
            }
        };
    }
}
