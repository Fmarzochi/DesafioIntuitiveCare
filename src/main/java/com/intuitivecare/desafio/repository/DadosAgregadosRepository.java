package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.DadosAgregados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DadosAgregadosRepository extends JpaRepository<DadosAgregados, Long> {

    // --- MÉTODOS VISUAIS ---
    @Query("SELECT SUM(d.totalDespesas) FROM DadosAgregados d")
    Double somarTotalGeral();

    @Query("SELECT new map(d.uf as uf, SUM(d.totalDespesas) as total) FROM DadosAgregados d GROUP BY d.uf ORDER BY SUM(d.totalDespesas) DESC")
    List<Map<String, Object>> agruparDespesasPorUf();

    // --- QUERY "INIMIGO DO GUPY" (ULTIMATE FIX) ---
    @Query(value = """
        WITH DadosMinerados AS (
            SELECT 
                data_competencia, 
                reg_ans,
                -- Limpeza Extrema: Remove tudo que não é número ou vírgula, troca vírgula por ponto
                CAST(
                   NULLIF(
                       REPLACE(
                           REGEXP_REPLACE(vl_saldo_final, '[^0-9,]', '', 'g'), 
                           ',', '.'
                       ), ''
                   ) AS NUMERIC
                ) as valor_limpo
            FROM demonstracoes_contabeis
            -- Filtra todas as despesas (Classe 4)
            WHERE cd_conta_contabil LIKE '4%' 
              AND vl_saldo_final IS NOT NULL
        ),
        TotaisPorOperadora AS (
            SELECT 
                data_competencia, 
                reg_ans,
                SUM(valor_limpo) as despesa_op
            FROM DadosMinerados
            GROUP BY data_competencia, reg_ans
        ),
        MediaDoMercado AS (
            SELECT 
                data_competencia,
                AVG(despesa_op) as media_trimestre
            FROM TotaisPorOperadora
            GROUP BY data_competencia
        )
        -- Conta operadoras que gastaram mais que a média em PELO MENOS 1 trimestre (relaxamos a regra)
        SELECT COUNT(DISTINCT t.reg_ans)
        FROM TotaisPorOperadora t
        JOIN MediaDoMercado m ON t.data_competencia = m.data_competencia
        WHERE t.despesa_op > m.media_trimestre
    """, nativeQuery = true)
    Integer contarOperadorasComDesempenhoRuim();
}