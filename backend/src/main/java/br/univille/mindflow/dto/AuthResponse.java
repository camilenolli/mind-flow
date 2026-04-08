package br.univille.mindflow.dto;

public class AuthResponse {
    private String token;
    private long expiresInMs;
    private UserDTO user;

    public AuthResponse() {}
    public AuthResponse(String token, long expiresInMs, UserDTO user) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
