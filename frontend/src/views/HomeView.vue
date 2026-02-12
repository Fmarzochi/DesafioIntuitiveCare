<template>
  <div class="container d-flex flex-column justify-content-center align-items-center py-5" style="min-height: 80vh;">

    <div class="text-center mb-5">
      <h1 class="display-4 fw-bold text-dark">
        Intuitive Care <span class="text-success">Desafio</span>
      </h1>
      <p class="lead text-muted fs-4">Selecione uma ferramenta para começar</p>
    </div>

    <div class="row g-4 w-100 justify-content-center">

      <div class="col-12 col-md-5 col-lg-4">
        <div class="card h-100 shadow border-0 hover-card">
          <div class="card-body text-center p-5">
            <div class="mb-4 display-1" aria-hidden="true">🔍</div>
            <h3 class="card-title fw-bold text-success mb-3">Pesquisar</h3>
            <p class="card-text text-muted mb-4">
              Consulte a base de dados por Razão Social, Registro ANS ou CNPJ.
            </p>
            <router-link to="/operadoras" class="btn btn-success btn-lg w-100 fw-bold">
              Ir para Pesquisa
            </router-link>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-5 col-lg-4">
        <div class="card h-100 shadow border-0 hover-card">
          <div class="card-body text-center p-5">
            <div class="mb-4 display-1" aria-hidden="true">📊</div>
            <h3 class="card-title fw-bold text-primary mb-3">Dashboard</h3>
            <p class="card-text text-muted mb-4">
              Visualize gráficos de despesas, KPIs e análise por estado (UF).
            </p>
            <router-link to="/dashboard" class="btn btn-primary btn-lg w-100 fw-bold">
              Abrir Dashboard
            </router-link>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* Animação original */
.hover-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.hover-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 1rem 3rem rgba(0,0,0,.15)!important;
}
</style>