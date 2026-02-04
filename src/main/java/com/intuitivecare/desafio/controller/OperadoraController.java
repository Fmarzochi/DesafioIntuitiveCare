package com.intuitivecare.desafio.controller;

import com.intuitivecare.desafio.model.Operadora;
import com.intuitivecare.desafio.model.Despesa;
import com.intuitivecare.desafio.repository.OperadoraRepository;
import com.intuitivecare.desafio.repository.DespesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Libera o acesso para o Frontend
public class OperadoraController {

    @Autowired
    private OperadoraRepository operadoraRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    // Rota 1: Listar Operadoras (com paginação e busca)
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

    // Rota 2: Pegar despesas de uma operadora específica (pelo Registro ANS)
    @GetMapping("/operadoras/{registroAns}/despesas")
    public List<Despesa> listarDespesasDaOperadora(@PathVariable String registroAns) {
        return despesaRepository.findByOperadora_RegistroAns(registroAns);
    }

    // Rota 3: Estatísticas Gerais
    @GetMapping("/estatisticas")
    public Map<String, Object> getEstatisticas() {
        Double total = despesaRepository.somarTodasDespesas();
        return Map.of("total_geral", total != null ? total : 0.0);
    }
}