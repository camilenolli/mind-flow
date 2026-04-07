// Cliente fetch + estado global de perfil ativo (com override manual em localStorage).
//
// BASE inteligente:
//   - Se servido pelo próprio Spring Boot (mesma origem na 8080), usa "/api" relativo.
//   - Se servido em outra porta/host, usa URL absoluta para localhost:8080.
const API = {
  BASE: (location.protocol === "http:" || location.protocol === "https:") && location.port === "8080"
        ? "/api"
        : "http://localhost:8080/api",

  async _req(path, options = {}) {
    const url = this.BASE + path;
    let res;
    try {
      res = await fetch(url, {
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        mode: "cors",
        ...options,
      });
    } catch (e) {
      // Falha de rede / CORS / backend offline
      console.error(`[API] Falha de rede em ${options.method || "GET"} ${url}`, e);
      throw new Error(
        `Não foi possível contatar o backend em ${this.BASE}. ` +
        `Verifique se o Spring Boot está rodando (mvn spring-boot:run) ` +
        `e se você abriu o frontend via http:// (não file://).`
      );
    }
    if (res.status === 204) return null;
    if (!res.ok) {
      let body = "";
      try { body = await res.text(); } catch {}
      console.error(`[API] ${res.status} em ${options.method || "GET"} ${url}:`, body);
      let msg = `${res.status} ${res.statusText}`;
      try {
        const j = JSON.parse(body);
        if (j.error) msg = j.error;
        else if (typeof j === "object") msg = Object.values(j).join(", ");
      } catch {
        if (body) msg = body.slice(0, 200);
      }
      throw new Error(msg);
    }
    try {
      return await res.json();
    } catch {
      return null;
    }
  },

  // Notas
  listNotes(focusProfileId)   { return this._req(`/notes${focusProfileId ? `?focusProfileId=${focusProfileId}` : ""}`); },
  recentNotes()               { return this._req(`/notes/recent`); },
  getNote(id)                 { return this._req(`/notes/${id}`); },
  relatedNotes(id)            { return this._req(`/notes/${id}/related`); },
  createNote(dto)             { return this._req(`/notes`, { method: "POST", body: JSON.stringify(dto) }); },
  updateNote(id, dto)         { return this._req(`/notes/${id}`, { method: "PUT", body: JSON.stringify(dto) }); },
  deleteNote(id)              { return this._req(`/notes/${id}`, { method: "DELETE" }); },

  // Flashcards
  listCards(focusProfileId)   { return this._req(`/flashcards${focusProfileId ? `?focusProfileId=${focusProfileId}` : ""}`); },
  drawCard(focusProfileId)    { return this._req(`/flashcards/draw${focusProfileId ? `?focusProfileId=${focusProfileId}` : ""}`); },
  createCard(dto)             { return this._req(`/flashcards`, { method: "POST", body: JSON.stringify(dto) }); },
  deleteCard(id)              { return this._req(`/flashcards/${id}`, { method: "DELETE" }); },

  // Perfis
  listProfiles()              { return this._req(`/focus-profiles`); },
  activeProfile()             { return this._req(`/focus-profiles/active`); },
  createProfile(p)            { return this._req(`/focus-profiles`, { method: "POST", body: JSON.stringify(p) }); },
  updateProfile(id, p)        { return this._req(`/focus-profiles/${id}`, { method: "PUT", body: JSON.stringify(p) }); },
  deleteProfile(id)           { return this._req(`/focus-profiles/${id}`, { method: "DELETE" }); },

  // Tags + Stats
  listTags()                  { return this._req(`/tags`); },
  stats()                     { return this._req(`/stats`); },
};

// ============================================================
// Perfil ativo: backend determina por horário, mas o usuário
// pode forçar manualmente um perfil via localStorage.
// ============================================================
const ProfileState = {
  KEY: "mindflow.manualProfileId",

  async resolveActive() {
    const manual = this.getManual();
    if (manual) {
      try {
        const profiles = await API.listProfiles();
        const found = profiles.find(p => p.id === manual);
        if (found) return { profile: found, manual: true };
      } catch {}
    }
    try {
      const p = await API.activeProfile();
      return { profile: p, manual: false };
    } catch {
      return { profile: null, manual: false };
    }
  },

  getManual() {
    const v = localStorage.getItem(this.KEY);
    return v ? parseInt(v, 10) : null;
  },
  setManual(id) {
    if (id == null) localStorage.removeItem(this.KEY);
    else localStorage.setItem(this.KEY, String(id));
    refreshActiveProfilePill();
    document.dispatchEvent(new CustomEvent("profile-changed"));
  },
};

async function refreshActiveProfilePill() {
  const el = document.getElementById("active-profile");
  if (!el) return;
  const text = el.querySelector(".text");
  const dot  = el.querySelector(".dot");
  try {
    const { profile, manual } = await ProfileState.resolveActive();
    if (profile) {
      text.textContent = `${manual ? "Forçado: " : ""}${profile.name}`;
      el.classList.add("active");
      el.title = manual
        ? "Perfil definido manualmente — clique para gerenciar"
        : "Perfil ativo pelo horário — clique para gerenciar";
    } else {
      text.textContent = "Sem perfil ativo";
      el.classList.remove("active");
      el.title = "Nenhum perfil ativo no horário atual";
    }
  } catch {
    text.textContent = "API offline";
    el.classList.remove("active");
  }
}

document.addEventListener("DOMContentLoaded", refreshActiveProfilePill);
