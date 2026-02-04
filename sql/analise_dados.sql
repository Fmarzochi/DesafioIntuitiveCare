-- ----------------------------------------------------------------------------
-- 3.4 QUERY 3 (CORRIGIDA): Quantas operadoras tiveram despesas acima da média
-- em pelo menos 2 dos 3 trimestres analisados?
-- ----------------------------------------------------------------------------
WITH media_geral_trimestre AS (
    SELECT data_competencia, AVG(vl_saldo_final) as media_geral
    FROM demonstracoes_contabeis
    WHERE descricao ILIKE '%Eventos/Sinistros%'
    GROUP BY data_competencia
),
despesas_por_op AS (
    SELECT
        op.registro_ans,
        dc.data_competencia,
        SUM(dc.vl_saldo_final) as despesa_op
    FROM demonstracoes_contabeis dc
    JOIN operadoras op ON dc.reg_ans = op.registro_ans
    WHERE dc.descricao ILIKE '%Eventos/Sinistros%'
    GROUP BY op.registro_ans, dc.data_competencia
),
operadoras_filtradas AS (
    SELECT d.registro_ans
    FROM despesas_por_op d
    JOIN media_geral_trimestre m ON d.data_competencia = m.data_competencia
    WHERE d.despesa_op > m.media_geral
    GROUP BY d.registro_ans
    HAVING COUNT(*) >= 2 -- Filtro: apareceu em 2 ou mais trimestres acima da media
)
SELECT COUNT(*) as total_operadoras_acima_media FROM operadoras_filtradas;