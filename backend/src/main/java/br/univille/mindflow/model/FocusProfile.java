package br.univille.mindflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

/**
 * Perfil do Modo Foco. Ex.: "Trabalho" 08:00–18:00, "Faculdade" 19:00–22:00.
 * Notas e flashcards podem opcionalmente referenciar um perfil; quando o perfil
 * está ativo (manualmente ou por horário), apenas itens vinculados a ele são exibidos.
 */
@Entity
@Table(name = "focus_profile")
public class FocusProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    /** Dono do perfil de foco. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    public FocusProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /** True se o horário atual cai dentro da janela do perfil. */
    public boolean isActiveAt(LocalTime time) {
        if (startTime == null || endTime == null) return false;
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && time.isBefore(endTime);
        }
        // janela que cruza meia-noite
        return !time.isBefore(startTime) || time.isBefore(endTime);
    }
}
