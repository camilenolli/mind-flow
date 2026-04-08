package br.univille.mindflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Usuário do MindFlow. Cada usuário possui seu próprio conjunto de
 * notas, flashcards, perfis de foco e tags — multi-tenant simples
 * via FK em todas as entidades de domínio.
 *
 * Senhas são armazenadas como hash BCrypt (nunca em claro).
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, length = 180)
    private String email;

    @NotBlank
    @Size(min = 60, max = 80) // BCrypt sempre ~60 chars
    @Column(name = "password_hash", nullable = false, length = 80)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
