// Modo Foco — CRUD de perfis + override manual
Layout.mount("focus");

const formTitle = document.getElementById("form-title");
function setFormTitle(text) { formTitle.innerHTML = ICONS.plus + ` <span>${text}</span>`; }
setFormTitle("Novo perfil");

document.getElementById("list-title").insertAdjacentHTML("afterbegin", ICONS.focus);
document.getElementById("save-btn").insertAdjacentHTML("afterbegin", ICONS.check);

const form     = document.getElementById("profile-form");
const idInput  = document.getElementById("profile-id");
const nameIn   = document.getElementById("profile-name");
const startIn  = document.getElementById("profile-start");
const endIn    = document.getElementById("profile-end");
const list     = document.getElementById("profiles-list");
const resetBtn = document.getElementById("profile-reset");

async function refresh() {
  try {
    const profiles = await API.listProfiles();
    const manual = ProfileState.getManual();
    let autoActiveId = null;
    try {
      const a = await API.activeProfile();
      autoActiveId = a ? a.id : null;
    } catch {}

    if (!profiles.length) {
      list.innerHTML = `<div class="empty">${ICONS.layers}<p><strong>Nenhum perfil cadastrado</strong></p><p>Crie seu primeiro contexto ao lado</p></div>`;
      return;
    }

    list.innerHTML = profiles.map((p, i) => {
      const isAuto   = autoActiveId === p.id && !manual;
      const isManual = manual === p.id;
      const isActive = isAuto || isManual;
      const badge = isManual
        ? `<span class="chip tiny">forçado</span>`
        : isAuto
          ? `<span class="chip tiny alt">ativo</span>`
          : "";
      return `
        <li class="card-item compact ${isActive ? "selected" : ""}" style="animation-delay:${i*40}ms">
          <div class="info">
            <span class="name">${escapeHtml(p.name)} ${badge}</span>
            <span class="time">${ICONS.clock} ${formatTime(p.startTime)} → ${formatTime(p.endTime)}</span>
          </div>
          <div class="actions">
            ${isManual
              ? `<button class="ghost" onclick="clearManual()">Desativar</button>`
              : `<button onclick="forceProfile(${p.id})">Forçar</button>`}
            <button class="icon" title="Editar" onclick='editProfile(${JSON.stringify(p)})'>${ICONS.edit}</button>
            <button class="icon" title="Excluir" onclick="removeProfile(${p.id})">${ICONS.trash}</button>
          </div>
        </li>`;
    }).join("");
    refreshActiveProfilePill();
  } catch (err) {
    console.error(err);
    list.innerHTML = `<div class="empty">${ICONS.layers}<p><strong>Falha ao carregar</strong></p><p>${escapeHtml(err.message)}</p></div>`;
    Toast.err(err.message);
  }
}

function formatTime(t) { return t ? t.slice(0, 5) : "--:--"; }

window.editProfile = function(p) {
  idInput.value = p.id;
  nameIn.value = p.name;
  startIn.value = (p.startTime || "").slice(0, 5);
  endIn.value = (p.endTime || "").slice(0, 5);
  setFormTitle("Editar perfil");
  document.querySelector(".panel").scrollIntoView({ behavior: "smooth" });
};

window.removeProfile = async function(id) {
  const ok = await confirmModal("Excluir perfil?", "As notas vinculadas perderão a referência (não serão excluídas).");
  if (!ok) return;
  await API.deleteProfile(id);
  if (ProfileState.getManual() === id) ProfileState.setManual(null);
  Toast.ok("Perfil excluído");
  reset(); refresh();
};

window.forceProfile = function(id) {
  ProfileState.setManual(id);
  Toast.ok("Perfil forçado");
  refresh();
};

window.clearManual = function() {
  ProfileState.setManual(null);
  Toast.info("Override removido");
  refresh();
};

form.addEventListener("submit", async e => {
  e.preventDefault();
  const dto = { name: nameIn.value.trim(), startTime: startIn.value, endTime: endIn.value };
  try {
    if (idInput.value) {
      await API.updateProfile(parseInt(idInput.value, 10), dto);
      Toast.ok("Perfil atualizado");
    } else {
      await API.createProfile(dto);
      Toast.ok("Perfil criado");
    }
    reset();
    refresh();
  } catch (err) {
    console.error(err);
    Toast.err("Falha ao salvar perfil: " + err.message);
  }
});

resetBtn.addEventListener("click", reset);
function reset() { idInput.value = ""; form.reset(); setFormTitle("Novo perfil"); }

refresh();
