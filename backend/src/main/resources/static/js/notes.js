// Tela de notas — CRUD + busca + relacionados
Layout.mount("notes");

const formTitle = document.getElementById("form-title");
function setFormTitle(text) { formTitle.innerHTML = ICONS.edit + ` <span>${text}</span>`; }
setFormTitle("Nova nota");

document.getElementById("list-title").insertAdjacentHTML("afterbegin", ICONS.notes);
document.getElementById("related-title").insertAdjacentHTML("afterbegin", ICONS.link);
document.getElementById("save-btn").insertAdjacentHTML("afterbegin", ICONS.check);

const form         = document.getElementById("note-form");
const idInput      = document.getElementById("note-id");
const titleInput   = document.getElementById("note-title");
const contentInput = document.getElementById("note-content");
const tagsInput    = document.getElementById("note-tags");
const profileSel   = document.getElementById("note-profile");
const list         = document.getElementById("notes-list");
const relatedList  = document.getElementById("related-list");
const filterToggle = document.getElementById("filter-by-active");
const searchInput  = document.getElementById("note-search");
const countBadge   = document.getElementById("notes-count");
const resetBtn     = document.getElementById("note-reset");

let allNotes = [];
let activeProfileId = null;
let selectedId = null;

async function bootstrap() {
  await loadProfiles();
  await refreshList();
  // deep-link via hash (#42 abre nota 42)
  if (location.hash) {
    const id = parseInt(location.hash.slice(1), 10);
    if (id) selectNote(id);
  }
}

async function loadProfiles() {
  const profiles = await API.listProfiles();
  profileSel.innerHTML = '<option value="">— nenhum —</option>' +
    profiles.map(p => `<option value="${p.id}">${p.name}</option>`).join("");
  const { profile } = await ProfileState.resolveActive();
  activeProfileId = profile ? profile.id : null;
}

async function refreshList() {
  const useFilter = filterToggle.checked && activeProfileId != null;
  try {
    allNotes = await API.listNotes(useFilter ? activeProfileId : null);
    renderList();
  } catch (err) {
    console.error(err);
    list.innerHTML = empty("Não foi possível carregar", err.message);
    Toast.err(err.message);
  }
}

function renderList() {
  const q = searchInput.value.trim().toLowerCase();
  const filtered = q
    ? allNotes.filter(n =>
        n.title.toLowerCase().includes(q) ||
        n.content.toLowerCase().includes(q) ||
        [...n.tags].some(t => t.toLowerCase().includes(q)))
    : allNotes;

  countBadge.textContent = filtered.length ? `(${filtered.length})` : "";

  if (!filtered.length) {
    list.innerHTML = empty(
      q ? "Nenhum resultado" : "Nenhuma nota ainda",
      q ? "Tente outra busca" : "Crie sua primeira nota ao lado"
    );
    return;
  }

  list.innerHTML = filtered.map((n, i) => noteCard(n, i)).join("");
  list.querySelectorAll(".card-item").forEach(li => {
    li.addEventListener("click", e => {
      if (e.target.closest(".row-actions")) return;
      selectNote(parseInt(li.dataset.id, 10));
    });
  });
  // re-marcar selecionada
  if (selectedId) {
    const el = list.querySelector(`[data-id="${selectedId}"]`);
    if (el) el.classList.add("selected");
  }
}

function noteCard(n, i) {
  const chips = [...n.tags].slice(0, 5)
    .map(t => `<span class="chip tiny">${escapeHtml(t)}</span>`).join("");
  return `
  <li class="card-item" data-id="${n.id}" style="animation-delay:${i*40}ms">
    <h3>${escapeHtml(n.title)}</h3>
    <p>${escapeHtml(n.content)}</p>
    <div class="meta">${chips}<span class="muted" style="font-size:0.7rem; margin-left:auto">${timeAgo(n.updatedAt)}</span></div>
    <div class="row-actions">
      <button class="icon" title="Editar" onclick="editNote(${n.id})">${ICONS.edit}</button>
      <button class="icon" title="Excluir" onclick="removeNote(${n.id})">${ICONS.trash}</button>
    </div>
  </li>`;
}

async function selectNote(id) {
  selectedId = id;
  list.querySelectorAll(".card-item").forEach(li =>
    li.classList.toggle("selected", parseInt(li.dataset.id, 10) === id)
  );
  try {
    const related = await API.relatedNotes(id);
    if (!related.length) {
      relatedList.innerHTML = empty("Sem conexões", "Adicione tags compartilhadas a outras notas");
      return;
    }
    relatedList.innerHTML = related.map((n, i) => `
      <li class="card-item" data-id="${n.id}" style="animation-delay:${i*40}ms" onclick="selectNote(${n.id})">
        <h3>${escapeHtml(n.title)}</h3>
        <p>${escapeHtml(n.content)}</p>
        <div class="meta">${[...n.tags].slice(0,4).map(t => `<span class="chip tiny alt">${escapeHtml(t)}</span>`).join("")}</div>
      </li>`).join("");
  } catch {}
}

window.editNote = async function(id) {
  const n = await API.getNote(id);
  idInput.value = n.id;
  titleInput.value = n.title;
  contentInput.value = n.content;
  tagsInput.value = [...n.tags].join(", ");
  profileSel.value = n.focusProfileId || "";
  setFormTitle("Editar nota");
  document.querySelector(".panel").scrollIntoView({ behavior: "smooth", block: "start" });
};

window.removeNote = async function(id) {
  const ok = await confirmModal("Excluir nota?", "Esta ação não pode ser desfeita.");
  if (!ok) return;
  await API.deleteNote(id);
  Toast.ok("Nota excluída");
  if (selectedId === id) { selectedId = null; relatedList.innerHTML = ""; }
  resetForm();
  refreshList();
};

form.addEventListener("submit", async e => {
  e.preventDefault();
  const dto = {
    title: titleInput.value.trim(),
    content: contentInput.value.trim(),
    tags: tagsInput.value.split(",").map(s => s.trim()).filter(Boolean),
    focusProfileId: profileSel.value ? parseInt(profileSel.value, 10) : null,
  };
  try {
    if (idInput.value) {
      await API.updateNote(parseInt(idInput.value, 10), dto);
      Toast.ok("Nota atualizada");
    } else {
      await API.createNote(dto);
      Toast.ok("Nota criada");
    }
    resetForm();
    refreshList();
  } catch (err) {
    console.error(err);
    Toast.err("Falha ao salvar: " + err.message);
  }
});

resetBtn.addEventListener("click", resetForm);
filterToggle.addEventListener("change", refreshList);
searchInput.addEventListener("input", renderList);
document.addEventListener("profile-changed", () => loadProfiles().then(refreshList));

function resetForm() { idInput.value = ""; form.reset(); setFormTitle("Nova nota"); }
function empty(t, m) { return `<div class="empty">${ICONS.layers}<p><strong>${t}</strong></p><p>${m}</p></div>`; }

bootstrap();
