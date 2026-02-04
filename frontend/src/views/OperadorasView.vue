<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Operadoras de Saúde</h2>
      <router-link to="/dashboard" class="btn btn-primary">Ver Dashboard Financeiro</router-link>
    </div>

    <div class="row mb-3">
      <div class="col-md-6">
        <div class="input-group">
          <input
            v-model="search"
            @keyup.enter="handleSearch"
            type="text"
            class="form-control"
            placeholder="Buscar por Razão Social..."
          />
          <button @click="handleSearch" class="btn btn-outline-secondary" type="button">Buscar</button>
        </div>
      </div>
    </div>

    <div class="table-responsive card shadow-sm p-3">
      <table class="table table-hover">
        <thead class="table-light">
          <tr>
            <th>Registro ANS</th>
            <th>CNPJ</th>
            <th>Razão Social</th>
            <th>Modalidade</th>
            <th>UF</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center py-4">
              <div class="spinner-border text-primary" role="status"></div>
            </td>
          </tr>
          <tr v-for="op in operadoras" :key="op.registroAns" v-else>
            <td>{{ op.registroAns }}</td>
            <td>{{ op.cnpj }}</td>
            <td>{{ op.razaoSocial }}</td>
            <td>{{ op.modalidade }}</td>
            <td>{{ op.uf }}</td>
          </tr>
          <tr v-if="!loading && operadoras.length === 0">
            <td colspan="5" class="text-center py-4">Nenhum dado encontrado.</td>
          </tr>
        </tbody>
      </table>

      <div class="d-flex justify-content-between mt-3">
        <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">Anterior</button>
        <span>Página {{ currentPage + 1 }} de {{ totalPages }}</span>
        <button class="btn btn-outline-secondary btn-sm" :disabled="currentPage + 1 >= totalPages" @click="changePage(currentPage + 1)">Próxima</button>
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
      search: '',
      loading: false,
      currentPage: 0,
      totalPages: 0,
      pageSize: 10
    }
  },
  methods: {
    async fetchOperadoras() {
      this.loading = true;
      try {
        const response = await api.get('/operadoras', {
          params: { search: this.search, page: this.currentPage, limit: this.pageSize }
        });
        this.operadoras = response.data.content;
        this.totalPages = response.data.totalPages;
      } catch (error) {
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    changePage(page) {
      this.currentPage = page;
      this.fetchOperadoras();
    },
    handleSearch() {
      this.currentPage = 0;
      this.fetchOperadoras();
    }
  },
  mounted() {
    this.fetchOperadoras();
  }
}
</script>