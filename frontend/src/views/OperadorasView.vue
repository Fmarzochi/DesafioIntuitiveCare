<template>
  <div>
    <div class="card shadow-sm mb-4 border-0">
      <div class="card-body p-4">
        <form @submit.prevent="buscarNova" class="d-flex gap-3">
          <input
            v-model="termoBusca"
            type="text"
            class="form-control form-control-lg"
            placeholder="Digite: Nome, Registro ANS ou CNPJ..."
          />
          <button type="submit" class="btn btn-success btn-lg px-4 fw-bold">
            Buscar
          </button>
        </form>
      </div>
    </div>

    <div class="card shadow-sm border-0">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-success" role="status"></div>
      </div>

      <div v-else>
        <div v-if="operadoras.length === 0" class="text-center py-5 text-muted">
          Nenhum resultado encontrado para "{{ termoBusca }}".
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="bg-light text-uppercase small text-muted">
              <tr>
                <th class="ps-4 py-3">Registro</th>
                <th>CNPJ</th>
                <th>Razão Social</th>
                <th>UF</th>
                <th class="text-end pe-4">Ação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in operadoras" :key="op.registroAns">
                <td class="ps-4 fw-bold text-primary">{{ op.registroAns }}</td>

                <td style="font-family: monospace; font-size: 0.95rem;">
                  {{ formatarCNPJ(op.cnpj) }}
                </td>

                <td class="text-truncate" style="max-width: 350px;">{{ op.razaoSocial }}</td>
                <td><span class="badge bg-light text-dark border">{{ op.uf }}</span></td>
                <td class="text-end pe-4">
                  <router-link :to="`/operadoras/${op.registroAns}`" class="btn btn-sm btn-outline-primary">
                    Detalhes
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card-footer bg-white py-3">
        <div class="d-flex justify-content-center gap-3 align-items-center">
          <button class="btn btn-outline-secondary btn-sm" @click="mudarPagina(-1)" :disabled="paginaAtual === 0">
            Anterior
          </button>

          <span class="fw-bold text-muted">
            Página {{ paginaAtual + 1 }} de {{ totalPaginas }}
          </span>

          <button class="btn btn-outline-secondary btn-sm" @click="mudarPagina(1)" :disabled="paginaAtual >= totalPaginas - 1">
            Próxima
          </button>
        </div>
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
      paginaAtual: 0,
      totalPaginas: 1
    }
  },
  created() { this.carregarOperadoras(); },
  methods: {
    async carregarOperadoras() {
      this.loading = true;
      try {
        const response = await api.get('/operadoras', {
          params: { page: this.paginaAtual, limit: 10, search: this.termoBusca }
        });

        this.operadoras = response.data.content || [];
        this.totalPaginas = response.data.totalPages || 1;

      } catch (e) {
        console.error("Erro ao carregar:", e);
        this.operadoras = [];
      } finally { this.loading = false; }
    },
    buscarNova() {
      this.paginaAtual = 0;
      this.carregarOperadoras();
    },
    mudarPagina(delta) {
      this.paginaAtual += delta;
      this.carregarOperadoras();
    },
    formatarCNPJ(v) {
      if (!v) return '';
      v = v.replace(/\D/g, '');
      return v.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
    }
  }
}
</script>