package br.univille.mindflow.dto;

import jakarta.validation.constraints.NotBlank;

public class FlashcardDTO {
    private Long id;
    @NotBlank private String question;
    @NotBlank private String answer;
    private Long noteId;
    private Long focusProfileId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public Long getFocusProfileId() { return focusProfileId; }
    public void setFocusProfileId(Long focusProfileId) { this.focusProfileId = focusProfileId; }
}
