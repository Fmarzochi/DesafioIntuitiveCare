<template>
  <div>
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-success" role="status"></div>
    </div>

    <div v-else>
      <div class="row mb-4 g-4">
        <div class="col-md-6">
          <div class="card shadow-sm border-0 h-100 border-top border-success border-3">
            <div class="card-body text-center p-4">
              <h6 class="text-uppercase text-muted small mb-3">Total de Despesas (2025)</h6>
              <h2 class="display-5 fw-bold text-dark">{{ formatarMoeda(stats.total_despesas) }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-6">
          <div class="card shadow-sm border-0 h-100 border-top border-danger border-3">
            <div class="card-body text-center p-4">
              <h6 class="text-uppercase text-muted small mb-3">Operadoras Acima da Média (2+ TRI)</h6>
              <h2 class="display-5 fw-bold text-danger">{{ stats.ops_acima_media }}</h2>
              <small class="text-muted">Empresas com alto custo</small>
            </div>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-md-6">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-body p-4">
              <h5 class="card-title mb-4">Por UF</h5>
              <div style="height: 300px;">
                <Pie v-if="chartDataPie.labels" :data="chartDataPie" :options="chartOptions" />
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-body p-4">
              <h5 class="card-title mb-4">Top 5 Custos</h5>
              <div style="height: 300px;">
                <Bar v-if="chartDataBar.labels" :data="chartDataBar" :options="chartOptionsBar" />
              </div>
            </div>
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
      stats: {},
      chartDataBar: {},
      chartDataPie: {},
      chartOptions: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'top' } }
      },
      chartOptionsBar: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } }
      }
    }
  },
  async created() {
    try {
      const response = await api.get('/estatisticas');
      this.stats = response.data;

      this.chartDataBar = {
        labels: this.stats.top_5_operadoras.map(op => op.razaoSocial.substring(0, 15) + '...'),
        datasets: [{
          label: 'R$',
          backgroundColor: '#10b981',
          borderRadius: 4,
          data: this.stats.top_5_operadoras.map(op => op.totalDespesas)
        }]
      };

      this.chartDataPie = {
        labels: this.stats.despesas_por_uf.slice(0, 5).map(u => u.uf),
        datasets: [{
          backgroundColor: ['#1f2937', '#10b981', '#3b82f6', '#ef4444', '#f59e0b'],
          data: this.stats.despesas_por_uf.slice(0, 5).map(u => u.total),
          borderWidth: 1
        }]
      };
    } catch (e) { console.error(e); } finally { this.loading = false; }
  },
  methods: {
    formatarMoeda(val) {
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val || 0);
    }
  }
}
</script>