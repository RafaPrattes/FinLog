# FinLog — front-end

Interface web do FinLog, app de controle financeiro pessoal: login e cadastro,
saldo, entradas e saídas, extrato com filtros, metas e um assistente de IA.
Feito com React 19 e Vite; conversa com a API do `finlog-backend` via axios.

## Como rodar

Requer Node.js 22.12 ou mais recente e o backend em execução.

```bash
npm ci
npm run dev
```

O Vite mostra o endereço local (normalmente http://localhost:5173).

## Endereço da API

Por padrão o front usa `http://localhost:8080/api`. Para apontar para outro
backend, copie `.env.example` para `.env.local` e ajuste `VITE_API_URL`:

```bash
VITE_API_URL=http://localhost:8080/api
```

O `.env.local` não vai para o Git. Reinicie o `npm run dev` depois de alterá-lo.

## Scripts

| Comando           | O que faz                                        |
| ----------------- | ------------------------------------------------ |
| `npm run dev`     | Servidor de desenvolvimento com recarga automática |
| `npm run build`   | Gera a versão de produção em `dist/`             |
| `npm run preview` | Serve localmente o conteúdo de `dist/`           |
| `npm run lint`    | Verifica o código com ESLint                     |
| `npm test`        | Roda os testes (Vitest + Testing Library)        |

## Estrutura

- `src/App.jsx` — troca entre as telas (login, cadastro, painel) e restaura a sessão
- `src/components/` — telas e componentes (`Dashboard`, `ChatBot`, `LancamentoModal`…)
- `src/service/` — cliente da API (`api.js`), token da sessão e mensagens de erro
- `src/utils/format.js` — formatação de valores e datas e mapeamento das respostas da API
