// Estado de autenticação no cliente: token JWT + dados do usuário
// guardados no localStorage. Usado por api.js (attach do Bearer) e
// layout.js (auth guard das páginas internas).

const Auth = {
  TOKEN_KEY: "mindflow.jwt",
  USER_KEY:  "mindflow.user",

  getToken() { return localStorage.getItem(this.TOKEN_KEY); },
  getUser()  {
    const raw = localStorage.getItem(this.USER_KEY);
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  },
  isAuthenticated() { return !!this.getToken(); },

  saveSession(authResponse) {
    localStorage.setItem(this.TOKEN_KEY, authResponse.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse.user));
  },

  clear() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  },

  logout() {
    this.clear();
    // Limpa também overrides de perfil de foco — eram do usuário anterior
    localStorage.removeItem("mindflow.manualProfileId");
    window.location.href = "login.html";
  },

  // Auth guard: se não estiver autenticado, redireciona para login.
  // Chamado por layout.js antes de mountar a UI.
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = "login.html";
      return false;
    }
    return true;
  },

  // Chamadas REST de auth (não usam o api.js para evitar dependência circular
  // — auth.js precisa estar disponível antes de api.js no <head>).
  async login(email, password) {
    return this._postJson("/api/auth/login", { email, password });
  },
  async register(name, email, password) {
    return this._postJson("/api/auth/register", { name, email, password });
  },

  async _postJson(path, body) {
    const base = this._apiBase();
    const res = await fetch(base + path, {
      method: "POST",
      headers: { "Content-Type": "application/json", "Accept": "application/json" },
      body: JSON.stringify(body),
    });
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = { error: text }; }
    if (!res.ok) {
      const msg = (data && data.error) || `${res.status} ${res.statusText}`;
      throw new Error(msg);
    }
    return data;
  },

  _apiBase() {
    const host = location.hostname;
    const isLocal = host === "localhost" || host === "127.0.0.1" || host === "";
    if (isLocal) {
      return location.port === "8080" ? "/api" : "http://localhost:8080/api";
    }
    return "/api";
  },
};
