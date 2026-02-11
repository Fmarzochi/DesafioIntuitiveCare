<template>
  <div class="app-wrapper">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark py-3 shadow-sm">
      <div class="container">
        <router-link to="/" class="navbar-brand fw-bold fs-4 text-decoration-none">
          Intuitive Care <span class="text-success">Desafio</span>
        </router-link>

        <div class="d-flex gap-2">
          <router-link to="/pesquisa" class="btn btn-outline-secondary btn-sm" active-class="active">
            Lista
          </router-link>

          <router-link to="/dashboard" class="btn btn-outline-secondary btn-sm" active-class="active">
            Dashboard
          </router-link>
        </div>
      </div>
    </nav>

    <main class="container py-5 flex-grow-1">
      <router-view />
    </main>

    <footer class="text-center py-4 mt-auto border-top bg-white">
      <div class="container">
        <small class="text-muted">&copy; 2026 — Desenvolvido por <span class="fw-bold">Felipe Marzochi</span>.</small>
      </div>
    </footer>
  </div>
</template>

<style>
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f8f9fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.text-success { color: #10b981 !important; }

.btn-outline-secondary.active {
  background-color: #343a40;
  color: white;
  border-color: #343a40;
}
</style>