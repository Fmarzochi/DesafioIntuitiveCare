<template>
  <div class="container py-4">
    <button @click="$router.back()" class="btn btn-outline-secondary mb-4">
      ⬅ Voltar
    </button>

    <div v-if="carregando" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2 text-muted">Carregando histórico...</p>
    </div>

    <div v-else-if="operadora" class="animate__animated animate__fadeIn">

      <div class="card shadow-sm border-0 mb-4 bg-light">
        <div class="card-body">
          <h2 class="text-primary fw-bold">{{ formatarTexto(operadora.razaoSocial) }}</h2>

          <div class="row mt-3">
            <div class="col-md-3"><strong>Registro ANS:</strong> {{ operadora.registroAns }}</div>
            <div class="col-md-3"><strong>CNPJ:</strong> {{ formatarCNPJ(operadora.cnpj) }}</div>
            <div class="col-md-3"><strong>UF:</strong> {{ operadora.uf }}</div>
            <div class="col-md-3"><strong>Modalidade:</strong> {{ operadora.modalidade }}</div>
          </div>
        </div>
      </div>

      <div class="card shadow-sm border-0">
        <div class="card-header bg-white fw-bold">
          📊 Histórico de Despesas (Ordenado por Data)
        </div>
        <div class="table-responsive">
          <table class="table table-hover mb-0">
            <thead class="table-light">
              <tr>
                <th>Data do Evento</th>
                <th>Descrição</th>
                <th class="text-end">Valor (R$)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="despesas.length === 0">
                <td colspan="3" class="text-center py-4 text-muted">Nenhum registro encontrado.</td>
              </tr>

              <tr v-for="item in despesas" :key="item.id">
                <td>{{ formatarData(item.dataEvento) }}</td>

                <td>{{ formatarTexto(item.descricao) }}</td>

                <td class="text-end fw-bold text-success">{{ formatarMoeda(item.valor) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// Importações
import api from '../services/api';
import { formatarTexto } from '../utils/formatters';

export default {
  name: 'OperadoraDetalhes',
  // Recebe o ID vindo da rota (configurado no router)
  props: ['registroAns'],

  data() {
    return {
      operadora: null,
      despesas: [],
      carregando: true
    };
  },

  async created() {
    await this.carregarDados();
  },

  methods: {
    // Disponibiliza a função de formatação para o HTML
    formatarTexto(texto) {
      return formatarTexto(texto);
    },

    async carregarDados() {
      try {
        // SEGURANÇA: Tenta pegar o ID via props OU via parâmetros da rota (garantia dupla)
        const id = this.registroAns || this.$route.params.registroAns;

        if (!id) {
            alert("Erro: ID da operadora não encontrado.");
            this.carregando = false;
            return;
        }

        // Busca os detalhes da operadora usando o ID seguro
        const respOp = await api.get(`/operadoras/${id}`);
        this.operadora = respOp.data;

        // Busca as despesas
        const respDesp = await api.get(`/operadoras/${id}/despesas`);
        this.despesas = respDesp.data;

      } catch (error) {
        console.error("Erro ao carregar detalhes", error);
        alert("Erro ao buscar dados. O servidor Backend está rodando?");
      } finally {
        this.carregando = false;
      }
    },

    formatarMoeda(val) {
      if (val === null || val === undefined) return 'R$ 0,00';
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
    },

    formatarData(data) {
      if (!data) return '-';
      const dataObj = new Date(data);
      return dataObj.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
    },

    formatarCNPJ(cnpj) {
      if (!cnpj) return '';
      return cnpj.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
    }
  }
};
</script>

<style scoped>
.table th {
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}
</style>