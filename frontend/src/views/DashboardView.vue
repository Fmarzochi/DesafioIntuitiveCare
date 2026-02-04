<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Dashboard Financeiro</h2>
      <router-link to="/" class="btn btn-outline-secondary">Voltar para a Lista</router-link>
    </div>

    <div class="row mb-4">
      <div class="col-12">
        <div class="card bg-primary text-white text-center p-3 shadow-sm border-0">
          <h6 class="text-uppercase small">Total Geral de Despesas (Trimestre)</h6>
          <h2 v-if="!loading" class="fw-bold">{{ formatMoeda(stats.total_despesas) }}</h2>
          <div v-else class="spinner-border spinner-border-sm" role="status"></div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-7 mb-4">
        <div class="card shadow-sm p-3 h-100 border-0">
          <h5>Despesas por Estado (UF)</h5>
          <div style="height: 300px;">
            <Bar v-if="!loading && chartUfData.labels.length > 0" :data="chartUfData" :options="chartOptions" />
          </div>
        </div>
      </div>

      <div class="col-md-5 mb-4">
        <div class="card shadow-sm p-3 h-100 border-0">
          <h5>Top 5 Operadoras</h5>
          <div style="height: 300px;">
            <Pie v-if="!loading && chartTopData.labels.length > 0" :data="chartTopData" :options="chartOptions" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api';
import { Bar, Pie } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement);

export default {
  components: { Bar, Pie },
  data() {
    return {
      loading: true,
      stats: { total_despesas: 0, top_5_operadoras: [], despesas_por_uf: [] },
      chartUfData: { labels: [], datasets: [] },
      chartTopData: { labels: [], datasets: [] },
      chartOptions: { responsive: true, maintainAspectRatio: false }
    }
  },
  methods: {
    formatMoeda(valor) {
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
    },
    async loadStats() {
      this.loading = true;
      try {
        const response = await api.get('/estatisticas');
        this.stats = response.data;

        // Gráfico por UF
        this.chartUfData = {
          labels: this.stats.despesas_por_uf.map(i => i.uf),
          datasets: [{ label: 'R$', backgroundColor: '#0d6efd', data: this.stats.despesas_por_uf.map(i => i.total) }]
        };

        // Gráfico Top 5
        this.chartTopData = {
          labels: this.stats.top_5_operadoras.map(i => i.razaoSocial.substring(0, 15)),
          datasets: [{ backgroundColor: ['#0d6efd', '#6610f2', '#6f42c1', '#d63384', '#dc3545'], data: this.stats.top_5_operadoras.map(i => i.totalDespesas) }]
        };
      } catch (e) { console.error(e); } finally { this.loading = false; }
    }
  },
  mounted() { this.loadStats(); }
}
</script>