-- 1. Tabela de Operadoras
CREATE TABLE IF NOT EXISTS operadoras (
    registro_ans VARCHAR(20) PRIMARY KEY, -- Chave Primária: O Registro ANS é único por operadora
    cnpj VARCHAR(20) NOT NULL,
    razao_social VARCHAR(255) NOT NULL,
    modalidade VARCHAR(100),
    uf VARCHAR(2)
);

-- Indice para acelerar a busca por nome
CREATE INDEX IF NOT EXISTS idx_operadora_nome ON operadoras(razao_social);


-- Tabela fato contendo apenas os eventos financeiros, ligada a operadora por FK
CREATE TABLE IF NOT EXISTS despesas (
    id SERIAL PRIMARY KEY,
    data_evento DATE,
    descricao VARCHAR(255),
    valor NUMERIC(20, 2),
    operadora_id VARCHAR(20),
    CONSTRAINT fk_operadora FOREIGN KEY (operadora_id) REFERENCES operadoras(registro_ans)
);

-- Indices para queries analiticas
CREATE INDEX IF NOT EXISTS idx_despesa_data ON despesas(data_evento);
CREATE INDEX IF NOT EXISTS idx_despesa_op ON despesas(operadora_id);


-- Tabela pré-calculada (Cache) para performance no Frontend (Trade-off 4.2.3)
CREATE TABLE IF NOT EXISTS dados_agregados (
    id SERIAL PRIMARY KEY,
    razao_social VARCHAR(255),
    uf VARCHAR(2),
    total_despesas NUMERIC(20, 2),
    media_trimestral NUMERIC(20, 2),
    desvio_padrao NUMERIC(20, 2),
    qtd_registros INT
);

