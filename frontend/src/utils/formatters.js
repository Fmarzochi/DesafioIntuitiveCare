<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'

const isMenuOpen = ref(false)
const navContainer = ref(null) // Referência para monitorar a área do menu

const closeMenu = () => {
  isMenuOpen.value = false
}

// Lógica de fechamento automático aprimorada
const handleGlobalMouseMove = (event) => {
  if (isMenuOpen.value && window.innerWidth < 992) {
    // Se o mouse estiver fora da área da navegação, fecha o menu
    if (navContainer.value && !navContainer.value.contains(event.target)) {
      closeMenu()
    }
  }
}

// Fecha ao clicar fora (Mobile/Touch)
const handleClickOutside = (event) => {
  if (navContainer.value && !navContainer.value.contains(event.target) && isMenuOpen.value) {
    closeMenu()
  }
}

onMounted(() => {
  window.addEventListener('mousemove', handleGlobalMouseMove)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleGlobalMouseMove)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <a href="#main-content" class="skip-link btn btn-light btn-sm">Pular para o conteúdo principal</a>

  <header class="sticky-top shadow-sm" ref="navContainer">
    <nav class="navbar navbar-expand-lg navbar-dark bg-success py-2" aria-label="Navegação Principal">
      <div class="container">
        <RouterLink class="navbar-brand fw-bold" to="/" @click="closeMenu">
          Intuitive Care
        </RouterLink>

        <button
          class="navbar-toggler border-0 shadow-none"
          type="button"
          @click="isMenuOpen = !isMenuOpen"
          :aria-expanded="isMenuOpen"
          aria-label="Alternar navegação"
        >
          <span class="navbar-toggler-icon"></span>
        </button>

        <div
          class="collapse navbar-collapse"
          :class="{ 'show': isMenuOpen }"
          id="navbarNav"
        >
          <ul class="navbar-nav ms-auto align-items-center mt-3 mt-lg-0">
            <li class="nav-item">
              <RouterLink class="nav-link px-3" to="/" active-class="active" @click="closeMenu">Home</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink class="nav-link px-3" to="/operadoras" active-class="active" @click="closeMenu">Operadoras</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink class="nav-link px-3 ms-lg-2 dashboard-pill" to="/dashboard" active-class="active" @click="closeMenu">Dashboard</RouterLink>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </header>

  <main class="container py-5 flex-grow-1" id="main-content" tabindex="-1">
    <RouterView />
  </main>

  <footer class="bg-white border-top py-4">
    <div class="container text-center">
      <p class="mb-0 text-muted fs-6">
        Desenvolvido por Felipe Marzochi © - 2026
      </p>
    </div>
  </footer>
</template>

<style>
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f8f9fa;
  font-family: 'Segoe UI', system-ui, sans-serif;
}

.navbar-brand { font-size: 1.25rem; }

.nav-link {
  color: rgba(255, 255, 255, 0.85) !important;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.active {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.2);
}

/* Estilo Mobile Corrigido */
@media (max-width: 991.98px) {
  .navbar-collapse {
    position: absolute;
    top: 100%;
    left: 15px;
    right: 15px;
    background-color: #ffffff;
    padding: 1rem;
    border-radius: 12px;
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    margin-top: 10px;
    /* Força o fechamento visual quando não tem a classe 'show' */
    display: none;
  }

  .navbar-collapse.show {
    display: block !important;
    animation: fadeInDown 0.3s ease;
  }

  .nav-link {
    color: #333 !important;
    text-align: center;
    padding: 12px !important;
    margin: 4px 0;
  }

  .active {
    background-color: #f1f3f5;
    color: #198754 !important;
    font-weight: 600;
  }
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.skip-link {
  position: absolute;
  top: -100px;
  left: 10px;
  z-index: 2000;
  transition: top 0.3s;
}

.skip-link:focus { top: 10px; }
</style>