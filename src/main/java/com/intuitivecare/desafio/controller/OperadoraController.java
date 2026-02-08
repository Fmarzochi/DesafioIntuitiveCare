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

    // --- CORREÇÃO DA BUSCA ---
    @GetMapping("/operadoras")
    public Page<Operadora> listarOperadoras(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        if (search == null || search.trim().isEmpty()) {
            return operadoraRepository.findAll(pageable);
        } else {
            // 1. Tenta limpar para pegar só números (caso seja busca por CNPJ)
            String apenasNumeros = search.replaceAll("[^0-9]", "");

            // 2. A CORREÇÃO CRÍTICA:
            // Se "apenasNumeros" ficou vazio (significa que o usuário digitou só letras, ex: "AD SALUTE"),
            // nós passamos um valor impossível para o filtro de CNPJ (ex: "XXX").
            // Isso impede que o SQL faça "CNPJ LIKE '%%'" (que traria tudo) e força ele a respeitar o filtro de Nome.
            if (apenasNumeros.isEmpty()) {
                apenasNumeros = "00000000000000_valor_impossivel";
            }

            // Agora a busca funciona para os dois casos corretamente.
            return operadoraRepository.buscarGeral(search, apenasNumeros, pageable);
        }
    }

    // Detalhes da Operadora
    @GetMapping("/operadoras/{registroAns}")
    public ResponseEntity<Operadora> buscarOperadora(@PathVariable String registroAns) {
        return operadoraRepository.findById(registroAns)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Histórico de Despesas
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