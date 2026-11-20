## FinLog - Gestão Financeira Inteligente
O FinLog é um sistema de controle financeiro desenvolvido para auxiliar usuários no gerenciamento eficiente de suas receitas e despesas. A aplicação oferece uma visão clara e organizada do comportamento financeiro, facilitando o planejamento e o alcance de metas pessoais.

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| [`docs/`](docs/README.md) | Documentação: integrantes, link do vídeo, BMC, modelo de dados e as propostas do semestre anterior |
| [`api/`](api/README.md) | API e serviços de backend (Java 21 + Spring Boot), com Dockerfile e `docker-compose.yml` que sobe a API e o MySQL |
| [`web/`](web/README.md) | Aplicação web (React + Vite), que consome a API |

## Representação Arquitetural
O projeto adota o modelo de Arquitetura 4+1 de Philippe Kruchten, organizando a documentação em múltiplas visões complementares:

- Visão Lógica: Organização em três camadas (Frontend, Backend e Banco de Dados).

- Visão de Implementação: Detalhamento de classes e fluxos de interação.

- Visão de Casos de Uso: Conjunto de funcionalidades que guiam as decisões técnicas.

- Visão de Implantação: Infraestrutura baseada em ambiente web com suporte a MySQL e Java Runtime.

## Funcionalidades (MVP)
Com base nos casos de uso identificados no DAS:

- Autenticação (UC01 & UC02): Cadastro, login com token assinado (HMAC) e logout que invalida o token no servidor. Limite de tentativas de login (após 5 erros em 15 minutos, novas tentativas para aquele e-mail são recusadas até a janela expirar) e opção "Permanecer conectado".

- Gestão de Movimentações (UC03 a UC08): CRUD completo (Registro, Edição e Exclusão) de receitas e despesas, com categorias por usuário.

- Dashboard Consolidado (UC09): Página Inicial com saldo, entradas, saídas, gráfico de Entradas x Saídas, últimas movimentações e metas.

- Filtros de Análise (UC10): Extrato Detalhado com filtro por tipo (Todas, Entradas, Saídas), por categoria e por período (Este Mês, Últimos 30 dias ou Personalizado, com datas de início e fim). O extrato filtrado pode ser exportado em PDF pela impressão do navegador.

- Metas Financeiras (UC11): Cadastro, edição e exclusão de metas com descrição e valor alvo, salvas no banco de dados por usuário. *(As metas não têm prazo.)*

- Assistente Financeiro (IA Conselheira): Chat em página própria que responde às perguntas do usuário usando o resumo das suas finanças e as suas metas (Google Gemini). A conversa vale pela sessão: fica salva no servidor e é recarregada ao voltar ao chat enquanto o usuário estiver logado, e é apagada no login e no logout.

## Stack Tecnológica
O sistema é construído sobre uma base tecnológica robusta e escalável:

- Frontend: React + Vite (`web`), testado com Vitest e Testing Library.

- Backend: Java 21 com Spring Boot (`api`), testado com JUnit e Mockito.

- Banco de Dados: MySQL, rodando via Docker Compose.

- Formatação: valores em reais (R$) e datas no padrão brasileiro (pt-BR).

## Requisitos de Qualidade e Performance

- Desempenho (RNF005, Documento de Visão): meta de tempo máximo de resposta de 200ms para operações comuns. O projeto não tem teste automatizado que meça esse tempo.

- Segurança (RNF003): senhas guardadas com hash BCrypt, cada usuário acessa apenas os próprios dados e o logout invalida o token no servidor, em linha com a LGPD.

- Confiabilidade: Testes automatizados no backend e no frontend, executados no GitHub Actions a cada pull request e a cada push na branch `master`.

## Como rodar
O passo a passo completo (Docker, `.env`, chave da Gemini e frontend) está em [COMO-RODAR.md](COMO-RODAR.md). Cada aplicação também tem o seu README: [api/README.md](api/README.md) e [web/README.md](web/README.md).

Resumo:
1. `cd api`, copie `.env.example` para `.env` e preencha `GEMINI_API_KEY` e `APP_AUTH_TOKEN_SECRET` (32+ caracteres aleatórios).
2. `docker compose up --build` (backend em http://localhost:8080).
3. Em outro terminal: `cd web`, `npm ci` e `npm run dev` (http://localhost:5173).

Testes: `./mvnw test` em `api` e `npm test` em `web`.

## Colaboradores

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
