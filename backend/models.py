from sqlalchemy import Column, String, Integer, Float, ForeignKey, Date
from sqlalchemy.orm import relationship
from database import Base

class Operadora(Base):
    __tablename__ = "operadoras"

    cnpj = Column(String, primary_key=True, index=True)
    razao_social = Column(String)
    registro_ans = Column(String)
    modalidade = Column(String)
    uf = Column(String)

    # Relacionamento com despesas
    despesas = relationship("Despesa", back_populates="operadora")

class Despesa(Base):
    __tablename__ = "despesas"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    cnpj_operadora = Column(String, ForeignKey("operadoras.cnpj"))
    trimestre = Column(String)
    ano = Column(Integer)
    valor = Column(Float)
    data_evento = Column(Date)

    operadora = relationship("Operadora", back_populates="despesas")
