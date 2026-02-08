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

    // --- MANTIDO: LISTAGEM COM BUSCA INTELIGENTE (NOME/CNPJ) ---
    @GetMapping("/operadoras")
    public Page<Operadora> listarOperadoras(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        if (search == null || search.trim().isEmpty()) {
            return operadoraRepository.findAll(pageable);
        } else {
            // Lógica que já validamos e está funcionando:
            String apenasNumeros = search.replaceAll("[^0-9]", "");

            if (apenasNumeros.isEmpty()) {
                // Se digitou letras, anula a busca por CNPJ
                apenasNumeros = "00000000000000_valor_impossivel";
            }

            return operadoraRepository.buscarGeral(search, apenasNumeros, pageable);
        }
    }

    // --- MANTIDO: DETALHES ---
    @GetMapping("/operadoras/{registroAns}")
    public ResponseEntity<Operadora> buscarOperadora(@PathVariable String registroAns) {
        return operadoraRepository.findById(registroAns)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MANTIDO: DESPESAS ---
    @GetMapping("/operadoras/{registroAns}/despesas")
    public ResponseEntity<List<Despesa>> listarDespesasDaOperadora(@PathVariable String registroAns) {
        List<Despesa> despesas = despesaRepository.findByOperadora_RegistroAns(registroAns);
        return ResponseEntity.ok(despesas);
    }

    // --- ALTERADO AGORA: ESTATÍSTICAS (Incluindo Query 3.4) ---
    @GetMapping("/estatisticas")
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Total Geral (Já existia)
        Double total = dadosAgregadosRepository.somarTotalGeral();
        stats.put("total_despesas", total != null ? total : 0.0);

        // 2. NOVO: Query 3.4 do PDF (Operadoras acima da média)
        // Adicionamos isso para o frontend ter o que mostrar no novo cartão
        try {
            Integer qtdRuim = dadosAgregadosRepository.contarOperadorasComDesempenhoRuim();
            stats.put("ops_acima_media", qtdRuim != null ? qtdRuim : 0);
        } catch (Exception e) {
            stats.put("ops_acima_media", 0); // Evita quebrar se o banco estiver vazio
        }

        // 3. Top 5 (Já existia)
        List<DadosAgregados> top5 = dadosAgregadosRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalDespesas"))
        ).getContent();
        stats.put("top_5_operadoras", top5);

        // 4. Por UF (Já existia)
        List<Map<String, Object>> porUf = dadosAgregadosRepository.agruparDespesasPorUf();
        stats.put("despesas_por_uf", porUf);

        return stats;
    }
}