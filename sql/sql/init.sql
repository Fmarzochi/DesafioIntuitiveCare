CREATE TABLE IF NOT EXISTS demonstracoes_contabeis (
    id SERIAL PRIMARY KEY,
    data_competencia DATE,
    reg_ans INT,
    cd_conta_contabil BIGINT,
    descricao VARCHAR(255),
    vl_saldo_inicial NUMERIC(20, 2),
    vl_saldo_final NUMERIC(20, 2)
);

-- Indices para performance (Fase 4 - API)
CREATE INDEX IF NOT EXISTS idx_descricao ON demonstracoes_contabeis (descricao);
CREATE INDEX IF NOT EXISTS idx_reg_ans ON demonstracoes_contabeis (reg_ans);
CREATE INDEX IF NOT EXISTS idx_data ON demonstracoes_contabeis (data_competencia);