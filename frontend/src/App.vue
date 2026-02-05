<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-wrapper">
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
      <div class="container">
        <a class="navbar-brand fw-bold" href="/">Intuitive Care Challenge</a>
      </div>
    </nav>

    <main class="main-content">
      <RouterView />
    </main>

    <footer class="footer mt-auto py-3 bg-light border-top">
      <div class="container text-center">
        <span class="text-muted small">
          &copy; 2026 - Projeto desenvolvido por <strong>Felipe Marzochi</strong>. Todos os direitos reservados.
        </span>
      </div>
    </footer>
  </div>
</template>

<style>
/* Força o rodapé a ficar no chão mesmo se tiver pouco conteúdo */
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.main-content {
  flex: 1;
}
</style>