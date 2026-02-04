<template>
  <div id="app">
    <nav class="navbar navbar-dark bg-dark mb-4">
      <div class="container">
        <span class="navbar-brand">Dashboard Intuitive Care</span>
      </div>
    </nav>

    <div class="container">
      <router-view />
    </div>
  </div>
</template>

<style>
/* Estilo simples para o corpo do site */
body {
  background-color: #f4f7f6;
  margin: 0;
  font-family: sans-serif;
}
</style>