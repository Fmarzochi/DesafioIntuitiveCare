# 🚧 README Temporário: Status do Projeto (Em Desenvolvimento)

📍 **Fase Atual: Integração Frontend-Backend & Ajustes de Interface**

O projeto já superou o maior desafio técnico: a ingestão massiva de dados. Agora, estamos na etapa de refinamento da UI, garantindo que as funcionalidades de busca retornem todos os campos obrigatórios exigidos pelo PDF (**Registro ANS, CNPJ, Razão Social, Modalidade e UF**).

---

## ✅ O que foi feito (Milestones Alcançados)

### 🚀 Ingestão de Dados de Alta Performance
- Desenvolvimento de scripts Java para processar milhões de linhas das Demonstrações Contábeis (**3T2025**).
- Uso do protocolo **COPY Manager** para garantir velocidade superior ao JPA tradicional.

---

### 📊 Dashboard Analítico
- Consolidação financeira funcional com soma total de despesas (**Eventos Indenizáveis**).
- Gráficos de **"Top 5 Operadoras"** e **"Distribuição por UF"** renderizando dados reais do banco.

---

### 🐳 Arquitetura Dockerizada
- Ambiente PostgreSQL configurado via **Docker Compose** para persistência e portabilidade.

---

### 🔥 Criação do Backend e Frontend
- Base **Spring Boot (API)** e Base **Vue.js (Vite)** estabelecidas e comunicando entre si.

---

## 🛠️ Como foi feito e Por quê?

### ☕ Java + Spring Boot
Escolhido pela robustez no tratamento de arquivos CSV e automação de fluxos.

---

### 🐘 PostgreSQL (Protocolo COPY)
Implementado porque o volume de dados da ANS (**milhões de linhas**) inviabiliza o insert tradicional. O COPY processa o volume total em segundos.

---

### ⚡ Vue.js + Vite
Utilizado para garantir que o dashboard seja rápido, moderno e altamente responsivo.

---

### 💰 Tratamento de Moeda
Conversão de `vl_saldo_final` de String (formato brasileiro) para Numeric via SQL para cálculos matemáticos precisos.

---

## ⏳ O que ainda falta fazer (Próximos Passos)
- **Funcionalidade de Busca (Botão Buscar)**: Ajustar o componente para que a tabela preencha as 5 colunas obrigatórias.
- **Saneamento de Input**: Lógica para buscar CNPJ ignorando pontuações digitadas pelo usuário.
- **Refinamento do CORS**: Garantir comunicação total entre as portas **5173** e **8080**.
- **Finalização do README.md**: Documentação completa e guia de endpoints.

---

# 🚀 Como rodar o projeto por hora

---

## 1. Banco de Dados (Docker)
Certifique-se de estar na raiz do projeto e que o Docker está ativo:

```bash
sudo docker compose down -v && sudo docker compose up -d

---

## 2. Backend (Java/Spring Boot)
./mvnw spring-boot:run
O sistema iniciará a carga automática de dados ao subir.

---

## 3. Frontend (Vue.js/Vite)
Abra uma nova aba no terminal, entre na pasta do frontend e rode o servidor:
cd frontend
npm install
npm run dev

Acesse no navegador: 👉 http://localhost:5173