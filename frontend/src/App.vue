<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, BarElement, CategoryScale, LinearScale } from 'chart.js';
import { Pie, Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend);

const abaAtual = ref('lista');
const operadoras = ref([]);
const termoBusca = ref("");
const paginaAtual = ref(1);
const totalPaginas = ref(1);
const operadoraSelecionada = ref(null);
const historicoDespesas = ref([]);
const exibindoDetalhes = ref(false);
const valorTotalGeral = ref(0);
const dadosPizza = ref({ labels: [], datasets: [] });
const dadosBarras = ref({ labels: [], datasets: [] });

const carregarDashboard = async () => {
  try {
    const res = await axios.get('http://127.0.0.1:8000/dashboard/uf');
    valorTotalGeral.value = res.data.total_geral;
    dadosBarras.value = { labels: res.data.labels, datasets: [{ label: 'R$', backgroundColor: '#10b981', data: res.data.datasets }] };
    dadosPizza.value = { labels: res.data.labels.slice(0, 5), datasets: [{ backgroundColor: ['#1e293b', '#10b981', '#3498db', '#e11d48', '#f1c40f'], data: res.data.datasets.slice(0, 5) }] };
  } catch (e) { console.error(e); }
};

const buscarOperadoras = async (p) => {
  try {
    const res = await axios.get('http://127.0.0.1:8000/operadoras', { params: { page: p, search: termoBusca.value } });
    operadoras.value = res.data.data;
    paginaAtual.value = res.data.meta.current_page;
    totalPaginas.value = res.data.meta.total_pages;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) { console.error(e); }
};

const abrirDetalhes = async (reg) => {
  try {
    const res = await axios.get(`http://127.0.0.1:8000/operadoras/${reg}/despesas`);
    operadoraSelecionada.value = res.data;
    historicoDespesas.value = res.data.historico;
    exibindoDetalhes.value = true;
  } catch (e) { alert("Sem dados."); }
};

onMounted(() => { buscarOperadoras(1); carregarDashboard(); });
</script>

<template>
  <div class="app-container">
    <header class="main-header">
      <div class="container header-content">
        <div class="logo">
          <h1>Intuitive Care <span>Desafio</span></h1>
        </div>
        <nav class="main-nav">
          <button @click="abaAtual = 'lista'" :class="{ active: abaAtual === 'lista' }">Lista</button>
          <button @click="abaAtual = 'dashboard'" :class="{ active: abaAtual === 'dashboard' }">Dashboard</button>
        </nav>
      </div>
    </header>

    <main class="container main-content">
      <div v-show="abaAtual === 'lista'">
        <div class="search-bar">
          <input v-model="termoBusca" @keyup.enter="buscarOperadoras(1)" placeholder="Nome ou Registro ANS..." />
          <button @click="buscarOperadoras(1)">Buscar</button>
        </div>

        <div class="table-container">
          <table class="desktop-table">
            <thead><tr><th>Registro</th><th>CNPJ</th><th>Razão Social</th><th>UF</th><th class="center">Ação</th></tr></thead>
            <tbody>
              <tr v-for="op in operadoras" :key="op.registro_ans">
                <td><span class="badge">{{ op.registro_ans }}</span></td>
                <td>{{ op.cnpj }}</td>
                <td><strong>{{ op.razao_social }}</strong></td>
                <td>{{ op.uf }}</td>
                <td class="center"><button class="btn-ver" @click="abrirDetalhes(op.registro_ans)">Ver</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination-container">
          <button class="btn-nav" :disabled="paginaAtual===1" @click="buscarOperadoras(paginaAtual-1)">Anterior</button>
          <span>Página {{ paginaAtual }} de {{ totalPaginas }}</span>
          <button class="btn-nav" :disabled="paginaAtual===totalPaginas" @click="buscarOperadoras(paginaAtual+1)">Próxima</button>
        </div>
      </div>

      <div v-show="abaAtual === 'dashboard'">
        <div class="kpi-card"><label>Total de Despesas Processadas</label><div class="value">{{ valorTotalGeral.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }}</div></div>
        <div class="charts-row">
          <div class="chart-card"><h3>Distribuição por UF</h3><div class="chart-wrapper"><Pie :data="dadosPizza" /></div></div>
          <div class="chart-card"><h3>Ranking Geral</h3><div class="chart-wrapper"><Bar :data="dadosBarras" :options="{ indexAxis: 'y', responsive: true, maintainAspectRatio: false }" /></div></div>
        </div>
      </div>

      <div v-if="exibindoDetalhes" class="modal-backdrop" @click.self="exibindoDetalhes=false">
        <div class="modal-box">
          <header class="modal-header">
            <div><h2>{{ operadoraSelecionada?.razao_social }}</h2><p>CNPJ: {{ operadoraSelecionada?.cnpj }}</p></div>
            <button class="btn-close" @click="exibindoDetalhes=false">&times;</button>
          </header>
          <div class="modal-body">
            <table class="modal-table">
              <thead><tr><th style="width: 100px;">Data</th><th>Descrição detalhada</th><th style="width: 140px;" class="right">Valor (R$)</th></tr></thead>
              <tbody>
                <tr v-for="(ev, i) in historicoDespesas" :key="i">
                  <td class="nowrap">{{ ev.data }}</td>
                  <td class="desc-text">{{ ev.descricao }}</td>
                  <td class="right bold nowrap" :class="{ red: ev.valor < 0 }">{{ ev.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>

    <footer class="main-footer">
      <div class="container"><p>&copy; 2026 — Desenvolvido por <strong>Felipe Marzochi</strong>. Todos os direitos reservados.</p></div>
    </footer>
  </div>
</template>

<style scoped>
:root { --primary: #1e293b; --accent: #10b981; --bg: #f8fafc; }
body { margin: 0; font-family: sans-serif; background: var(--bg); }
.app-container { min-height: 100vh; display: flex; flex-direction: column; }
.container { width: 100%; max-width: 1100px; margin: 0 auto; padding: 0 20px; }

/* NAVBAR E CORES ORIGINAIS */
.main-header { background: #1e293b; color: white; padding: 1rem 0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.logo h1 { font-size: 1.25rem; margin: 0; color: white; font-weight: 800; }
.logo h1 span { color: #10b981; font-weight: 400; }
.main-nav button { background: none; border: none; color: #94a3b8; padding: 8px 16px; cursor: pointer; font-weight: 600; }
.main-nav button.active { color: white; background: rgba(255,255,255,0.1); border-radius: 6px; }

/* BUSCA E TABELA */
.search-bar { display: flex; gap: 10px; margin: 30px 0; background: white; padding: 6px; border-radius: 12px; border: 1px solid #e2e8f0; }
.search-bar input { flex: 1; border: none; padding: 10px; outline: none; }
.search-bar button { background: #10b981; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: bold; cursor: pointer; }
.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.desktop-table { width: 100%; border-collapse: collapse; }
.desktop-table th, .desktop-table td { padding: 15px; text-align: left; border-bottom: 1px solid #f1f5f9; }
.btn-ver { background: #3498db; color: white; border: none; padding: 6px 16px; border-radius: 4px; cursor: pointer; }
.badge { background: #e0f2fe; color: #0369a1; padding: 4px 10px; border-radius: 6px; font-family: monospace; font-weight: bold; }

/* PAGINAÇÃO */
.pagination-container { display: flex; justify-content: space-between; align-items: center; margin: 40px 0; }
.btn-nav { background: #10b981; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; }

/* DASHBOARD */
.kpi-card { background: white; padding: 30px; border-radius: 16px; text-align: center; border-bottom: 4px solid var(--accent); margin-bottom: 30px; }
.kpi-card .value { font-size: 2.5rem; font-weight: 900; }
.charts-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 20px; }
.chart-card { background: white; padding: 20px; border-radius: 12px; height: 400px; border: 1px solid #e2e8f0; }
.chart-wrapper { height: 300px; position: relative; }

/* MODAL */
.modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.7); display: flex; justify-content: center; align-items: center; z-index: 1000; padding: 20px; }
.modal-box { background: white; width: 100%; max-width: 800px; max-height: 85vh; border-radius: 20px; overflow: hidden; display: flex; flex-direction: column; }
.modal-header { background: #1e293b; color: white; padding: 25px; display: flex; justify-content: space-between; }
.btn-close { background: none; border: none; color: white; font-size: 2rem; cursor: pointer; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.modal-table td, .modal-table th { padding: 12px; border-bottom: 1px solid #f1f5f9; vertical-align: top; }
.desc-text { word-wrap: break-word; line-height: 1.4; color: #475569; }
.nowrap { white-space: nowrap; }
.right { text-align: right; }
.red { color: #e11d48; }
.main-footer { margin-top: auto; padding: 30px 0; text-align: center; color: #94a3b8; border-top: 1px solid #e2e8f0; }
</style>