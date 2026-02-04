import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

# Carrega as variáveis do arquivo .env
load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL")

# Cria o engine de conexão
engine = create_engine(DATABASE_URL)

# Configura a sessão
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# Dependência para pegar a sessão do DB
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
