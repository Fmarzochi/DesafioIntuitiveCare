from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine, text
import math

# --- CONFIGURAÇÃO ---
DATABASE_URL = "postgresql://user_ans:password_ans@localhost:5432/ans_despesas_db"

app = FastAPI(title="API Intuitive Care Final")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = create_engine(DATABASE_URL)

# --- CORREÇÃO ORTOGRÁFICA  ---
def corrigir_texto(texto):
    if not texto: return ""
    try:
        t = texto.encode('latin1').decode('utf-8')
    except:
        t = texto

    # Mapa de caracteres
    correcoes = {
        "ãŠ": "ê", "ã€": "à", "ãš": "ú", "ã£": "ã", "ã§": "ç",
        "ã‡": "ç", "ãƒ": "ã", "ã‰": "é", "ã©": "é", "ã³": "ó",
        "ã¡": "á", "ãµ": "õ", "ã": "í"
    }
    for errado, certo in correcoes.items():
        t = t.replace(errado, certo)
    return t.title()

def formatar_cnpj(v):
    if not v: return ""
    v = str(v).replace('.', '').replace('/', '').replace('-', '').strip()
    if len(v) == 14:
        return f"{v[:2]}.{v[2:5]}.{v[5:8]}/{v[8:12]}-{v[12:]}"
    return v

# --- ROTAS ---

# 1. LISTAGEM E BUSCA
@app.get("/operadoras")
def listar_operadoras(
    search: str = Query(default=""),
    page: int = Query(default=0),  # Aceita 0
    limit: int = Query(default=10)
):
    offset = page * limit

    with engine.connect() as conn:
        # SQL Base
        sql = "SELECT registro_ans, cnpj, razao_social, modalidade, uf FROM operadoras"
        params = {"limit": limit, "offset": offset}

        # Filtro de Busca Híbrido (Texto e Números)
        if search:
            # Versão limpa para bater com CNPJ/Registro (remove pontos e traços)
            termo_limpo = search.replace(".", "").replace("/", "").replace("-", "").strip()

            # Busca: Nome (ILIKE normal) OU Registro OU CNPJ (limpo)
            sql += """ WHERE
                razao_social ILIKE :termo
                OR registro_ans ILIKE :termo
                OR cnpj ILIKE :termo_limpo
            """
            params["termo"] = f"%{search}%"
            params["termo_limpo"] = f"%{termo_limpo}%"

        sql += " ORDER BY razao_social LIMIT :limit OFFSET :offset"

        # Contagem para paginação
        count_sql = "SELECT COUNT(*) FROM operadoras"
        if search:
            termo_limpo = search.replace(".", "").replace("/", "").replace("-", "").strip()
            count_sql += """ WHERE
                razao_social ILIKE :termo
                OR registro_ans ILIKE :termo
                OR cnpj ILIKE :termo_limpo
            """

        total = conn.execute(text(count_sql), params).scalar() or 0
        result = conn.execute(text(sql), params).fetchall()

        return {
            "content": [{
                "registroAns": row[0],
                "cnpj": formatar_cnpj(row[1]),
                "razaoSocial": corrigir_texto(row[2]),
                "modalidade": corrigir_texto(row[3]),
                "uf": row[4]
            } for row in result],
            "totalPages": math.ceil(total / limit) if limit > 0 else 1
        }

# 2. DASHBOARD (Com KPI Real)
@app.get("/estatisticas")
def get_dashboard():
    with engine.connect() as conn:
        try:
            total = conn.execute(text("SELECT SUM(total_despesas) FROM dados_agregados")).scalar() or 0.0

            # Quantas empresas gastaram mais que a média?
            kpi_sql = "SELECT COUNT(*) FROM dados_agregados WHERE total_despesas > (SELECT AVG(total_despesas) FROM dados_agregados)"
            acima_media = conn.execute(text(kpi_sql)).scalar() or 0

            top5_res = conn.execute(text("SELECT razao_social, total_despesas FROM dados_agregados ORDER BY total_despesas DESC LIMIT 5")).fetchall()
            top5 = [{"razaoSocial": corrigir_texto(r[0]), "totalDespesas": float(r[1])} for r in top5_res]

            uf_res = conn.execute(text("SELECT uf, SUM(total_despesas) as total FROM dados_agregados GROUP BY uf ORDER BY total DESC LIMIT 5")).fetchall()
            ufs = [{"uf": r[0], "total": float(r[1])} for r in uf_res]

            return {
                "total_despesas": float(total),
                "ops_acima_media": int(acima_media),
                "top_5_operadoras": top5,
                "despesas_por_uf": ufs
            }
        except:
            return {"total_despesas": 0, "ops_acima_media": 0, "top_5_operadoras": [], "despesas_por_uf": []}

# 3. DETALHES
@app.get("/operadoras/{registro}/despesas")
def detalhes(registro: str):
    with engine.connect() as conn:
        res = conn.execute(text("SELECT data_evento, descricao, valor FROM despesas WHERE operadora_id = :reg ORDER BY data_evento DESC LIMIT 50"), {"reg": registro}).fetchall()
        return [{"dataEvento": str(r[0]), "descricao": r[1], "valor": float(r[2])} for r in res]