package com.intuitivecare.desafio.controller;

import com.intuitivecare.desafio.model.Operadora;
import com.intuitivecare.desafio.model.DadosAgregados;
import com.intuitivecare.desafio.repository.OperadoraRepository;
import com.intuitivecare.desafio.repository.DadosAgregadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Libera o acesso para o Frontend (Vue.js)
public class OperadoraController {

    @Autowired
    private OperadoraRepository operadoraRepository;

    @Autowired
    private DadosAgregadosRepository dadosAgregadosRepository;

    // Rota 1: Listar Operadoras (com paginação e busca) - Item 4.2
    @GetMapping("/operadoras")
    public Page<Operadora> listarOperadoras(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        if (search.isEmpty()) {
            return operadoraRepository.findAll(PageRequest.of(page, limit));
        } else {
            return operadoraRepository.searchByRazaoSocial(search, PageRequest.of(page, limit));
        }
    }

    // Rota 2: Estatísticas Gerais para o Dashboard - Itens 4.2 e 4.3
    @GetMapping("/estatisticas")
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Total Geral de Despesas
        Double total = dadosAgregadosRepository.somarTotalGeral();
        stats.put("total_despesas", total != null ? total : 0.0);

        // 2. Top 5 Operadoras com mais despesas (Para lista de destaque)
        List<DadosAgregados> top5 = dadosAgregadosRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalDespesas"))
        ).getContent();
        stats.put("top_5_operadoras", top5);

        // 3. Distribuição por UF (Para o Gráfico de Barras/Pizza)
        List<Map<String, Object>> porUf = dadosAgregadosRepository.agruparDespesasPorUf();
        stats.put("despesas_por_uf", porUf);

        return stats;
    }
}