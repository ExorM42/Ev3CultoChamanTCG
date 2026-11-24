package cl.cultochaman.api.auth;

import cl.cultochaman.api.security.JwtService;
import cl.cultochaman.api.user.UserAccount;
import cl.cultochaman.api.user.UserRepository;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 50) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,128}$",
                    message = "La contraseña debe incluir mayúscula, minúscula, número y símbolo") String password,
            String phone, String region, String commune) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record AuthResponse(String token, String refreshToken, String email, String fullName, String role) {}
    public record Profile(String email, String fullName, String role, String phone, String region, String commune) {}

    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @Operation(summary = "Registrar cuenta (siempre rol USER; el rol nunca viene del cliente)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Profile register(@Valid @RequestBody RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este correo ya está registrado.");
        UserAccount saved = users.save(new UserAccount(r.fullName().trim(), email,
                passwords.encode(r.password()), r.phone(), r.region(), r.commune(), "USER"));
        return profile(saved);
    }

    @Operation(summary = "Iniciar sesión: devuelve token (access) y refreshToken")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        UserAccount user = users.findByEmail(r.email().trim().toLowerCase())
                .filter(u -> passwords.matches(r.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        return tokens(user);
    }

    @Operation(summary = "Renovar tokens usando un refreshToken válido")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest r) {
        try {
            String email = jwt.subjectOf(r.refreshToken(), JwtService.REFRESH);
            UserAccount user = users.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida."));
            return tokens(user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión expiró. Inicia sesión nuevamente.");
        }
    }

    @Operation(summary = "Perfil del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public Profile me(Principal principal) {
        return users.findByEmail(principal.getName()).map(this::profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida."));
    }

    private AuthResponse tokens(UserAccount u) {
        return new AuthResponse(jwt.issueAccess(u), jwt.issueRefresh(u), u.getEmail(), u.getFullName(), u.getRole());
    }

    private Profile profile(UserAccount u) {
        return new Profile(u.getEmail(), u.getFullName(), u.getRole(), u.getPhone(), u.getRegion(), u.getCommune());
    }
}
