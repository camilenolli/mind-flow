package br.univille.mindflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String question;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Nota que originou este flashcard (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_profile_id")
    private FocusProfile focusProfile;

    /** Dono do flashcard. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
    public FocusProfile getFocusProfile() { return focusProfile; }
    public void setFocusProfile(FocusProfile focusProfile) { this.focusProfile = focusProfile; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
