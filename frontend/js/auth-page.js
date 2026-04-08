// Lógica das páginas login.html e register.html.
// Detecta qual formulário existe e liga o handler.

(function () {
  // Logo na página de auth (mesma SVG usada no header das outras páginas)
  const mark = document.getElementById("auth-mark");
  if (mark) {
    mark.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="12" x2="5.5" y2="5.5"/><line x1="12" y1="12" x2="18.5" y2="5.5"/><line x1="12" y1="12" x2="5.5" y2="18.5"/><line x1="12" y1="12" x2="18.5" y2="18.5"/><circle cx="12" cy="12" r="2.6" fill="currentColor" stroke="none"/><circle cx="5.5" cy="5.5" r="1.8" fill="currentColor" stroke="none"/><circle cx="18.5" cy="5.5" r="1.8" fill="currentColor" stroke="none"/><circle cx="5.5" cy="18.5" r="1.8" fill="currentColor" stroke="none"/><circle cx="18.5" cy="18.5" r="1.8" fill="currentColor" stroke="none"/></svg>';
  }

  // Se há um token salvo, valida-o com /auth/me ANTES de redirecionar.
  // Cenário comum no Render free tier: usuário tem token válido sintaticamente
  // mas o backend reiniciou e perdeu os usuários (cold start zerou o H2). Nesse
  // caso, /auth/me retorna 401, limpamos o localStorage e ficamos no login.
  // Nota: _apiBase() já retorna /api, então o path concatenado é /api/auth/me.
  if (Auth.isAuthenticated()) {
    fetch(Auth._apiBase() + "/auth/me", {
      headers: { "Authorization": "Bearer " + Auth.getToken() }
    }).then(res => {
      if (res.ok) {
        window.location.href = "index.html";
      } else {
        Auth.clear();
      }
    }).catch(() => {
      // Backend offline ou erro de rede — limpa pra deixar o usuário tentar logar
      Auth.clear();
    });
    return;
  }

  // Login
  const loginForm = document.getElementById("login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("login-btn");
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      btn.disabled = true;
      btn.textContent = "Entrando…";
      try {
        const data = await Auth.login(email, password);
        Auth.saveSession(data);
        window.location.href = "index.html";
      } catch (err) {
        showError(err.message || "Falha ao entrar");
        btn.disabled = false;
        btn.textContent = "Entrar";
      }
    });

    const demoBtn = document.getElementById("demo-btn");
    if (demoBtn) {
      demoBtn.addEventListener("click", async () => {
        demoBtn.disabled = true;
        demoBtn.textContent = "Entrando como demo…";
        try {
          const data = await Auth.login("demo@mindflow.com", "demo123");
          Auth.saveSession(data);
          window.location.href = "index.html";
        } catch (err) {
          showError("Conta demo indisponível: " + err.message);
          demoBtn.disabled = false;
          demoBtn.textContent = "Entrar como demonstração";
        }
      });
    }
  }

  // Register
  const registerForm = document.getElementById("register-form");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("register-btn");
      const name = document.getElementById("name").value.trim();
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      btn.disabled = true;
      btn.textContent = "Criando…";
      try {
        const data = await Auth.register(name, email, password);
        Auth.saveSession(data);
        window.location.href = "index.html";
      } catch (err) {
        showError(err.message || "Falha ao criar conta");
        btn.disabled = false;
        btn.textContent = "Criar conta";
      }
    });
  }

  function showError(msg) {
    let el = document.getElementById("auth-error");
    if (!el) {
      el = document.createElement("div");
      el.id = "auth-error";
      el.className = "auth-error";
      const form = document.querySelector("form");
      form.parentNode.insertBefore(el, form);
    }
    el.textContent = msg;
    el.style.display = "block";
  }
})();
