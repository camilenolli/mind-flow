// Dashboard
Layout.mount("home");

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

// ── Image upload (ação rápida) ────────────────────────────────────────────────
const qImageInput   = document.getElementById("q-image");
const qImgLabel     = document.getElementById("q-img-label");
const qImgPreview   = document.getElementById("q-img-preview");
const qImgWrap      = document.getElementById("q-img-preview-wrap");
const qImgText      = document.getElementById("q-img-text");
const qImgRemoveBtn = document.getElementById("q-img-remove");
let   qImageData     = null;
let   qImageFileName = null;

qImageInput.addEventListener("change", () => {
  const file = qImageInput.files[0];
  if (!file) return;
  if (file.size > 2 * 1024 * 1024) { Toast.err("Imagem muito grande (máx. 2 MB)"); qImageInput.value = ""; return; }
  const reader = new FileReader();
  reader.onload = e => {
    qImageData = e.target.result;
    qImageFileName = file.name;
    qImgPreview.src = qImageData;
    qImgWrap.style.display = "block";
    qImgText.textContent = file.name;
  };
  reader.readAsDataURL(file);
});

qImgRemoveBtn.addEventListener("click", clearQImage);

function clearQImage() {
  qImageData = null; qImageFileName = null;
  qImageInput.value = ""; qImgPreview.src = "";
  qImgWrap.style.display = "none";
  qImgText.textContent = "Selecionar imagem…";
}

// Drag & drop na área de upload
qImgLabel.addEventListener("dragover", e => { e.preventDefault(); qImgLabel.style.borderColor = "var(--c2)"; });
qImgLabel.addEventListener("dragleave", () => { qImgLabel.style.borderColor = ""; });
qImgLabel.addEventListener("drop", e => {
  e.preventDefault(); qImgLabel.style.borderColor = "";
  const file = e.dataTransfer.files[0];
  if (!file || !file.type.startsWith("image/")) return;
  if (file.size > 2 * 1024 * 1024) { Toast.err("Imagem muito grande (máx. 2 MB)"); return; }
  const reader = new FileReader();
  reader.onload = ev => {
    qImageData = ev.target.result; qImageFileName = file.name;
    qImgPreview.src = qImageData; qImgWrap.style.display = "block"; qImgText.textContent = file.name;
  };
  reader.readAsDataURL(file);
});

// ── Sugestão de tags com IA ───────────────────────────────────────────────────
const qSuggestBtn = document.getElementById("q-suggest-btn");
const qTagsInput  = document.getElementById("q-tags");

qSuggestBtn.addEventListener("click", async () => {
  const title   = document.getElementById("q-title").value.trim();
  const content = document.getElementById("q-content").value.trim();
  if (!title && !content) { Toast.info("Preencha título ou conteúdo para sugerir tags"); return; }
  const orig = qSuggestBtn.innerHTML;
  qSuggestBtn.disabled = true;
  qSuggestBtn.innerHTML = `<span class="spinner spinner-sm"></span>`;
  try {
    const tags = await API.suggestTags(title, content);
    if (tags && tags.length) {
      qTagsInput.value = tags.join(", ");
      Toast.ok(`${tags.length} tags sugeridas pela IA!`);
    } else {
      Toast.info("Sem sugestões disponíveis");
    }
  } catch (err) {
    Toast.err("IA indisponível: " + err.message);
  } finally {
    qSuggestBtn.disabled = false;
    qSuggestBtn.innerHTML = orig;
  }
});

// ── Stats ─────────────────────────────────────────────────────────────────────
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

// ── Notas recentes ────────────────────────────────────────────────────────────
async function loadRecent() {
  const list = document.getElementById("recent-notes");
  list.innerHTML = spinnerHtml();
  try {
    const notes = await API.recentNotes();
    if (!notes.length) {
      list.innerHTML = emptyState("Nenhuma nota ainda", "Capture sua primeira ideia ao lado →");
      return;
    }
    list.innerHTML = notes.map((n, i) => {
      const thumb = n.imageData
        ? `<img class="note-thumb-home" src="${n.imageData}" alt="${escapeHtml(n.imageFileName || 'imagem')}" onclick="Lightbox.open(this.src, this.alt)" />`
        : "";
      return `
        <li class="card-item" style="animation-delay:${i*50}ms" onclick="window.location.href='notes.html#${n.id}'">
          ${thumb}
          <h3>${escapeHtml(n.title)} <span class="muted" style="font-size:0.7rem; font-weight:400">${timeAgo(n.updatedAt)}</span></h3>
          <p>${escapeHtml(n.content)}</p>
          <div class="meta">${[...n.tags].slice(0,4).map(t => `<span class="chip tiny">${escapeHtml(t)}</span>`).join("")}</div>
        </li>`;
    }).join("");
  } catch {
    list.innerHTML = emptyState("Não foi possível carregar", "Verifique a API");
  }
}

// ── Tags ──────────────────────────────────────────────────────────────────────
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

// ── Ação rápida ───────────────────────────────────────────────────────────────
document.getElementById("quick-form").addEventListener("submit", async e => {
  e.preventDefault();
  const dto = {
    title:         document.getElementById("q-title").value.trim(),
    content:       document.getElementById("q-content").value.trim(),
    tags:          qTagsInput.value.split(",").map(s => s.trim()).filter(Boolean),
    imageData:     qImageData || null,
    imageFileName: qImageFileName || null,
  };
  try {
    await API.createNote(dto);
    e.target.reset();
    clearQImage();
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
