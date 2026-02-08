UPDATE demonstracoes_contabeis
SET vl_saldo_final = REPLACE(REGEXP_REPLACE(vl_saldo_final, '[^0-9,-]', '', 'g'), ',', '.')
WHERE vl_saldo_final IS NOT NULL;

UPDATE demonstracoes_contabeis
SET vl_saldo_inicial = REPLACE(REGEXP_REPLACE(vl_saldo_inicial, '[^0-9,-]', '', 'g'), ',', '.')
WHERE vl_saldo_inicial IS NOT NULL;

ALTER TABLE demonstracoes_contabeis
ALTER COLUMN vl_saldo_final TYPE NUMERIC(38,2)
USING NULLIF(vl_saldo_final, '')::numeric;

ALTER TABLE demonstracoes_contabeis
ALTER COLUMN vl_saldo_inicial TYPE NUMERIC(38,2)
USING NULLIF(vl_saldo_inicial, '')::numeric;

