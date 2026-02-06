package com.intuitivecare.desafio.controller;

import com.intuitivecare.desafio.model.Operadora;
import com.intuitivecare.desafio.model.Despesa;
import com.intuitivecare.desafio.model.DadosAgregados;
import com.intuitivecare.desafio.repository.OperadoraRepository;
import com.intuitivecare.desafio.repository.DespesaRepository;
import com.intuitivecare.desafio.repository.DadosAgregadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OperadoraController {

    @Autowired
    private OperadoraRepository operadoraRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private DadosAgregadosRepository dadosAgregadosRepository;

    // Listagem Principal (Atualizada para Busca Inteligente)
    @GetMapping("/operadoras")
    public Page<Operadora> listarOperadoras(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        if (search.isEmpty()) {
            return operadoraRepository.findAll(PageRequest.of(page, limit));
        } else {
            // Alteramos aqui: Agora chamamos um método genérico que busca por Nome, CNPJ ou ANS
            return operadoraRepository.buscarPorTexto(search, PageRequest.of(page, limit));
        }
    }

    // Detalhes da Operadora (Obrigatório Fase 4)
    @GetMapping("/operadoras/{registroAns}")
    public ResponseEntity<Operadora> buscarOperadora(@PathVariable String registroAns) {
        return operadoraRepository.findById(registroAns)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Histórico de Despesas (Obrigatório Fase 4)
    @GetMapping("/operadoras/{registroAns}/despesas")
    public ResponseEntity<List<Despesa>> listarDespesasDaOperadora(@PathVariable String registroAns) {
        List<Despesa> despesas = despesaRepository.findByOperadora_RegistroAns(registroAns);
        return ResponseEntity.ok(despesas);
    }

    // Estatísticas (Dashboard)
    @GetMapping("/estatisticas")
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        Double total = dadosAgregadosRepository.somarTotalGeral();
        stats.put("total_despesas", total != null ? total : 0.0);

        List<DadosAgregados> top5 = dadosAgregadosRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalDespesas"))
        ).getContent();
        stats.put("top_5_operadoras", top5);

        List<Map<String, Object>> porUf = dadosAgregadosRepository.agruparDespesasPorUf();
        stats.put("despesas_por_uf", porUf);

        return stats;
    }
}