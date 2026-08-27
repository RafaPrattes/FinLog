# Como Rodar o FinLog

Guia passo a passo para configurar e rodar o projeto do zero na sua máquina — backend, banco de dados e frontend.

## 1. O que instalar antes de começar

| Programa | Para que serve | Link |
|---|---|---|
| **Git** | Baixar o código do repositório | https://git-scm.com/downloads |
| **Docker Desktop** | Roda o backend e o banco de dados MySQL automaticamente, sem precisar instalar Java nem MySQL na sua máquina | https://www.docker.com/products/docker-desktop |
| **Node.js** (versão 20 ou mais recente) | Roda o frontend (a tela do sistema) | https://nodejs.org |

Depois de instalar, confirme que deu certo abrindo um terminal (PowerShell, no Windows) e rodando:

```powershell
git --version
docker --version
node --version
npm --version
```

Se cada comando desses mostrar um número de versão (em vez de erro), está tudo certo.

> **Sobre o banco de dados:** você **não precisa baixar nem instalar o MySQL** separadamente. O Docker sobe um banco vazio automaticamente, e o próprio sistema cria as tabelas sozinho na primeira vez que o backend liga. Não existe nenhum arquivo de banco pra baixar ou importar.

## 2. Clonar o repositório

```powershell
git clone https://github.com/RafaPrattes/FinLog.git
cd FinLog
```

Depois de clonar, você deve ver duas pastas principais: `finlog-backend` e `finlog-frontend`.

## 3. Configurar o backend (arquivo `.env`)

O backend precisa de um arquivo `.env` com suas configurações locais (senhas do banco, porta, chave da IA). Esse arquivo **não vem pronto no repositório** de propósito — cada pessoa cria o seu.

```powershell
cd finlog-backend
copy .env.example .env
```

Abra o arquivo `.env` recém-criado em qualquer editor de texto. Ele já vem com valores padrão que funcionam sem precisar mudar nada, **exceto a linha da chave da Gemini** (`GEMINI_API_KEY`), que você precisa preencher — veja o passo 4.

Se quiser, pode revisar/ajustar os outros valores (útil só se alguma porta já estiver em uso na sua máquina):

```
FINLOG_DB_URL=jdbc:mysql://localhost:3306/finlog_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
FINLOG_DB_USERNAME=root
FINLOG_DB_PASSWORD=change-me
MYSQL_ROOT_PASSWORD=change-me
MYSQL_DATABASE=finlog_db
MYSQL_USER=finlog
MYSQL_PASSWORD=change-me
MYSQL_PORT=3306
FINLOG_BACKEND_PORT=8080
GEMINI_API_KEY=change-me
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1/models/gemini-3.6-flash:generateContent
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
APP_AUTH_TOKEN_SECRET=change-me-to-a-long-random-secret
APP_AUTH_TOKEN_EXPIRATION_SECONDS=3600
```

**Importante:** o seu `.env` é só seu — não precisa (e não deve) ser igual ao de outra pessoa da equipe, e não deve ser enviado pra ninguém nem commitado no Git (ele já está protegido pelo `.gitignore`).

## 4. Criar sua chave gratuita da API Gemini (Google AI)

O Assistente de IA do FinLog usa a API Gemini do Google. É gratuita e leva menos de um minuto pra criar a sua:

1. Acesse **https://aistudio.google.com/apikey**
2. Faça login com uma conta Google.
3. Aceite os termos de uso, se aparecer.
4. Clique em **"Create API key"** (ou "Criar chave de API").
5. Copie a chave gerada (uma sequência longa de letras e números).
6. Cole no seu `.env`, substituindo o valor de `GEMINI_API_KEY`:
   ```
   GEMINI_API_KEY=cole_sua_chave_aqui
   ```

Sem essa chave, o sistema todo funciona normalmente (login, lançamentos, categorias) — só a aba **Assistente IA** não vai responder.

## 5. Subir o backend e o banco de dados (Docker)

Ainda dentro da pasta `finlog-backend`:

```powershell
docker compose up --build
```

- Na primeira vez, demora um pouco mais (ele baixa as imagens e monta o projeto). Nas próximas, é bem mais rápido.
- Deixe esse terminal aberto rodando — é ele que mantém o backend e o banco ligados.
- O backend fica disponível em **http://localhost:8080**.

Para desligar tudo depois:

```powershell
docker compose down
```

(Ctrl+C nesse mesmo terminal também para os containers, mas `docker compose down` garante que eles são removidos direito.)

## 6. Rodar o frontend

Abra **um novo terminal** (deixe o do Docker rodando no outro) e vá até a pasta do frontend:

```powershell
cd finlog-frontend
npm install
npm run dev
```

O `npm install` baixa as dependências do projeto (só precisa rodar isso na primeira vez, ou quando as dependências mudarem). O `npm run dev` liga a tela do sistema — o terminal vai mostrar um link, geralmente **http://localhost:5173**. Abra esse link no navegador.

## 7. Testando

Com os dois terminais rodando (Docker no primeiro, `npm run dev` no segundo):

1. Abra http://localhost:5173 no navegador.
2. Crie uma conta e faça login.
3. Cadastre uma receita ou despesa.
4. Confira o Dashboard e teste o Assistente IA (se já tiver colocado sua chave da Gemini).

## Problemas comuns

**`no configuration file provided: not found`**
Você rodou `docker compose` fora da pasta `finlog-backend`. O arquivo `docker-compose.yml` fica dentro dela — sempre rode o comando de lá (`cd finlog-backend` primeiro).

**Porta já em uso (erro ao subir o Docker)**
Alguma outra coisa na sua máquina já está usando a porta 3306 (banco) ou 8080 (backend). Troque `MYSQL_PORT` ou `FINLOG_BACKEND_PORT` no seu `.env` para outro número livre (ex: `3307`, `8081`) e rode `docker compose up --build` de novo.
> Se mudar `FINLOG_BACKEND_PORT`, também precisa atualizar a URL em `finlog-frontend/src/service/api.js` (o endereço que o frontend usa pra falar com o backend), já que esse endereço está fixo no código, não vem do `.env`.

**Assistente IA não responde**
Confire se preencheu `GEMINI_API_KEY` no `.env` com uma chave válida (veja passo 4), e se o backend foi reiniciado depois de editar o `.env` (`docker compose down` + `docker compose up --build`).

**"docker: command not found" ou Docker não abre**
O Docker Desktop precisa estar instalado *e aberto* (rodando em segundo plano) antes de usar os comandos `docker compose`.
