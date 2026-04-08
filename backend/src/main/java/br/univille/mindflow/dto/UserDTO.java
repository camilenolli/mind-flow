package br.univille.mindflow.dto;

import br.univille.mindflow.model.User;

public class UserDTO {
    private Long id;
    private String name;
    private String email;

    public UserDTO() {}
    public UserDTO(User u) {
        this.id = u.getId();
        this.name = u.getName();
        this.email = u.getEmail();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
