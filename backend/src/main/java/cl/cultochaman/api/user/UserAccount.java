package cl.cultochaman.api.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String fullName;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    private String phone;
    private String region;
    private String commune;
    @Column(nullable = false) private String role = "USER";
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected UserAccount() {}

    public UserAccount(String fullName, String email, String passwordHash, String phone,
                       String region, String commune, String role) {
        this.fullName = fullName; this.email = email; this.passwordHash = passwordHash;
        this.phone = phone; this.region = region; this.commune = commune; this.role = role;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public String getRegion() { return region; }
    public String getCommune() { return commune; }
    public String getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setRole(String role) { this.role = role; }
}
