// Cliente fetch + estado global de perfil ativo (com override manual em localStorage).
//
// BASE inteligente:
//   - Localhost na 8080 (Spring Boot dev): "/api" relativo (mesma origem).
//   - Localhost em outra porta (ex: python http.server na 5500): URL absoluta para localhost:8080.
//   - Qualquer outro host (Render, deploy, github.io etc): "/api" relativo, pois o
//     próprio Spring Boot serve os arquivos estáticos da mesma origem.
const API = {
  BASE: (() => {
    const host = location.hostname;
    const isLocal = host === "localhost" || host === "127.0.0.1" || host === "";
    if (isLocal) {
      return location.port === "8080" ? "/api" : "http://localhost:8080/api";
    }
    return "/api";
  })(),

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
// Perfil ativo: o cálculo é feito no CLIENTE usando a hora local
// do navegador. Por que não usar o endpoint /api/focus-profiles/active?
// Porque o backend usa LocalTime.now() do servidor, e o Render roda em
// Oregon (Pacific Time) — então um usuário no Brasil veria o perfil
// errado. "Faculdade 19h-22h" significa 19h-22h NA HORA DO USUÁRIO.
// O usuário ainda pode forçar manualmente um perfil via localStorage.
// ============================================================
function isProfileActiveLocal(profile) {
  if (!profile || !profile.startTime || !profile.endTime) return false;
  const now = new Date();
  const cur = now.getHours() * 60 + now.getMinutes();
  const [sh, sm] = String(profile.startTime).split(":").map(Number);
  const [eh, em] = String(profile.endTime).split(":").map(Number);
  const start = sh * 60 + sm;
  const end = eh * 60 + em;
  if (start < end) return cur >= start && cur < end;
  // Janela cruzando meia-noite (ex: 22h-06h)
  return cur >= start || cur < end;
}

const ProfileState = {
  KEY: "mindflow.manualProfileId",

  async resolveActive() {
    let profiles = [];
    try {
      profiles = await API.listProfiles();
    } catch {
      return { profile: null, manual: false };
    }

    // 1) Override manual tem precedência
    const manual = this.getManual();
    if (manual) {
      const found = profiles.find(p => p.id === manual);
      if (found) return { profile: found, manual: true };
    }

    // 2) Auto-detecção pela hora local do navegador
    const found = profiles.find(isProfileActiveLocal);
    return { profile: found || null, manual: false };
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
