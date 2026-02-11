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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class OperadoraController {

    @Autowired
    private OperadoraRepository operadoraRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private DadosAgregadosRepository dadosAgregadosRepository;

    // LISTAGEM COM BUSCA INTELIGENTE (NOME/CNPJ) ---
    @GetMapping("/operadoras")
    public Page<Operadora> listarOperadoras(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        if (search == null || search.trim().isEmpty()) {
            return operadoraRepository.findAll(pageable);
        } else {
            String apenasNumeros = search.replaceAll("[^0-9]", "");

            if (apenasNumeros.isEmpty()) {
                // Se digitou letras, anula a busca por CNPJ
                apenasNumeros = "00000000000000_valor_impossivel";
            }

            return operadoraRepository.buscarGeral(search, apenasNumeros, pageable);
        }
    }

    @GetMapping("/operadoras/{registroAns}")
    public ResponseEntity<Operadora> buscarOperadora(@PathVariable String registroAns) {
        return operadoraRepository.findById(registroAns)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DESPESAS ---
    @GetMapping("/operadoras/{registroAns}/despesas")
    public ResponseEntity<List<Despesa>> listarDespesasDaOperadora(@PathVariable String registroAns) {
        List<Despesa> despesas = despesaRepository.findByOperadora_RegistroAns(registroAns);
        return ResponseEntity.ok(despesas);
    }

    // --- ESTATÍSTICAS ---
    @GetMapping("/estatisticas")
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Total Geral
        Double total = dadosAgregadosRepository.somarTotalGeral();
        stats.put("total_despesas", total != null ? total : 0.0);

        // 2. (Operadoras acima da média)
        try {
            Integer qtdRuim = dadosAgregadosRepository.contarOperadorasComDesempenhoRuim();
            stats.put("ops_acima_media", qtdRuim != null ? qtdRuim : 0);
        } catch (Exception e) {
            stats.put("ops_acima_media", 0); // Evita quebrar se o banco estiver vazio
        }

        // 3. Top 5
        List<DadosAgregados> top5 = dadosAgregadosRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalDespesas"))
        ).getContent();
        stats.put("top_5_operadoras", top5);

        // 4. Por UF
        List<Map<String, Object>> porUf = dadosAgregadosRepository.agruparDespesasPorUf();
        stats.put("despesas_por_uf", porUf);

        return stats;
    }
}