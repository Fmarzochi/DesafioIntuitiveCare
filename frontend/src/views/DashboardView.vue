<template>
  <div class="dashboard-view">
    <h1 class="mb-4 text-primary fw-bold">Painel de Controle</h1>

    <div v-if="loading" class="text-center py-5" aria-live="polite" aria-busy="true">
      <div class="spinner-border text-success" role="status">
        <span class="visually-hidden">Carregando estatísticas...</span>
      </div>
    </div>

    <div v-else>
      <section class="row mb-4 g-4" aria-label="Indicadores Chave de Desempenho">
        <div class="col-12 col-md-6">
          <div class="card shadow-sm border-0 h-100 border-top border-success border-3">
            <div class="card-body text-center p-4">
              <h2 class="h6 text-uppercase text-muted small mb-3">Total de Despesas (2025)</h2>
              <p class="display-5 fw-bold text-dark mb-0">{{ formatarMoeda(stats.total_despesas) }}</p>
            </div>
          </div>
        </div>
        <div class="col-12 col-md-6">
          <div class="card shadow-sm border-0 h-100 border-top border-danger border-3">
            <div class="card-body text-center p-4">
              <h2 class="h6 text-uppercase text-muted small mb-3">Operadoras Acima da Média (2+ TRI)</h2>
              <p class="display-5 fw-bold text-danger mb-0">{{ stats.ops_acima_media }}</p>
              <small class="text-muted">Empresas com alto custo recorrente</small>
            </div>
          </div>
        </div>
      </section>
      <section class="row g-4" aria-label="Gráficos Analíticos">
        <div class="col-12 col-md-6">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-white py-3">
              <h3 class="h5 fw-bold mb-0 text-dark">Top 5 Maiores Despesas</h3>
            </div>
            <div class="card-body">
              <p class="visually-hidden">
              </p>
              <div style="height: 300px; position: relative;">
                <Bar
                  v-if="chartDataBar.labels"
                  :data="chartDataBar"
                  :options="chartOptionsBar"
                  aria-label="Gráfico de barras: Top 5 Operadoras por Despesa"
                  role="img"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="col-12 col-md-6">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-white py-3">
              <h3 class="h5 fw-bold mb-0 text-dark">Despesas por Estado (Top 5)</h3>
            </div>
            <div class="card-body">
              <p class="visually-hidden">
                Gráfico de pizza mostrando a distribuição das despesas entre os 5 estados com maior volume.
              </p>
              <div style="height: 300px; position: relative;">
                <Pie
                  v-if="chartDataPie.labels"
                  :data="chartDataPie"
                  :options="chartOptions"
                  aria-label="Gráfico de pizza: Distribuição de despesas por Estado"
                  role="img"
                />
              </div>
            </div>
          </div>
        </div>

      </section>
    </div>
  </div>
</template>

<script>
import api from '../services/api';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js'
import { Bar, Pie } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement);

export default {
  name: 'DashboardView',
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
        plugins: {
          legend: { position: 'top' },
          tooltip: {
            callbacks: {
              label: (context) => {
                let label = context.label || '';
                if (label) label += ': ';
                if (context.parsed !== null) {
                  label += new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(context.parsed);
                }
                return label;
              }
            }
          }
        }
      },
      chartOptionsBar: {
        indexAxis: 'y', // Barra Horizontal (melhor para ler nomes longos no celular)
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }, // Legenda desnecessária para barra única
          tooltip: {
            callbacks: {
              label: (context) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(context.raw)
            }
          }
        }
      }
    }
  },
  async created() {
    try {
      const response = await api.get('/estatisticas');
      this.stats = response.data;
      this.chartDataBar = {
        labels: this.stats.top_5_operadoras.map(op => {
          // Trunca nomes muito longos para não quebrar layout mobile
          return op.razaoSocial.length > 20 ? op.razaoSocial.substring(0, 20) + '...' : op.razaoSocial
        }),
        datasets: [{
          label: 'Total R$',
          backgroundColor: '#198754', // Bootstrap Success
          borderRadius: 4,
          data: this.stats.top_5_operadoras.map(op => op.totalDespesas)
        }]
      };

      this.chartDataPie = {
        labels: this.stats.despesas_por_uf.slice(0, 5).map(u => u.uf),
        datasets: [{
          backgroundColor: ['#212529', '#198754', '#0d6efd', '#dc3545', '#ffc107'],
          data: this.stats.despesas_por_uf.slice(0, 5).map(u => u.total)
        }]
      };

    } catch (e) {
      console.error("Erro dashboard:", e);
    } finally {
      this.loading = false;
    }
  },
  methods: {
    formatarMoeda(val) {
      if (!val) return 'R$ 0,00';
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
    }
  }
}
</script>

<style scoped>
.spinner-border {
  width: 3rem;
  height: 3rem;
}
</style>