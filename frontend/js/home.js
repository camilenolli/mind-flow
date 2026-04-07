// Dashboard
Layout.mount("home");

// Injetar ícones nos títulos
document.getElementById("recent-title").insertAdjacentHTML("afterbegin", ICONS.notes);
document.getElementById("quick-title").insertAdjacentHTML("afterbegin", ICONS.sparkles);
document.getElementById("tags-title").insertAdjacentHTML("afterbegin", ICONS.tag);
document.getElementById("quick-submit-btn").insertAdjacentHTML("afterbegin", ICONS.plus);

const STAT_LABELS = [
  { key: "notes",      label: "Notas",      icon: "notes" },
  { key: "flashcards", label: "Flashcards", icon: "cards" },
  { key: "tags",       label: "Tags",       icon: "tag" },
  { key: "profiles",   label: "Perfis",     icon: "focus" },
];

async function loadStats() {
  try {
    const s = await API.stats();
    const container = document.getElementById("stats");
    container.innerHTML = STAT_LABELS.map((m, i) => `
      <div class="stat" style="animation-delay:${i*60}ms">
        <div class="label">${ICONS[m.icon]} ${m.label}</div>
        <div class="value" data-target="${s[m.key] ?? 0}">0</div>
      </div>
    `).join("");
    container.querySelectorAll(".value").forEach(animateCount);
  } catch (err) {
    console.error(err);
    Toast.err(err.message);
  }
}

function animateCount(el) {
  const target = parseInt(el.dataset.target, 10) || 0;
  const dur = 700;
  const start = performance.now();
  function step(now) {
    const t = Math.min(1, (now - start) / dur);
    const eased = 1 - Math.pow(1 - t, 3);
    el.textContent = Math.round(eased * target);
    if (t < 1) requestAnimationFrame(step);
  }
  requestAnimationFrame(step);
}

async function loadRecent() {
  const list = document.getElementById("recent-notes");
  try {
    const notes = await API.recentNotes();
    if (!notes.length) {
      list.innerHTML = emptyState("Nenhuma nota ainda", "Capture sua primeira ideia ao lado →");
      return;
    }
    list.innerHTML = notes.map((n, i) => `
      <li class="card-item" style="animation-delay:${i*50}ms" onclick="window.location.href='notes.html#${n.id}'">
        <h3>${escapeHtml(n.title)} <span class="muted" style="font-size:0.7rem; font-weight:400">${timeAgo(n.updatedAt)}</span></h3>
        <p>${escapeHtml(n.content)}</p>
        <div class="meta">${[...n.tags].slice(0,4).map(t => `<span class="chip tiny">${escapeHtml(t)}</span>`).join("")}</div>
      </li>`).join("");
  } catch {
    list.innerHTML = emptyState("Não foi possível carregar", "Verifique a API");
  }
}

async function loadTags() {
  const cloud = document.getElementById("tag-cloud");
  try {
    const tags = await API.listTags();
    if (!tags.length) {
      cloud.innerHTML = `<p class="muted">Nenhuma tag ainda — crie notas para começar.</p>`;
      return;
    }
    cloud.innerHTML = tags.map(t => `<span class="chip">${ICONS.tag} ${escapeHtml(t.name)}</span>`).join("");
  } catch {
    cloud.innerHTML = "";
  }
}

function emptyState(title, msg) {
  return `<div class="empty">${ICONS.layers}<p><strong>${title}</strong></p><p>${msg}</p></div>`;
}

document.getElementById("quick-form").addEventListener("submit", async e => {
  e.preventDefault();
  const dto = {
    title:   document.getElementById("q-title").value.trim(),
    content: document.getElementById("q-content").value.trim(),
    tags:    document.getElementById("q-tags").value.split(",").map(s => s.trim()).filter(Boolean),
  };
  try {
    await API.createNote(dto);
    e.target.reset();
    Toast.ok("Nota salva!");
    loadStats(); loadRecent(); loadTags();
  } catch (err) {
    console.error(err);
    Toast.err("Falha ao salvar: " + err.message);
  }
});

loadStats();
loadRecent();
loadTags();
