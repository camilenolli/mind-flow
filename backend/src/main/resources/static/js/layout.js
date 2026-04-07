// Layout compartilhado: appbar, nav desktop e bottom nav mobile.
// Cada página chama Layout.mount("home" | "notes" | "flashcards" | "focus").

const ICONS = {
  brand: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18M5 8l7 4 7-4M5 16l7 4 7-4"/></svg>',
  home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 11l9-8 9 8v10a2 2 0 0 1-2 2h-4v-7H10v7H6a2 2 0 0 1-2-2V11z"/></svg>',
  notes: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="13" y2="17"/></svg>',
  cards: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="14" height="14" rx="2"/><path d="M7 19h12a2 2 0 0 0 2-2V7"/></svg>',
  focus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg>',
  search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>',
  plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
  edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>',
  trash: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/></svg>',
  check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>',
  x: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
  shuffle: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 3 21 3 21 8"/><line x1="4" y1="20" x2="21" y2="3"/><polyline points="21 16 21 21 16 21"/><line x1="15" y1="15" x2="21" y2="21"/><line x1="4" y1="4" x2="9" y2="9"/></svg>',
  link: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>',
  tag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>',
  clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
  rotate: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>',
  layers: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>',
  sparkles: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l1.9 5.8L20 11l-6.1 2.2L12 19l-1.9-5.8L4 11l6.1-2.2z"/></svg>',
  sun:  '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/></svg>',
  moon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>',
};

// ============================================================
// Theme manager — alterna claro/escuro, persistido em localStorage.
// O tema inicial já é aplicado por um <script> inline no <head> de
// cada página, antes do parse do CSS, para evitar flash de tema errado.
// ============================================================
const Theme = {
  KEY: "mindflow.theme",

  get() {
    return document.documentElement.dataset.theme
        || localStorage.getItem(this.KEY)
        || "dark";
  },

  apply(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(this.KEY, theme);
    this.refreshButton();
  },

  toggle() {
    this.apply(this.get() === "dark" ? "light" : "dark");
  },

  refreshButton() {
    const btn = document.getElementById("theme-toggle");
    if (!btn) return;
    const isDark = this.get() === "dark";
    btn.innerHTML = isDark ? ICONS.sun : ICONS.moon;
    btn.title = isDark ? "Mudar para tema claro" : "Mudar para tema escuro";
    btn.setAttribute("aria-label", btn.title);
  },
};

const NAV = [
  { key: "home",       href: "index.html",      label: "Início",     icon: "home" },
  { key: "notes",      href: "notes.html",      label: "Notas",      icon: "notes" },
  { key: "flashcards", href: "flashcards.html", label: "Flashcards", icon: "cards" },
  { key: "focus",      href: "focus.html",      label: "Foco",       icon: "focus" },
];

const Layout = {
  mount(active) {
    document.body.insertAdjacentHTML("afterbegin", this.appbar(active));
    document.body.insertAdjacentHTML("beforeend", this.bottomNav(active));
    document.body.insertAdjacentHTML("beforeend", '<div id="toast-container"></div>');

    document.getElementById("active-profile").addEventListener("click", () => {
      window.location.href = "focus.html";
    });

    const themeBtn = document.getElementById("theme-toggle");
    themeBtn.addEventListener("click", () => Theme.toggle());
    Theme.refreshButton();
  },

  appbar(active) {
    const links = NAV.map(n => `
      <a href="${n.href}" class="${n.key === active ? "active" : ""}">
        ${ICONS[n.icon]} <span>${n.label}</span>
      </a>`).join("");
    return `
      <header class="appbar">
        <div class="brand">
          <div class="brand-mark">${ICONS.brand}</div>
          <div class="brand-text">
            <span class="name">MindFlow</span>
            <span class="sub">PKM acadêmico · Univille</span>
          </div>
        </div>
        <nav class="nav-desktop">${links}</nav>
        <button id="theme-toggle" class="theme-toggle" type="button" aria-label="Alternar tema"></button>
        <div id="active-profile" class="profile-pill" title="Clique para gerenciar perfis">
          <span class="dot"></span>
          <span class="text">Carregando…</span>
        </div>
      </header>`;
  },

  bottomNav(active) {
    const links = NAV.map(n => `
      <a href="${n.href}" class="${n.key === active ? "active" : ""}">
        ${ICONS[n.icon]} <span>${n.label}</span>
      </a>`).join("");
    return `<nav class="nav-bottom">${links}</nav>`;
  },
};

// ============ Toasts ============
const Toast = {
  show(message, kind = "info", timeout = 2800) {
    const c = document.getElementById("toast-container");
    if (!c) return;
    const icon = kind === "success" ? ICONS.check : kind === "error" ? ICONS.x : ICONS.sparkles;
    const el = document.createElement("div");
    el.className = `toast ${kind}`;
    el.innerHTML = `${icon}<span>${message}</span>`;
    c.appendChild(el);
    setTimeout(() => {
      el.classList.add("fade");
      setTimeout(() => el.remove(), 350);
    }, timeout);
  },
  ok(m)   { this.show(m, "success"); },
  err(m)  { this.show(m, "error"); },
  info(m) { this.show(m, "info"); },
};

// ============ Modal de confirmação ============
function confirmModal(title, message) {
  return new Promise(resolve => {
    const bd = document.createElement("div");
    bd.className = "modal-backdrop";
    bd.innerHTML = `
      <div class="modal">
        <h3>${title}</h3>
        <p>${message}</p>
        <div class="actions">
          <button class="ghost" data-act="no">Cancelar</button>
          <button class="danger" data-act="yes">Confirmar</button>
        </div>
      </div>`;
    document.body.appendChild(bd);
    bd.addEventListener("click", e => {
      if (e.target === bd || e.target.dataset.act === "no") {
        bd.remove(); resolve(false);
      } else if (e.target.dataset.act === "yes") {
        bd.remove(); resolve(true);
      }
    });
  });
}

// ============ Helpers ============
function escapeHtml(s) {
  return String(s ?? "").replace(/[&<>"']/g, m => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;" }[m]));
}

function timeAgo(iso) {
  if (!iso) return "";
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (diff < 60) return "agora";
  if (diff < 3600) return `há ${Math.floor(diff/60)}min`;
  if (diff < 86400) return `há ${Math.floor(diff/3600)}h`;
  if (diff < 2592000) return `há ${Math.floor(diff/86400)}d`;
  return new Date(iso).toLocaleDateString("pt-BR");
}
