from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from database import get_db
import models
from sqlalchemy import func

app = FastAPI(title="API Desafio Intuitive Care")

# Configuração de CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 4.2 Rotas Conectadas ao Banco

@app.get("/api/operadoras")
def list_operadoras(page: int = 1, limit: int = 10, search: str = "", db: Session = Depends(get_db)):
    skip = (page - 1) * limit
    
    query = db.query(models.Operadora)
    
    if search:
        query = query.filter(models.Operadora.razao_social.ilike(f"%{search}%"))

    total = query.count()
    operadoras = query.offset(skip).limit(limit).all()

    return {
        "data": operadoras,
        "total": total,
        "page": page,
        "limit": limit
    }

@app.get("/api/operadoras/{cnpj}")
def get_operadora(cnpj: str, db: Session = Depends(get_db)):
    operadora = db.query(models.Operadora).filter(models.Operadora.cnpj == cnpj).first()
    if not operadora:
        raise HTTPException(status_code=404, detail="Operadora não encontrada")
    return operadora

@app.get("/api/operadoras/{cnpj}/despesas")
def get_operadora_despesas(cnpj: str, db: Session = Depends(get_db)):
    despesas = db.query(models.Despesa).filter(models.Despesa.cnpj_operadora == cnpj).all()
    return despesas

@app.get("/api/estatisticas")
def get_estatisticas(db: Session = Depends(get_db)):
    # Total de despesas
    total_despesas = db.query(func.sum(models.Despesa.valor)).scalar() or 0
    
    # Top 5 operadoras por gasto
    top_5 = db.query(
        models.Operadora.razao_social, 
        func.sum(models.Despesa.valor).label("total")
    ).join(models.Despesa).group_by(models.Operadora.razao_social).order_by(func.sum(models.Despesa.valor).desc()).limit(5).all()

    return {
        "total_geral": total_despesas,
        "top_5": [{"operadora": t[0], "total": t[1]} for t in top_5]
    }
