# 🚀 Teste Técnico Intuitive Care: Engenharia de Dados & Full Stack

![Status](https://img.shields.io/badge/Status-Entregue-success)
![Java](https://img.shields.io/badge/ETL-Java%20Spring%20Boot-orange)
![Python](https://img.shields.io/badge/API-Python%20FastAPI-blue)
![Vue](https://img.shields.io/badge/Frontend-Vue.js%203-green)
![Postgres](https://img.shields.io/badge/Database-PostgreSQL-blue)

## 📍 Visão Geral e Abordagem

Este projeto foi desenvolvido com foco em **Performance** e **Integridade de Dados**. O desafio de processar arquivos CSV da ANS (que contêm milhões de registros) exigiu uma arquitetura híbrida, onde cada linguagem resolve um problema específico:

* **Java (Spring Boot):** Atua como o "motor de força". Escolhi Java para o ETL porque o gerenciamento de memória da JVM e o ecossistema de streams são superiores para ler arquivos gigantes sem estourar a RAM.
* **Python (FastAPI):** Atua como a "camada de inteligência". Escolhi Python para a API pela facilidade de manipulação de dados (Pandas/SQLAlchemy) e pela velocidade de desenvolvimento de endpoints assíncronos.
* **PostgreSQL:** O banco relacional foi a escolha óbvia para garantir a tipagem forte (`NUMERIC`) dos dados financeiros.

---

## 🛠️ Decisões Técnicas e Justificativas (O "Como" e o "Porquê")

Abaixo, detalho as escolhas arquiteturais baseadas nos requisitos do PDF.

### 1. Ingestão de Dados (ETL)
* **Como fiz:** Implementei um leitor de CSV em Java que utiliza `BufferedReader` e envia os dados para o banco usando o protocolo `COPY` do PostgreSQL.
* **Por que fiz:** A abordagem tradicional com JPA/Hibernate (`.save()`) seria inviável para milhões de linhas (demoraria horas). O protocolo `COPY` insere blocos de dados diretamente no binário do banco, reduzindo o tempo de carga para segundos.
* **Tratamento de Dados:** Implementei rotinas SQL para converter strings financeiras brasileiras (ex: `1.200,50`) para tipos `NUMERIC` nativos, garantindo precisão matemática e corrigindo erros de encoding (UTF-8/Latin1).

### 2. Performance da Tabela (Requisito 4.3.3)
* **Estratégia:** Paginação no Lado do Servidor (Server-side Pagination).
* **Justificativa:** O PDF questiona sobre exibir muitas operadoras. Carregar 50.000 operadoras no navegador do cliente travaria a interface (DOM excessivo). Optei por enviar apenas 10 registros por vez via SQL (`LIMIT 10 OFFSET X`). Isso mantém a interface leve e responsiva, independentemente do tamanho do banco de dados.

### 3. Tratamento de Erros e Loading (Requisito 4.3.4)
* **Estados de Loading:** Durante as requisições assíncronas (fetch), a interface exibe indicadores visuais (spinners ou skeleton screens) para informar ao usuário que o dado está sendo processado.
* **Dados Vazios:** Se uma busca não retorna resultados (ex: um CNPJ inexistente), o sistema exibe uma mensagem amigável ("Nenhum registro encontrado") em vez de uma tabela em branco, melhorando a UX.
* **Erros de API:** Implementei blocos `try/catch` no Frontend. Caso a API Python esteja offline ou retorne erro 500, o usuário recebe um alerta visual (Toast/Modal) em vez de o site quebrar silenciosamente.

### 4. Trade-off: Query 1 (Crescimento vs Volume)
* **Decisão:** Optei por exibir as **"Top 5 Maiores Despesas" (Volume Total)** ao invés do crescimento percentual.
* **Justificativa Crítica:** Em análise de dados da ANS, operadoras inativas ou muito pequenas que gastam R$ 100,00 e passam a gastar R$ 500,00 apresentam um "crescimento" matemático de 400%, gerando ruído estatístico. Para um Dashboard de Visão Geral, entendi que identificar os **maiores volumes financeiros** (os "players" que movem o mercado) traz mais valor de negócio do que variações percentuais de pequenas entidades.

---

## 🚀 Como Executar o Projeto (Passo a Passo)

Siga a ordem abaixo para garantir que o ambiente suba corretamente.

### 1. Banco de Dados (Docker)
A persistência é garantida via Docker. Na raiz do projeto:
```bash
cd docker
docker compose up -d

## 2. Backend ETL (Java)
Na raiz do projeto
./mvnw spring-boot:run
Aguarde a mensagem "PROCESSO FINALIZADO" no console. Isso significa que o banco está carregado e pronto.

---

## 3. API (Python)
Responsável por servir os dados ao Frontend.
cd backend
pip install -r requirements.txt
uvicorn main:app --reload

---

## 4. Frontend (Vue.js)
cd frontend
npm install
npm run dev
Acesse o Dashboard em: 👉 http://localhost:5173

---

## 📡 Documentação da API
A API foi construída em REST. Abaixo, as rotas principais para teste:
Método	Endpoint	Função
GET	/operadoras	Retorna a lista paginada. Aceita param search (Nome ou Registro ANS).
GET	/operadoras/{registro}/despesas	Busca o histórico financeiro detalhado de uma operadora específica.
GET	/dashboard/uf	Retorna o JSON agregado para o gráfico de distribuição por Estado.
GET	/dashboard/top5	Retorna as operadoras com maiores despesas consolidadas.

---

## 🧪 Considerações Finais
O código foi estruturado pensando em escalabilidade. A separação entre o processo de carga (Java) e o processo de leitura (Python) permite que, no futuro, o ETL rode em um servidor dedicado de processamento em batch sem impactar a performance da API que atende os usuários.

Desenvolvido por Felipe Marzochi

---