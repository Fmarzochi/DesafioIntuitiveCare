<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, BarElement, CategoryScale, LinearScale } from 'chart.js';
import { Pie, Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend);

// --- CONFIGURAÇÃO DA API (INTACTO) ---
const api = axios.create({ baseURL: 'http://localhost:8080' });

// --- ESTADOS (INTACTO) ---
const abaAtual = ref('lista');
const operadoras = ref([]);
const termoBusca = ref("");
const paginaAtual = ref(0);
const totalPaginas = ref(1);
const operadoraSelecionada = ref(null);
const historicoDespesas = ref([]);
const exibindoDetalhes = ref(false);
const valorTotalGeral = ref(0);
const dadosPizza = ref({ labels: [], datasets: [] });
const dadosBarras = ref({ labels: [], datasets: [] });

// --- FUNÇÃO CNPJ (INTACTO) ---
const formatarCNPJ = (v) => {
  if (!v) return '';
  const apenasNumeros = v.toString().replace(/\D/g, '');
  if (apenasNumeros.length === 14) {
    return apenasNumeros.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, "$1.$2.$3/$4-$5");
  }
  return v;
};

// --- FUNÇÕES DO SISTEMA (INTACTO) ---
const carregarDashboard = async () => {
  try {
    const res = await api.get('/estatisticas');
    const data = res.data;
    valorTotalGeral.value = data.total_despesas;
    if (data.top_5_operadoras) {
      dadosBarras.value = {
        labels: data.top_5_operadoras.map(op => op.razaoSocial.substring(0, 15) + '...'),
        datasets: [{ label: 'R$', backgroundColor: '#10b981', data: data.top_5_operadoras.map(op => op.totalDespesas) }]
      };
    }
    if (data.despesas_por_uf) {
      const topUfs = data.despesas_por_uf.slice(0, 5);
      dadosPizza.value = {
        labels: topUfs.map(u => u.uf),
        datasets: [{ backgroundColor: ['#1e293b', '#10b981', '#3498db', '#e11d48', '#f1c40f'], data: topUfs.map(u => u.total) }]
      };
    }
  } catch (e) { console.error("Erro Dashboard:", e); }
};

const buscarOperadoras = async (p) => {
  try {
    const res = await api.get('/operadoras', { params: { page: p, search: termoBusca.value } });
    operadoras.value = res.data.content;
    paginaAtual.value = p;
    totalPaginas.value = res.data.totalPages;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) { console.error("Erro Lista:", e); }
};

const abrirDetalhes = async (op) => {
  try {
    operadoraSelecionada.value = op;
    const res = await api.get(`/operadoras/${op.registroAns}/despesas`);
    historicoDespesas.value = res.data;
    exibindoDetalhes.value = true;
  } catch (e) { alert("Erro ao carregar despesas."); }
};

const irPara = (delta) => {
  const nova = paginaAtual.value + delta;
  if(nova >= 0 && nova < totalPaginas.value) buscarOperadoras(nova);
}

// FUNÇÃO NOVA: Resetar para Home (Limpa busca e volta pra lista)
const voltarParaHome = () => {
    abaAtual.value = 'lista';
    termoBusca.value = ''; // Opcional: limpa a busca ao clicar no logo
    buscarOperadoras(0);
};

onMounted(() => { buscarOperadoras(0); carregarDashboard(); });
</script>

<template>
  <div class="app-container">

    <header class="main-header">
      <div class="container header-content">

        <div class="logo-link" @click="voltarParaHome()" title="Voltar para a Página Inicial">
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
          <input v-model="termoBusca" @keyup.enter="buscarOperadoras(0)" placeholder="Pesquise por Nome, CNPJ ou Registro..." />
          <button @click="buscarOperadoras(0)">Buscar</button>
        </div>

        <div class="table-container">
          <table class="desktop-table">
            <thead>
              <tr>
                <th>Registro</th>
                <th>CNPJ</th>
                <th>Razão Social</th>
                <th>UF</th>
                <th class="center">Ação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="operadoras.length === 0">
                <td colspan="5" class="text-center">Nenhum dado encontrado.</td>
              </tr>
              <tr v-for="op in operadoras" :key="op.registroAns">
                <td><span class="badge">{{ op.registroAns }}</span></td>
                <td style="font-family: monospace; font-size: 0.95rem;">{{ formatarCNPJ(op.cnpj) }}</td>
                <td><strong>{{ op.razaoSocial }}</strong></td>
                <td>{{ op.uf }}</td>
                <td class="center">
                  <button class="btn-ver" @click="abrirDetalhes(op)">Ver</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination-container">
          <button class="btn-nav" :disabled="paginaAtual === 0" @click="irPara(-1)">Anterior</button>
          <span>Página {{ paginaAtual + 1 }} de {{ totalPaginas }}</span>
          <button class="btn-nav" :disabled="paginaAtual >= totalPaginas - 1" @click="irPara(1)">Próxima</button>
        </div>
      </div>

      <div v-show="abaAtual === 'dashboard'">
        <div class="kpi-card">
          <label>Total de Despesas</label>
          <div class="value">{{ valorTotalGeral?.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }}</div>
        </div>
        <div class="charts-row">
          <div class="chart-card">
            <h3>Por UF</h3>
            <div class="chart-wrapper">
              <Pie v-if="dadosPizza.labels.length" :data="dadosPizza" :options="{ maintainAspectRatio: false }" />
            </div>
          </div>
          <div class="chart-card">
            <h3>Top 5 Custos</h3>
            <div class="chart-wrapper">
              <Bar v-if="dadosBarras.labels.length" :data="dadosBarras" :options="{ indexAxis: 'y', responsive: true, maintainAspectRatio: false }" />
            </div>
          </div>
        </div>
      </div>

      <div v-if="exibindoDetalhes" class="modal-backdrop" @click.self="exibindoDetalhes=false">
        <div class="modal-box">
          <header class="modal-header">
            <div>
              <h2>{{ operadoraSelecionada?.razaoSocial }}</h2>
              <p style="margin:0; opacity:0.8; font-size: 0.9rem;">
                ANS: {{ operadoraSelecionada?.registroAns }} | CNPJ: {{ formatarCNPJ(operadoraSelecionada?.cnpj) }}
              </p>
            </div>
            <button class="btn-close" @click="exibindoDetalhes=false">&times;</button>
          </header>
          <div class="modal-body">
            <table class="modal-table">
              <thead><tr><th>Data</th><th>Descrição</th><th class="right">Valor</th></tr></thead>
              <tbody>
                <tr v-for="(ev, i) in historicoDespesas" :key="i">
                  <td class="nowrap">{{ new Date(ev.dataEvento).toLocaleDateString('pt-BR', {timeZone: 'UTC'}) }}</td>
                  <td class="desc-text">{{ ev.descricao }}</td>
                  <td class="right bold nowrap">{{ ev.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </main>

    <footer class="main-footer">
      <div class="container">
        <p>&copy; 2026 — Desenvolvido por <strong>Felipe Marzochi</strong>.</p>
      </div>
    </footer>

  </div>
</template>

<style scoped>
/* CORES */
:root { --primary: #1e293b; --accent: #10b981; --bg: #f8fafc; }
body { margin: 0; font-family: 'Inter', sans-serif; background: #f8fafc; color: #334155; }
.app-container { min-height: 100vh; display: flex; flex-direction: column; }
.container { width: 100%; max-width: 1200px; margin: 0 auto; padding: 0 20px; }

/* HEADER */
.main-header { background: #1e293b; color: white; padding: 1rem 0; box-shadow: 0 4px 10px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; }
.header-content { display: flex; justify-content: space-between; align-items: center; }

/* LOGO COMO LINK */
.logo-link { cursor: pointer; transition: opacity 0.2s; user-select: none; }
.logo-link:hover { opacity: 0.8; }
.logo-link h1 { font-size: 1.4rem; margin: 0; font-weight: 800; letter-spacing: -0.5px; }
.logo-link h1 span { color: #10b981; font-weight: 400; }

.main-nav button { background: transparent; border: none; color: #94a3b8; padding: 8px 16px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
.main-nav button:hover { color: white; }
.main-nav button.active { color: white; background: rgba(255,255,255,0.1); border-radius: 8px; }

/* TABLE & SEARCH */
.search-bar { display: flex; gap: 10px; margin: 30px 0; background: white; padding: 8px; border-radius: 12px; border: 1px solid #e2e8f0; }
.search-bar input { flex: 1; border: none; padding: 12px; outline: none; font-size: 1rem; }
.search-bar button { background: #10b981; color: white; border: none; padding: 0 25px; border-radius: 8px; font-weight: bold; cursor: pointer; }

.table-container { background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0; }
.desktop-table { width: 100%; border-collapse: collapse; }
.desktop-table th { background: #f8fafc; padding: 16px; text-align: left; font-size: 0.85rem; color: #64748b; text-transform: uppercase; font-weight: 700; border-bottom: 1px solid #e2e8f0; }
.desktop-table td { padding: 16px; border-bottom: 1px solid #f1f5f9; color: #334155; }

.badge { background: #e0f2fe; color: #0369a1; padding: 4px 8px; border-radius: 6px; font-family: monospace; font-weight: 600; }
.btn-ver { background: white; color: #3b82f6; border: 1px solid #3b82f6; padding: 6px 14px; border-radius: 6px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
.btn-ver:hover { background: #3b82f6; color: white; }
.text-center { text-align: center; }

/* PAGINATION */
.pagination-container { display: flex; justify-content: center; gap: 20px; align-items: center; margin: 30px 0; }
.btn-nav { background: white; border: 1px solid #cbd5e1; color: #475569; padding: 8px 16px; border-radius: 8px; cursor: pointer; }
.btn-nav:hover:not(:disabled) { border-color: #10b981; color: #10b981; }
.btn-nav:disabled { opacity: 0.5; cursor: not-allowed; }

/* DASHBOARD */
.kpi-card { background: white; padding: 40px; border-radius: 16px; text-align: center; border-top: 5px solid #10b981; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; }
.kpi-card label { display: block; color: #64748b; font-size: 0.9rem; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 10px; }
.kpi-card .value { font-size: 3rem; font-weight: 800; color: #1e293b; }
.charts-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 25px; margin-bottom: 40px; }
.chart-card { background: white; padding: 25px; border-radius: 16px; height: 420px; border: 1px solid #e2e8f0; display: flex; flex-direction: column; }
.chart-wrapper { flex: 1; position: relative; width: 100%; min-height: 0; }

/* MODAL */
.modal-backdrop { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 1000; padding: 20px; }
.modal-box { background: white; width: 100%; max-width: 900px; max-height: 85vh; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; }
.modal-header { background: #1e293b; color: white; padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; }
.btn-close { background: none; border: none; color: rgba(255,255,255,0.7); font-size: 2rem; cursor: pointer; line-height: 1; }
.modal-body { padding: 30px; overflow-y: auto; background: #f8fafc; }
.modal-table { width: 100%; border-collapse: separate; border-spacing: 0; background: white; border-radius: 8px; border: 1px solid #e2e8f0; }
.modal-table th { background: #f1f5f9; padding: 12px 15px; text-align: left; font-size: 0.8rem; color: #64748b; font-weight: 700; border-bottom: 1px solid #e2e8f0; }
.modal-table td { padding: 12px 15px; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
.desc-text { color: #475569; }
.right { text-align: right; }
.bold { font-weight: 700; color: #1e293b; }
.nowrap { white-space: nowrap; }

.main-footer { margin-top: auto; padding: 30px 0; text-align: center; color: #94a3b8; font-size: 0.9rem; border-top: 1px solid #e2e8f0; background: white; }

/* RESPONSIVIDADE */
@media (max-width: 768px) {
  .header-content { flex-direction: column; gap: 15px; }
  .charts-row { grid-template-columns: 1fr; }
  .kpi-card .value { font-size: 2.2rem; }
  .table-container { overflow-x: auto; }
  .desktop-table { min-width: 600px; }
}
</style>