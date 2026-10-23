## FinLog - Gestão Financeira Inteligente 💰
O FinLog é um sistema de controle financeiro desenvolvido para auxiliar utilizadores no gerenciamento eficiente de suas receitas e despesas. A aplicação oferece uma visão clara e organizada do comportamento financeiro, facilitando o planejamento e o alcance de metas pessoais.

## 🏗️ Representação Arquitetural
O projeto adota o modelo de Arquitetura 4+1 de Philippe Kruchten , organizando a documentação em múltiplas visões complementares:

- Visão Lógica: Organização em três camadas (Frontend, Backend e Banco de Dados).

- Visão de Implementação: Detalhamento de classes e fluxos de interação.

- Visão de Casos de Uso: Conjunto de funcionalidades que guiam as decisões técnicas.

- Visão de Implantação: Infraestrutura baseada em ambiente web com suporte a MySQL e Java Runtime.

## 🚀 Funcionalidades (MVP)
Com base nos casos de uso identificados no DAS:

- Autenticação (UC01 & UC02): Cadastro, login com token assinado (HMAC) e logout que invalida o token no servidor. Limite de tentativas de login e opção "Permanecer conectado".

- Gestão de Movimentações (UC03 a UC08): CRUD completo (Registro, Edição e Exclusão) de receitas e despesas, com categorias por usuário.

- Dashboard Consolidado (UC09): Resumo financeiro com saldo total, entradas, saídas e gráfico.

- Filtros de Análise (UC10): Consulta de transações por tipo e por categoria. *(Filtro por período ainda não implementado.)*

- Metas Financeiras (UC11): Cadastro, edição e exclusão de metas com valor alvo, salvas por usuário. *(Prazo ainda não implementado.)*

- Assistente Financeiro (IA): Chat integrado ao Dashboard que responde às perguntas do usuário usando o resumo das suas finanças (Google Gemini).

## 🛠️ Stack Tecnológica
O sistema é construído sobre uma base tecnológica robusta e escalável:

- Frontend: React + Vite (`finlog-frontend`), testado com Vitest e Testing Library.

- Backend: Java 21 com Spring Boot (`finlog-backend`), testado com JUnit e Mockito.

- Banco de Dados: MySQL, rodando via Docker Compose.

- Internacionalização (i18n): Suporte à formatação de valores monetários conforme a localidade do utilizador.

## 🛡️ Requisitos de Qualidade e Performance

- Desempenho (RNF005): Tempo máximo de resposta de 200ms para operações comuns.

- Segurança (RNF003): Credenciais protegidas por hash criptográfico e conformidade com a LGPD.

- Confiabilidade: Testes automatizados no backend e no frontend, executados no GitHub Actions a cada pull request.

## ▶️ Como rodar
O passo a passo completo (Docker, `.env`, chave da Gemini e frontend) está em [COMO-RODAR.md](COMO-RODAR.md).

Resumo:
1. `cd finlog-backend`, copie `.env.example` para `.env` e preencha `GEMINI_API_KEY` e `APP_AUTH_TOKEN_SECRET` (32+ caracteres aleatórios).
2. `docker compose up --build` (backend em http://localhost:8080).
3. Em outro terminal: `cd finlog-frontend`, `npm ci` e `npm run dev` (http://localhost:5173).

Testes: `./mvnw test` em `finlog-backend` e `npm test` em `finlog-frontend`.

## 👥 Colaboradores

<table>
  <tr>
    <td align="center"><a href="https://github.com/LorranyM"><img src="https://github.com/LorranyM.png" width="100px;" alt=""/><br /><sub><b>Lorrany Matos</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/RafaPrattes"><img src="https://github.com/RafaPrattes.png" width="100px;" alt=""/><br /><sub><b>Rafaela Prates</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/rehoaguiar"><img src="https://github.com/Rehoaguiar.png" width="100px;" alt=""/><br /><sub><b>Renata Aguiar</b></sub></a><br /></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/TeodoroSavite"><img src="https://github.com/TeodoroSavite.png" width="100px;" alt=""/><br /><sub><b>Teodoro Savite</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/Tiborhn"><img src="https://github.com/Tiborhn.png" width="100px;" alt=""/><br /><sub><b>Tibor Hodos</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/Yohanna-Araujo"><img src="https://github.com/Yohanna-Araujo.png" width="100px;" alt=""/><br /><sub><b>Yohanna Silva</b></sub></a><br /></td>
  </tr>
</table>
