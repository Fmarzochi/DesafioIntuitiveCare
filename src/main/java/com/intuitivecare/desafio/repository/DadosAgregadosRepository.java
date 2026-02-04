package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.DadosAgregados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface DadosAgregadosRepository extends JpaRepository<DadosAgregados, Long> {

    // Consulta 1: Soma total de todas as despesas (Para o Card de Total)
    @Query("SELECT SUM(d.totalDespesas) FROM DadosAgregados d")
    Double somarTotalGeral();

    // Consulta 2: Agrupa por UF para o gráfico (Item 4.3 do PDF)
    // Retorna algo como: [{"uf": "SP", "total": 5000.00}, {"uf": "RJ", "total": 3000.00}]
    @Query("SELECT new map(d.uf as uf, SUM(d.totalDespesas) as total) " +
            "FROM DadosAgregados d " +
            "GROUP BY d.uf " +
            "ORDER BY SUM(d.totalDespesas) DESC")
    List<Map<String, Object>> agruparDespesasPorUf();
}