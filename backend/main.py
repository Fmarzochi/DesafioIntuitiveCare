from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine, text
import math
import re

# --- CONFIGURAÇÃO DO BANCO ---
DATABASE_URL = "postgresql://user_ans:password_ans@localhost:5432/ans_despesas_db"

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = create_engine(DATABASE_URL)

def corrigir_texto(texto):
    if not texto: return ""
    try: return texto.title()
    except: return texto

# --- ROTA 1: LISTAGEM (BUSCA CONSERTADA) ---
@app.get("/operadoras")
def listar_operadoras(
    search: str = Query(default=""),
    page: int = Query(default=0),
    limit: int = Query(default=10)
):
    offset = page * limit

    with engine.connect() as conn:
        # Monta a base da query
        sql = "SELECT registro_ans, cnpj, razao_social, modalidade, uf FROM operadoras"
        count_sql = "SELECT COUNT(*) FROM operadoras"

        params = {"limit": limit, "offset": offset}
        where_parts = []

        if search:
            # 1. Busca por Texto (Razão Social ou Registro bruto)
            where_parts.append("lower(razao_social) LIKE :termo_texto")
            where_parts.append("registro_ans LIKE :termo_texto")
            params["termo_texto"] = f"%{search.lower()}%"

            # 2. Busca Numérica (CNPJ sem ponto)
            # Remove tudo que NÃO é número. Se sobrar algo, busca no CNPJ.
            apenas_numeros = re.sub(r'[^0-9]', '', search)
            if len(apenas_numeros) > 0:
                where_parts.append("cnpj LIKE :termo_num")
                params["termo_num"] = f"%{apenas_numeros}%"

            # Junta tudo com OR dentro de parênteses (Importante!)
            filtro = " WHERE (" + " OR ".join(where_parts) + ")"
            sql += filtro
            count_sql += filtro

        sql += " ORDER BY razao_social LIMIT :limit OFFSET :offset"

        # Executa
        try:
            result = conn.execute(text(sql), params).fetchall()
            total_items = conn.execute(text(count_sql), params).scalar() or 0
        except Exception as e:
            print(f"Erro SQL: {e}")
            return {"content": [], "totalPages": 0, "totalElements": 0}

        total_pages = math.ceil(total_items / limit) if limit > 0 else 1

        lista = []
        for row in result:
            lista.append({
                "registroAns": row[0],
                "cnpj": row[1],
                "razaoSocial": corrigir_texto(row[2]),
                "modalidade": row[3],
                "uf": row[4]
            })

        return {
            "content": lista,
            "totalPages": total_pages,
            "totalElements": total_items
        }

# --- ROTA 2: DASHBOARD (SINTAXE CORRIGIDA) ---
@app.get("/estatisticas")
def get_dashboard():
    with engine.connect() as conn:
        try:
            # Total Geral
            total = conn.execute(text("SELECT COALESCE(SUM(total_despesas), 0) FROM dados_agregados")).scalar()

            # KPI: Média (SQL Simples)
            kpi_sql = """
                SELECT COUNT(*) FROM dados_agregados
                WHERE total_despesas > (SELECT AVG(total_despesas) FROM dados_agregados WHERE total_despesas > 0)
            """
            acima_media = conn.execute(text(kpi_sql)).scalar() or 0

            # Top 5
            top5_res = conn.execute(text("SELECT razao_social, total_despesas FROM dados_agregados ORDER BY total_despesas DESC LIMIT 5")).fetchall()
            top5_list = []
            for r in top5_res:
                top5_list.append({"razaoSocial": corrigir_texto(r[0]), "totalDespesas": float(r[1])})

            # Por UF
            uf_res = conn.execute(text("SELECT uf, SUM(total_despesas) as total FROM dados_agregados GROUP BY uf ORDER BY total DESC LIMIT 5")).fetchall()
            ufs_list = []
            for r in uf_res:
                ufs_list.append({"uf": r[0], "total": float(r[1])})

            return {
                "total_despesas": float(total),
                "ops_acima_media": int(acima_media),
                "top_5_operadoras": top5_list,
                "despesas_por_uf": ufs_list
            }
        except Exception as e:
            print(f"Erro Dash: {e}")
            return {
                "total_despesas": 0,
                "ops_acima_media": 0,
                "top_5_operadoras": [],
                "despesas_por_uf": []
            }

# --- ROTA 3: DETALHES ---
@app.get("/operadoras/{registro}/despesas")
def detalhes(registro: str):
    with engine.connect() as conn:
        res = conn.execute(text("SELECT data_evento, descricao, valor FROM despesas WHERE operadora_id = :reg ORDER BY data_evento DESC LIMIT 50"), {"reg": registro}).fetchall()

        lista_detalhes = []
        for r in res:
            lista_detalhes.append({
                "dataEvento": str(r[0]),
                "descricao": r[1],
                "valor": float(r[2])
            })
        return lista_detalhes