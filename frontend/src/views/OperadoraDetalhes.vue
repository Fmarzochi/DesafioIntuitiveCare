<template>
  <div>
    <button @click="$router.back()" class="btn btn-outline-secondary mb-4">
      ⬅ Voltar
    </button>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-success" role="status"></div>
    </div>

    <div v-else-if="operadora">
      <div class="card shadow-sm mb-4 border-0">
        <div class="card-body">
          <h2 class="text-primary fw-bold">{{ formatarTextoFrontend(operadora.razaoSocial) }}</h2>
          <div class="row mt-3 text-muted">
            <div class="col-md-3"><strong>Registro:</strong> {{ operadora.registroAns }}</div>
            <div class="col-md-3"><strong>CNPJ:</strong> {{ formatarCNPJ(operadora.cnpj) }}</div>
            <div class="col-md-3"><strong>UF:</strong> {{ operadora.uf }}</div>
            <div class="col-md-3"><strong>Modalidade:</strong> {{ formatarTextoFrontend(operadora.modalidade) }}</div>
          </div>
        </div>
      </div>

      <div class="card shadow-sm border-0">
        <div class="card-header bg-white fw-bold py-3">Últimos Eventos Financeiros</div>
        <div class="table-responsive">
          <table class="table table-striped mb-0 align-middle">
            <thead class="bg-light">
              <tr>
                <th style="width: 15%;">Data</th>
                <th style="width: 65%;">Descrição (Conta Contábil)</th>
                <th class="text-end" style="width: 20%;">Valor</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(d, index) in despesas" :key="index">
                <td class="fw-bold text-secondary">{{ formatarDataBR(d.data_evento || d.dataEvento) }}</td>

                <td>{{ formatarTextoFrontend(d.descricao) }}</td>

                <td class="text-end fw-bold">{{ formatarMoeda(d.valor) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api';

export default {
  props: ['registroAns'],
  data() { return { operadora: null, despesas: [], loading: true } },
  async created() {
    try {
      // Busca dados da operadora
      const opResp = await api.get(`/operadoras?search=${this.registroAns}`);
      if (opResp.data.content && opResp.data.content.length > 0) {
         // Encontra a operadora correta na lista filtrada
         this.operadora = opResp.data.content.find(op => op.registroAns == this.registroAns);
      }

      // Busca despesas financeiras
      const despResp = await api.get(`/operadoras/${this.registroAns}/despesas`);
      this.despesas = despResp.data;

    } catch (e) {
      console.error(e);
    } finally {
      this.loading = false;
    }
  },
  methods: {
    formatarTextoFrontend(texto) {
      if (!texto) return '';

      let out = texto.toLowerCase();

      out = out.replace(/(?:^|\s|["'([{])+\S/g, match => match.toUpperCase());

      out = out.replace(/\/([a-z])/g, (match, p1) => '/' + p1.toUpperCase());
      const siglasObrigatorias = ['SUS', 'GRU', 'ANS', 'COV', 'PIS', 'COFINS', 'ISS', 'RN', 'RES'];

      siglasObrigatorias.forEach(sigla => {
        const regex = new RegExp(`\\b${sigla}\\b`, 'gi');
        out = out.replace(regex, sigla);
      });

      out = out.replace(/\b([a-z]{3})$/gi, (match) => {
        const preposicoes = ['dos', 'das', 'aos', 'por', 'com', 'sem'];
        if (preposicoes.includes(match.toLowerCase())) return match;
        return match.toUpperCase();
      });
      out = out.replace(/\s(De|Da|Do|Dos|Das|E|Em|Por|Com)\s/g, (match) => match.toLowerCase());
      return out;
    },

    formatarMoeda(val) {
      return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
    },

    formatarDataBR(dataIso) {
      if (!dataIso) return '-';
      // Garante que a data ISO (YYYY-MM-DD) seja quebrada corretamente
      const [ano, mes, dia] = dataIso.split('-');
      return `${dia}/${mes}/${ano}`;
    },

    formatarCNPJ(v) {
      if (!v) return '';
      v = v.replace(/\D/g, '');
      return v.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
    }
  }
}
</script>