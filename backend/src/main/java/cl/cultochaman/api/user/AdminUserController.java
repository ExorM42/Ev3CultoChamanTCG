package cl.cultochaman.api.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios (ADMIN)")
public class AdminUserController {

    public record UserView(Long id, String email, String fullName, String role,
                           String phone, String region, String commune, Instant createdAt) {}
    public record RoleRequest(@NotBlank @Pattern(regexp = "USER|ADMIN", message = "El rol debe ser USER o ADMIN") String role) {}

    private final UserRepository users;

    public AdminUserController(UserRepository users) { this.users = users; }

    @Operation(summary = "Listar usuarios (sin hashes de contraseña)")
    @GetMapping
    public List<UserView> all() {
        return users.findAll().stream().map(this::view).toList();
    }

    @Operation(summary = "Obtener un usuario por id")
    @GetMapping("/{id}")
    public UserView one(@PathVariable Long id) {
        return view(find(id));
    }

    @Operation(summary = "Cambiar el rol de un usuario (USER o ADMIN)")
    @PutMapping("/{id}/role")
    public UserView changeRole(@PathVariable Long id, @Valid @RequestBody RoleRequest body, Principal principal) {
        UserAccount user = find(id);
        if (user.getEmail().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol.");
        user.setRole(body.role());
        return view(users.save(user));
    }

    @Operation(summary = "Eliminar un usuario")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        UserAccount user = find(id);
        if (user.getEmail().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        users.delete(user);
    }

    private UserAccount find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UserView view(UserAccount u) {
        return new UserView(u.getId(), u.getEmail(), u.getFullName(), u.getRole(),
                u.getPhone(), u.getRegion(), u.getCommune(), u.getCreatedAt());
    }
}
