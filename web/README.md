# FinLog — aplicação web

Interface web do FinLog, sistema de gestão financeira pessoal. Consome a API do
diretório `api/` (Java + Spring Boot).

## Como executar

### 1. Pré-requisitos

- **Node.js 24.15 ou mais recente na linha 24 (recomendado), 22.22.2 ou mais
  recente na linha 22, ou 26+**, e **npm** (já vem com o Node): https://nodejs.org
  (a versão LTS do site serve). As ferramentas de teste (`vitest` e `jsdom`) não
  aceitam versões mais antigas nem as linhas ímpares (23, 25). Testado com
  Node 24.15.0 e npm 11.12.1.
- **A API do FinLog rodando.** O front não funciona sozinho: login, lançamentos,
  metas e IA dependem do backend. Siga primeiro o [README da API](../api/README.md).
  Por padrão a API fica em `http://localhost:8080`.

Confira as versões:

```powershell
node --version
npm --version
```

### 2. Instalar as dependências

A partir da raiz do projeto (a pasta que contém `api/`, `web/` e `docs/`), entre
em `web/` e instale. Os demais comandos deste README rodam dentro de `web/`.

```powershell
cd web
npm ci
```

O `npm ci` instala exatamente as versões do `package-lock.json`. Só é preciso
rodar de novo quando as dependências mudarem.

### 3. Variável de ambiente (opcional)

| Variável       | Padrão                      | Para que serve           |
| -------------- | --------------------------- | ------------------------ |
| `VITE_API_URL` | `http://localhost:8080/api` | Endereço base da API     |

Se a API estiver no endereço padrão, **não é preciso configurar nada**. Para
apontar para outro endereço, crie o arquivo `.env.local` a partir do exemplo e
ajuste o valor:

```powershell
# Windows PowerShell
copy .env.example .env.local
```

```bash
# bash (Linux, macOS, Git Bash)
cp .env.example .env.local
```

Conteúdo do `.env.local` (exemplo com a API na porta 8081):

```
VITE_API_URL=http://localhost:8081/api
```

O `.env.local` não vai para o Git. O Vite lê a variável ao iniciar: depois de
alterá-la, pare e rode de novo o `npm run dev` (ou refaça o `npm run build`).

### 4. Executar

```powershell
npm run dev
```

Deixe o terminal aberto e abra **http://localhost:5173** no navegador. Na tela
de login, clique em "Não tem cadastro? Clique aqui", crie uma conta e entre com
ela. Para parar o servidor, `Ctrl+C` no terminal.

Versão de produção:

```powershell
npm run build     # gera os arquivos em dist/
npm run preview   # serve o conteúdo de dist/ (por padrão em http://localhost:4173)
```

Para usar o `npm run preview` com a API, inclua `http://localhost:4173` em
`APP_CORS_ALLOWED_ORIGINS` no backend (veja [Problemas comuns](#problemas-comuns)).

## Scripts disponíveis

| Comando           | O que faz                                              |
| ----------------- | ------------------------------------------------------ |
| `npm run dev`     | Servidor de desenvolvimento com recarga automática     |
| `npm run build`   | Gera a versão de produção em `dist/`                   |
| `npm run preview` | Serve localmente o conteúdo de `dist/`                 |
| `npm test`        | Roda os testes uma vez (Vitest + Testing Library)      |
| `npm run lint`    | Verifica o código com ESLint                           |

## Testes e lint

```powershell
npm test
npm run lint
```

Os testes rodam em ambiente `jsdom` e não precisam da API ligada. Ficam ao lado
do código, com o sufixo `.test.js` / `.test.jsx` (login, painel, chat, sessão,
mensagens de erro e formatação).

## Telas da aplicação

- **Login** — e-mail e senha, com a opção "Permanecer Conectado" (mantém a
  sessão depois de fechar o navegador).
- **Cadastro** — nome completo, e-mail e senha, cada um com confirmação.

Depois do login, o menu lateral leva a:

- **Página Inicial** — entradas, saídas e saldo; gráfico "Entradas x Saídas";
  últimos lançamentos e metas, com atalhos para criar novos.
- **Extrato Detalhado** — totais do período e tabela de movimentações com
  filtros por tipo (todas, entradas, saídas), categoria e período (este mês,
  últimos 30 dias ou datas personalizadas); editar e excluir lançamentos e
  botão "Exportar PDF" (abre a impressão do navegador).
- **Metas** — lista das metas com quantidade e valor total; criar, editar e
  excluir metas.
- **IA Conselheira** — chat com a assistente financeira, com sugestões de
  perguntas. A conversa fica guardada no backend até o logout.
- **Configurações** — mostra o nome e o e-mail da conta (somente leitura).

Os lançamentos (entrada ou saída, com valor, data, descrição e categoria) são
criados e editados em uma janela própria, onde também dá para criar uma
categoria nova. Para sair da conta, clique no seu nome no rodapé do menu.

## Estrutura de `src/`

```
src/
├── main.jsx          ponto de entrada do React
├── App.jsx           alterna entre login, cadastro e painel; restaura a sessão
├── styles.css        estilos da aplicação
├── components/       painel (Dashboard), menu lateral, login, cadastro, chat,
│                     janelas (lançamento, meta, confirmação) e componentes visuais
├── pages/            telas do painel: Inicio, Extrato, Metas, Configuracoes
├── service/
│   ├── api.js        cliente axios: usa VITE_API_URL, envia o token e trata 401
│   ├── session.js    guarda o token (localStorage ou sessionStorage)
│   └── errors.js     converte erros da API em mensagens para a pessoa
├── utils/format.js   formatação de valores e datas, filtros e totais
└── test/setup.js     configuração dos testes
```

## Problemas comuns

**A tela abre, mas o login falha ou nada carrega.**
Confira se a API está rodando (veja o [README da API](../api/README.md)). Se ela
estiver em outra porta (por exemplo, porque `FINLOG_BACKEND_PORT` foi trocado no
`.env` do backend), ajuste `VITE_API_URL` no `.env.local` e reinicie o
`npm run dev`.

**Erro de CORS no console do navegador.**
O backend só aceita chamadas das origens listadas em `APP_CORS_ALLOWED_ORIGINS`
(no `.env` da API). O padrão é `http://localhost:5173,http://localhost:5174`.
Se a porta 5173 estiver ocupada, o Vite usa a próxima livre; se o front abrir em
outra porta (ou em `npm run preview`, porta 4173), acrescente essa origem à
lista, separada por vírgula, e reinicie a API.

**"Sua sessão expirou. Entre novamente."**
O token de login venceu ou foi invalidado. Basta entrar de novo.

**A IA Conselheira não responde.**
A chave da IA é configurada no backend (`GEMINI_API_KEY`); veja o
[README da API](../api/README.md).
