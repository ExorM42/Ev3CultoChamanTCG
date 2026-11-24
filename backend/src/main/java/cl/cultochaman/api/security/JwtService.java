package cl.cultochaman.api.security;

import cl.cultochaman.api.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Emite y valida dos tipos de token firmados con HMAC:
 *  - access  (corta duración, se envía en Authorization: Bearer ...)
 *  - refresh (larga duración, solo sirve en POST /api/auth/refresh)
 * El claim "type" evita que un refresh token se use como access token.
 */
@Service
public class JwtService {
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    private final SecretKey key;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long accessMs,
                      @Value("${app.jwt.refresh-expiration-ms}") long refreshMs,
                      Environment env) {
        if (secret.startsWith("dev-only") && env.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("Configura JWT_SECRET con un valor aleatorio antes de usar el perfil prod.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String issueAccess(UserAccount user) { return build(user, ACCESS, accessMs); }

    public String issueRefresh(UserAccount user) { return build(user, REFRESH, refreshMs); }

    private String build(UserAccount user, String type, long ttl) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .claim("type", type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(key)
                .compact();
    }

    /** Verifica firma, expiración y tipo; devuelve el email (subject). Lanza JwtException si algo falla. */
    public String subjectOf(String token, String expectedType) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!expectedType.equals(claims.get("type", String.class))) {
            throw new JwtException("Tipo de token inválido");
        }
        return claims.getSubject();
    }
}
