<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
      <h2 class="text-primary fw-bold">Operadoras de Saúde</h2>
      <router-link to="/dashboard" class="btn btn-primary shadow-sm">
        📊 Ver Dashboard Financeiro
      </router-link>
    </div>

    <div class="card shadow-sm mb-4 border-0">
      <div class="card-body">
        <form @submit.prevent="pesquisar" class="d-flex gap-2">
          <input
            v-model="termoBusca"
            type="text"
            class="form-control"
            placeholder="Digite o nome, registro ANS ou CNPJ..."
          />
          <button type="submit" class="btn btn-dark px-4">
            🔍 Buscar
          </button>
        </form>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <div v-else class="card shadow-sm border-0">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-3">Registro ANS</th>
              <th>CNPJ</th>
              <th>Razão Social</th>
              <th>Modalidade</th>
              <th class="text-center">UF</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="operadoras.length === 0">
              <td colspan="5" class="text-center py-4 text-muted">Nenhuma operadora encontrada.</td>
            </tr>
            <tr v-for="op in operadoras" :key="op.registroAns">
              <td class="ps-3 fw-bold text-secondary">{{ op.registroAns }}</td>
              <td>{{ op.cnpj }}</td>
              <td class="fw-semibold text-primary">{{ op.razaoSocial }}</td>
              <td><span class="badge bg-light text-dark border">{{ op.modalidade }}</span></td>
              <td class="text-center fw-bold">{{ op.uf }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card-footer bg-white d-flex justify-content-between py-3">
        <button class="btn btn-sm btn-outline-secondary" @click="mudarPagina(-1)" :disabled="paginaAtual === 0">Anterior</button>
        <span class="text-muted align-self-center">Página {{ paginaAtual + 1 }}</span>
        <button class="btn btn-sm btn-outline-secondary" @click="mudarPagina(1)">Próxima</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api';

export default {
  data() {
    return {
      operadoras: [],
      loading: true,
      termoBusca: '',
      paginaAtual: 0
    }
  },
  created() {
    this.carregarOperadoras();
  },
  methods: {
    async carregarOperadoras() {
      this.loading = true;
      try {
        const params = {
          page: this.paginaAtual,
          limit: 10,
          search: this.termoBusca // Agora envia o termo para o Java
        };
        const response = await api.get('/operadoras', { params });
        this.operadoras = response.data.content || response.data;
      } catch (error) {
        console.error("Erro ao listar", error);
      } finally {
        this.loading = false;
      }
    },
    pesquisar() {
      this.paginaAtual = 0;
      this.carregarOperadoras();
    },
    mudarPagina(delta) {
      this.paginaAtual += delta;
      this.carregarOperadoras();
    }
  }
}
</script>