// Flashcards — sorteio aleatório com flip
Layout.mount("flashcards");

document.getElementById("review-title").insertAdjacentHTML("afterbegin", ICONS.shuffle);
document.getElementById("form-title").insertAdjacentHTML("afterbegin", ICONS.plus);
document.getElementById("list-title").insertAdjacentHTML("afterbegin", ICONS.cards);
document.getElementById("draw-btn").insertAdjacentHTML("afterbegin", ICONS.shuffle);
document.getElementById("flip-btn").insertAdjacentHTML("afterbegin", ICONS.rotate);
document.getElementById("create-btn").insertAdjacentHTML("afterbegin", ICONS.check);

const cardForm   = document.getElementById("card-form");
const qInput     = document.getElementById("card-q");
const aInput     = document.getElementById("card-a");
const noteSel    = document.getElementById("card-note");
const profileSel = document.getElementById("card-profile");
const list       = document.getElementById("cards-list");
const countBadge = document.getElementById("cards-count");
const drawBtn    = document.getElementById("draw-btn");
const flipBtn    = document.getElementById("flip-btn");
const drawActive = document.getElementById("draw-active-only");
const flashcard  = document.getElementById("flashcard");
const qText      = document.getElementById("card-q-text");
const aText      = document.getElementById("card-a-text");

let activeProfileId = null;
let currentCard = null;

async function bootstrap() {
  try {
    const [notes, profiles] = await Promise.all([API.listNotes(), API.listProfiles()]);
    noteSel.innerHTML = '<option value="">— nenhuma —</option>' +
      notes.map(n => `<option value="${n.id}">${escapeHtml(n.title)}</option>`).join("");
    profileSel.innerHTML = '<option value="">— nenhum —</option>' +
      profiles.map(p => `<option value="${p.id}">${p.name}</option>`).join("");
    const { profile } = await ProfileState.resolveActive();
    activeProfileId = profile ? profile.id : null;
  } catch {}
  refreshList();
}

async function refreshList() {
  try {
    const cards = await API.listCards();
    countBadge.textContent = cards.length ? `(${cards.length})` : "";
    if (!cards.length) {
      list.innerHTML = empty("Nenhum flashcard ainda", "Crie o primeiro ao lado");
      return;
    }
    list.innerHTML = cards.map((c, i) => `
      <li class="card-item" style="animation-delay:${i*40}ms">
        <h3>${escapeHtml(c.question)}</h3>
        <p>${escapeHtml(c.answer)}</p>
        <div class="row-actions">
          <button class="icon" onclick="removeCard(${c.id})" title="Excluir">${ICONS.trash}</button>
        </div>
      </li>`).join("");
  } catch {
    list.innerHTML = empty("Falha ao carregar", "Verifique o backend");
  }
}

window.removeCard = async function(id) {
  const ok = await confirmModal("Excluir flashcard?", "Esta ação não pode ser desfeita.");
  if (!ok) return;
  await API.deleteCard(id);
  Toast.ok("Flashcard excluído");
  refreshList();
};

cardForm.addEventListener("submit", async e => {
  e.preventDefault();
  try {
    await API.createCard({
      question: qInput.value.trim(),
      answer: aInput.value.trim(),
      noteId: noteSel.value ? parseInt(noteSel.value, 10) : null,
      focusProfileId: profileSel.value ? parseInt(profileSel.value, 10) : null,
    });
    cardForm.reset();
    Toast.ok("Flashcard criado");
    refreshList();
  } catch (err) {
    console.error(err);
    Toast.err("Falha ao criar: " + err.message);
  }
});

drawBtn.addEventListener("click", async () => {
  const filter = drawActive.checked && activeProfileId != null ? activeProfileId : null;
  try {
    const card = await API.drawCard(filter);
    if (!card) {
      Toast.info("Nenhum flashcard disponível");
      return;
    }
    currentCard = card;
    flashcard.classList.remove("flipped");
    setTimeout(() => {
      qText.textContent = card.question;
      aText.textContent = card.answer;
    }, 100);
  } catch (err) {
    console.error(err);
    Toast.err("Falha ao sortear: " + err.message);
  }
});

function flip() {
  if (!currentCard) {
    Toast.info("Sorteie um flashcard primeiro");
    return;
  }
  flashcard.classList.toggle("flipped");
}
flipBtn.addEventListener("click", flip);
flashcard.addEventListener("click", flip);

function empty(t, m) { return `<div class="empty">${ICONS.layers}<p><strong>${t}</strong></p><p>${m}</p></div>`; }

bootstrap();
