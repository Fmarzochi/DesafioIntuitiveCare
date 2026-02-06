from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine, text
import math

# --- CONFIGURAÇÃO ---
DATABASE_URL = "postgresql://user_ans:password_ans@localhost:5432/ans_despesas_db"

app = FastAPI(title="Intuitive Care Desafio - API Final")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = create_engine(DATABASE_URL)

# --- MOTOR DE CORREÇÃO ORTOGRÁFICA (MAPA DE CARACTERES ANS) ---
def corrigir_texto(texto):
    if not texto:
        return ""

    # 1. Tenta reverter encoding básico
    try:
        t = texto.encode('latin1').decode('utf-8')
    except:
        t = texto

    # 2. MAPEAMENTO ESPECÍFICO (Baseado nos seus prints e textos colados)
    # Resolve: AssistãŠncia, ã€ Saãšde, Provisã£o, Variaã‡ãƒo
    correcoes = {
        "ãŠ": "ê",  # AssistãŠncia -> Assistência
        "ã€": "à",  # ã€ Saãšde -> à Saúde
        "ãš": "ú",  # Saãšde -> Saúde
        "ã£": "ã",  # Provisã£o / Nã£o -> Provisão / Não
        "ã§": "ç",  # Serviã§os -> Serviços
        "ã‡": "ç",  # Variaã‡ãƒo -> Variação (parte 1)
        "ãƒ": "ã",  # Variaã‡ãƒo -> Variação (parte 2)
        "ã‰": "é",  # Mã‰dico -> Médico
        "ã©": "é",
        "ã³": "ó",
        "ã¡": "á",
        "ãµ": "õ",
        "ã": "í"
    }

    for errado, certo in correcoes.items():
        t = t.replace(errado, certo)

    return t

def formatar_cnpj(cnpj_raw):
    if not cnpj_raw or len(str(cnpj_raw)) < 14: return cnpj_raw
    c = str(cnpj_raw).zfill(14)
    return f"{c[0:2]}.{c[2:5]}.{c[5:8]}/{c[8:12]}-{c[12:14]}"

# --- ROTAS ---

@app.get("/operadoras")
def listar_operadoras(search: str = "", page: int = Query(1, ge=1), per_page: int = Query(10, le=100)):
    with engine.connect() as conn:
        offset = (page - 1) * per_page
        filtros = ""
        params = {"limit": per_page, "offset": offset}
        if search:
            filtros = "WHERE razao_social ILIKE :search OR registro_ans::text ILIKE :search"
            params["search"] = f"%{search}%"

        sql_count = text(f"SELECT COUNT(*) FROM operadoras {filtros}")
        total_items = conn.execute(sql_count, params).scalar() or 0

        sql_data = text(f"SELECT registro_ans, cnpj, razao_social, modalidade, uf FROM operadoras {filtros} ORDER BY razao_social ASC LIMIT :limit OFFSET :offset")
        result = conn.execute(sql_data, params).fetchall()

        return {
            "data": [{
                "registro_ans": r[0],
                "cnpj": formatar_cnpj(r[1]),
                "razao_social": corrigir_texto(r[2]),
                "modalidade": corrigir_texto(r[3]),
                "uf": r[4]
            } for r in result],
            "meta": {"total_items": total_items, "total_pages": math.ceil(total_items / per_page), "current_page": page}
        }

@app.get("/dashboard/uf")
def estatisticas_uf():
    with engine.connect() as conn:
        sql = text("SELECT uf, SUM(total_despesas) as total FROM dados_agregados WHERE uf IS NOT NULL GROUP BY uf ORDER BY total DESC")
        result = conn.execute(sql).fetchall()
        if not result:
            sql = text("SELECT o.uf, SUM(d.valor) as total FROM despesas d JOIN operadoras o ON d.operadora_id = o.registro_ans GROUP BY o.uf ORDER BY total DESC")
            result = conn.execute(sql).fetchall()

        labels = [row[0] for row in result]
        data = [float(row[1]) for row in result]
        return {"labels": labels, "datasets": data, "total_geral": sum(data)}

@app.get("/operadoras/{registro}/despesas")
def historico_operadora(registro: str):
    with engine.connect() as conn:
        op = conn.execute(text("SELECT razao_social, cnpj FROM operadoras WHERE registro_ans = :reg"), {"reg": registro}).fetchone()
        if not op: raise HTTPException(status_code=404)

        hist = conn.execute(text("""
            SELECT data_evento, descricao, valor
            FROM despesas
            WHERE operadora_id = :reg
            ORDER BY valor DESC LIMIT 50
        """), {"reg": registro}).fetchall()

        return {
            "razao_social": corrigir_texto(op[0]),
            "cnpj": formatar_cnpj(op[1]),
            "historico": [{
                "data": str(row[0]),
                "descricao": corrigir_texto(row[1]),
                "valor": float(row[2])
            } for row in hist]
        }