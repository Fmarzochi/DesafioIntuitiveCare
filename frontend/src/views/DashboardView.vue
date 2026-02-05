<template>
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="text-primary">Dashboard Analitico</h1>
      <router-link to="/" class="btn btn-outline-primary">Voltar para Lista</router-link>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2 text-muted">Eu estou carregando os dados estatisticos...</p>
    </div>

    <div v-else>
      <div class="row mb-4">
        <div class="col-md-12">
          <div class="card bg-primary text-white shadow-sm">
            <div class="card-body text-center">
              <h5 class="card-title">Total de Despesas Processadas (Ultimo Trimestre)</h5>
              <h2 class="display-4 fw-bold">
                {{ formatMoney(stats.total_despesas) }}
              </h2>
              <p class="card-text">Soma total dos eventos indenizaveis importados</p>
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-md-8 mb-4">
          <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-bold">Top 5 - Maiores Despesas por Operadora</div>
            <div class="card-body">
              <Bar v-if="chartDataBar" :data="chartDataBar" :options="chartOptionsBar" />
            </div>
          </div>
        </div>

        <div class="col-md-4 mb-4">
          <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-bold">Distribuicao por Estado</div>
            <div class="card-body d-flex align-items-center justify-content-center">
              <div style="height: 300px; width: 100%">
                <Doughnut v-if="chartDataPie" :data="chartDataPie" :options="chartOptionsPie" />
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
import { Bar, Doughnut } from 'vue-chartjs'

// Eu registro os componentes necessarios do Chart.js
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

export default {
  components: { Bar, Doughnut },
  data() {
    return {
      loading: true,
      stats: { total_despesas: 0 },
      chartDataBar: null,
      chartDataPie: null,
      // Opcoes para deixar o grafico responsivo
      chartOptionsBar: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false }
        }
      },
      chartOptionsPie: {
        responsive: true,
        maintainAspectRatio: false
      }
    }
  },
  async created() {
    await this.carregarEstatisticas();
  },
  methods: {
    formatMoney(val) {
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val || 0);
    },
    async carregarEstatisticas() {
      try {
        // Eu chamo a API que calculou os dados no backend
        const response = await api.get('/estatisticas');
        const data = response.data;
        this.stats = data;

        // Eu monto o Grafico de Barras (Top 5)
        if (data.top_5_operadoras && data.top_5_operadoras.length > 0) {
          this.chartDataBar = {
            labels: data.top_5_operadoras.map(op => {
                // Eu trunco o nome se for muito grande para nao quebrar o grafico
                return op.razaoSocial.length > 15 ? op.razaoSocial.substring(0, 15) + '...' : op.razaoSocial;
            }),
            datasets: [{
              label: 'Total de Despesas',
              backgroundColor: '#0d6efd',
              data: data.top_5_operadoras.map(op => op.totalDespesas)
            }]
          };
        }

        // Eu monto o Grafico de Pizza (Por UF)
        if (data.despesas_por_uf && data.despesas_por_uf.length > 0) {
          // Eu pego apenas os 5 maiores estados para o grafico nao ficar poluido
          const topUfs = data.despesas_por_uf.slice(0, 5);
          this.chartDataPie = {
            labels: topUfs.map(u => u.uf),
            datasets: [{
              backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16', '#ffc107'],
              data: topUfs.map(u => u.total)
            }]
          };
        }

      } catch (error) {
        console.error("Erro ao carregar dashboard", error);
        // Nao exibo alert para nao travar a UX, apenas logo o erro
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>