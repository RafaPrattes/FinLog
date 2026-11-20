# FinLog API (backend)

API REST do FinLog em Java 21 + Spring Boot 4, com banco MySQL. O frontend (`web/`) consome esta API em `http://localhost:8080/api`.

## Como executar

### Pré-requisitos

- **Docker Desktop** instalado e aberto (forma recomendada). Ele sobe a API e o MySQL; não é preciso instalar Java nem MySQL.
- Alternativa sem Docker: **JDK 21** (com `JAVA_HOME` apontando para ele) e um **MySQL** rodando na máquina (o Docker usa a imagem `mysql:9.7`).

Não há banco para importar: o MySQL começa vazio e as tabelas são criadas pela própria API ao iniciar (`JPA_DDL_AUTO=update`).

### Passo a passo com Docker

Todos os comandos partem da raiz do projeto (a pasta que contém `api/`, `web/` e `docs/`, seja o repositório clonado ou o `.zip` extraído).

**1. Entrar na pasta e criar o `.env`**

PowerShell (Windows):

```powershell
cd api
Copy-Item .env.example .env
```

bash (Linux/macOS/Git Bash):

```bash
cd api
cp .env.example .env
```

**2. Gerar o segredo dos tokens (obrigatório)**

A API **não sobe** sem `APP_AUTH_TOKEN_SECRET`: ele precisa ter pelo menos 32 caracteres e não pode conter `change-me` (o valor do `.env.example` é recusado de propósito). Gere um valor aleatório:

PowerShell:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

bash:

```bash
openssl rand -base64 48
```

Abra o `.env` (`notepad .env` no Windows) e troque a linha `APP_AUTH_TOKEN_SECRET=change-me-to-a-long-random-secret` por uma com o valor gerado, sem aspas e sem espaços:

```
APP_AUTH_TOKEN_SECRET=cole_aqui_o_valor_gerado
```

**3. Chave da IA (opcional)**

Para o assistente de IA responder, crie uma chave gratuita em https://aistudio.google.com/apikey e troque a linha `GEMINI_API_KEY=change-me` do `.env` por:

```
GEMINI_API_KEY=cole_sua_chave_aqui
```

Sem uma chave válida, todo o resto funciona (cadastro, login, movimentações, categorias, metas); só a IA responde "Erro ao conectar com a IA. Tente novamente.".

Os demais valores do `.env.example` já funcionam como estão.

**4. Subir a API e o banco**

```powershell
docker compose up --build
```

A primeira execução demora mais (baixa as imagens e compila o projeto). Deixe o terminal aberto. Para rodar em segundo plano, use `docker compose up --build -d`.

**5. Conferir se subiu**

Abra http://localhost:8080/ no navegador, ou:

```powershell
curl.exe http://localhost:8080/
```

(no bash, `curl http://localhost:8080/`). A resposta é um JSON com `"status":"FinLog backend em execucao"` e a lista de endpoints. Se a página não abrir logo, aguarde: a API só inicia depois que o healthcheck do MySQL passa.

**6. Parar**

```powershell
docker compose down
```

Os dados ficam guardados no volume `mysql_finlog` e voltam na próxima subida. `docker compose down -v` apaga também o banco.

### Passo a passo sem Docker

1. Deixe o MySQL rodando em `localhost:3306`.
2. Faça os passos 1 a 3 acima e, no `.env`, ajuste `FINLOG_DB_USERNAME` e `FINLOG_DB_PASSWORD` para um usuário do seu MySQL. O `FINLOG_DB_URL` do exemplo já aponta para `localhost:3306/finlog_db` e cria o banco se ele não existir.
3. Ainda dentro de `api`, rode:

PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

bash:

```bash
./mvnw spring-boot:run
```

(Se o bash responder `Permission denied`, o que pode acontecer depois de extrair o `.zip`, rode antes `chmod +x mvnw`, ou use `sh mvnw spring-boot:run`.)

A API lê o `.env` da pasta `api` automaticamente (`spring.config.import` em `application.properties`) e sobe em http://localhost:8080. Para parar, `Ctrl+C`.

## Variáveis de ambiente

Ficam no `api/.env` (copiado de `.env.example`). Com Docker, o `docker-compose.yml` repassa as variáveis para os contêineres; sem Docker, a API lê o `.env` direto. Quando a variável não está definida, vale o padrão abaixo.

| Variável | Obrigatória? | Padrão no Docker | Padrão sem Docker | Para que serve |
|---|---|---|---|---|
| `APP_AUTH_TOKEN_SECRET` | **Sim** | nenhum (o compose para com "Defina APP_AUTH_TOKEN_SECRET no .env") | nenhum (a API não inicia) | Segredo HMAC que assina os tokens de login. Mínimo 32 caracteres, sem `change-me`. |
| `GEMINI_API_KEY` | Não | vazio | vazio | Chave da API Gemini (Google). Sem ela, só a IA deixa de responder. |
| `GEMINI_API_URL` | Não | `https://generativelanguage.googleapis.com/v1/models/gemini-3.6-flash:generateContent` | igual | Endpoint do modelo Gemini usado pela IA. |
| `FINLOG_DB_URL` | Não | ignorado: o compose monta `jdbc:mysql://mysql-db:3306/<MYSQL_DATABASE>?...` | `jdbc:mysql://localhost:3306/finlog_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true` | URL JDBC do banco. |
| `FINLOG_DB_USERNAME` | Não | `finlog` | `root` | Usuário com que a API conecta no MySQL. |
| `FINLOG_DB_PASSWORD` | Não | `finlog` | vazio | Senha desse usuário. No Docker, deve ser igual a `MYSQL_ROOT_PASSWORD` (se o usuário for `root`) ou a `MYSQL_PASSWORD` (se for `MYSQL_USER`). |
| `MYSQL_ROOT_PASSWORD` | Não | `123456` | não usada | Senha do root do contêiner MySQL. |
| `MYSQL_DATABASE` | Não | `finlog_db` | não usada | Nome do banco criado no contêiner (também entra na URL do banco no Docker). |
| `MYSQL_USER` | Não | `finlog` | não usada | Usuário extra criado no contêiner MySQL. |
| `MYSQL_PASSWORD` | Não | `finlog` | não usada | Senha de `MYSQL_USER`. |
| `MYSQL_PORT` | Não | `3306` | não usada | Porta do MySQL no seu computador. Só vale se você descomentar o bloco `ports` do `mysql-db` no `docker-compose.yml`. |
| `FINLOG_BACKEND_PORT` | Não | `8080` | não usada (a API usa 8080) | Porta da API no seu computador. |
| `JPA_DDL_AUTO` | Não | `update` | `update` | Como o Hibernate trata o esquema (`update` cria e ajusta as tabelas). |
| `JPA_SHOW_SQL` | Não | `false` | `false` | `true` mostra o SQL gerado no log. |
| `APP_AUTH_TOKEN_EXPIRATION_SECONDS` | Não | `3600` | `3600` | Validade do token de login, em segundos. |
| `APP_CORS_ALLOWED_ORIGINS` | Não | `http://localhost:5173,http://localhost:5174` | igual | Endereços do frontend liberados no CORS, separados por vírgula. |

Os valores do `.env.example` (usuário `root`, senha `change-me` em `FINLOG_DB_PASSWORD` e `MYSQL_ROOT_PASSWORD`) já combinam entre si. Se trocar uma senha, troque a outra junto.

O `.env` tem segredos: não o envie para ninguém nem faça commit dele (ele já está no `.gitignore`).

## Dependências principais

Gerenciadas pelo Maven (`pom.xml`), baixadas automaticamente pelo wrapper `mvnw` (Maven 3.9.15) ou pelo build do Docker.

- Spring Boot 4.0.6 (parent `spring-boot-starter-parent`), Java 21
- `spring-boot-starter-webmvc`: API REST
- `spring-boot-starter-data-jpa`: acesso ao banco com JPA/Hibernate
- `spring-boot-starter-security`: autenticação por token e senhas com BCrypt
- `spring-boot-starter-validation`: validação dos dados recebidos
- `mysql-connector-j`: driver do MySQL
- Lombok
- Testes: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`, `spring-boot-starter-webmvc-test`; cobertura com JaCoCo 0.8.14

## Testes

Precisa de JDK 21. Não precisa de Docker nem de MySQL. Dentro de `api`:

PowerShell:

```powershell
.\mvnw.cmd test
```

bash:

```bash
./mvnw test
```

(Se aparecer `Permission denied`, rode antes `chmod +x mvnw`.) Na primeira execução o wrapper baixa o Maven e as dependências, então é preciso internet.

Para gerar também o relatório de cobertura (JaCoCo), rode `verify` no lugar de `test`; o relatório fica em `target/site/jacoco/index.html`. Os mesmos testes rodam no GitHub Actions (`.github/workflows/ci.yml`).

## Endpoints

Base: `http://localhost:8080`. As rotas autenticadas exigem o header `Authorization: Bearer <token>`, com o `token` devolvido pelo login. Sem token válido, a resposta é `401` com "Token ausente ou inválido".

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| GET | `/` | Pública | Status da API e lista de endpoints |
| POST | `/api/cadastro` | Pública | Cria usuário (`nome`, `email`, `senha` de 6 a 72 caracteres) |
| POST | `/api/auth/login` | Pública | Login (`email`, `senha`); devolve `usuario`, `token`, `tipo` e `expiraEmSegundos` |
| POST | `/api/auth/logout` | Token | Encerra a sessão: invalida os tokens do usuário e apaga o histórico do chat com a IA |
| GET | `/api/usuarios/me` | Token | Dados do usuário logado |
| GET | `/api/movimentacoes` | Token | Lista as receitas e despesas do usuário |
| GET | `/api/movimentacoes/{id}` | Token | Busca uma movimentação |
| POST | `/api/movimentacoes` | Token | Cria movimentação |
| PUT | `/api/movimentacoes/{id}` | Token | Edita movimentação |
| DELETE | `/api/movimentacoes/{id}` | Token | Exclui movimentação |
| GET | `/api/categorias` | Token | Lista as categorias do usuário |
| GET | `/api/categorias/{id}` | Token | Busca uma categoria |
| POST | `/api/categorias` | Token | Cria categoria |
| PUT | `/api/categorias/{id}` | Token | Edita categoria |
| DELETE | `/api/categorias/{id}` | Token | Exclui categoria |
| GET | `/api/categorias/resumo` | Token | Total de receitas, despesas e saldo por categoria |
| GET | `/api/categorias/{id}/resumo` | Token | Total de receitas, despesas e saldo de uma categoria |
| GET | `/api/metas` | Token | Lista as metas do usuário |
| POST | `/api/metas` | Token | Cria meta |
| PUT | `/api/metas/{id}` | Token | Edita meta |
| DELETE | `/api/metas/{id}` | Token | Exclui meta |
| GET | `/api/ai/conselho` | Token | Conselho da IA com base nas finanças do usuário |
| GET | `/api/ai/chat` | Token | Histórico da conversa com a IA |
| POST | `/api/ai/chat` | Token | Envia uma pergunta à IA (`mensagem`) |

Cada usuário só acessa os próprios dados. Após 5 tentativas de login erradas para o mesmo e-mail em 15 minutos, o login responde `429` até a janela expirar.

## Problemas comuns

- **`no configuration file provided: not found`**: o `docker compose` foi rodado fora da pasta `api`. Entre nela (`cd api`) e repita.
- **`Defina APP_AUTH_TOKEN_SECRET no .env`**: o `.env` não existe na pasta `api` ou a variável está vazia. Refaça os passos 1 e 2.
- **A API sai logo ao iniciar com `APP_AUTH_TOKEN_SECRET deve ser definido com pelo menos 32 caracteres aleatorios`**: o segredo é curto ou ainda contém `change-me`. Gere um novo (passo 2) e suba de novo.
- **Porta 8080 já em uso**: troque `FINLOG_BACKEND_PORT` no `.env` (ex.: `8081`) e rode `docker compose up --build` de novo. Avise o frontend: em `web`, copie `.env.example` para `.env.local` e ajuste `VITE_API_URL=http://localhost:8081/api`. Se quem ocupa a porta são contêineres de uma versão antiga do projeto (pasta `finlog-backend`), pare-os com `docker compose -p finlog-backend down`.
- **`Access denied for user` no log da API**: a senha da API não bate com a do MySQL. Confira `FINLOG_DB_USERNAME`/`FINLOG_DB_PASSWORD` (veja a tabela). Se você mudou senhas do MySQL depois da primeira subida, o volume antigo continua com as senhas originais: rode `docker compose down -v` (apaga os dados) e suba de novo.
- **A IA não responde**: confira `GEMINI_API_KEY` no `.env` e reinicie (`docker compose down` e `docker compose up --build`). O `.env` só é lido na subida.
- **Erro de CORS no navegador**: o frontend está em um endereço que não está em `APP_CORS_ALLOWED_ORIGINS`. Inclua o endereço e reinicie a API.
- **`docker` não é reconhecido ou não conecta**: o Docker Desktop precisa estar instalado e aberto antes dos comandos.
- **`mvnw` falha ao compilar (sem Docker)**: confira se `java -version` mostra a versão 21 e se `JAVA_HOME` aponta para o JDK 21.
