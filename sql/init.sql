-- 1. Limpeza inicial
DROP TABLE IF EXISTS dados_agregados;
DROP TABLE IF EXISTS despesas;
DROP TABLE IF EXISTS operadoras;

-- 2. Tabela de Operadoras (Dados Cadastrais)
CREATE TABLE operadoras (
    registro_ans VARCHAR(20) PRIMARY KEY,
    cnpj VARCHAR(20) NOT NULL,
    razao_social VARCHAR(255) NOT NULL,
    modalidade VARCHAR(100),
    uf VARCHAR(2)
);

-- Índices para busca rápida
CREATE INDEX idx_operadoras_razao ON operadoras(razao_social);
CREATE INDEX idx_operadoras_cnpj ON operadoras(cnpj);

-- 3. Tabela de Despesas
CREATE TABLE despesas (
    id SERIAL PRIMARY KEY,
    data_evento DATE,
    descricao VARCHAR(255),
    valor NUMERIC(20, 2),
    operadora_id VARCHAR(20),
    CONSTRAINT fk_operadora FOREIGN KEY (operadora_id) REFERENCES operadoras(registro_ans)
);

CREATE INDEX idx_despesas_data ON despesas(data_evento);
CREATE INDEX idx_despesas_op_id ON despesas(operadora_id);

-- 4. Tabela de Cache para Dashboard (KPIs)
CREATE TABLE dados_agregados (
    id SERIAL PRIMARY KEY,
    registro_ans VARCHAR(20),
    razao_social VARCHAR(255),
    uf VARCHAR(2),
    total_despesas NUMERIC(20, 2),
    CONSTRAINT fk_agregado_op FOREIGN KEY (registro_ans) REFERENCES operadoras(registro_ans)
);

