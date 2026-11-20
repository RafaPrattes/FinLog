# FinLog — Documentação

## Integrantes

| Nome completo | Matrícula | Turma |
|---|---|---|
| Lorrany Matos Magalhães | UC23200204 | GPNCETHB23T |
| Rafaela Prates dos Santos Furtado | UC23200722 | GPNCETHB23T |
| Renata de Oliveira Aguiar | UC23100360 | GPNCETHB23T |
| Teodoro Martins Savite | UC23200042 | GPNCETHB23T |
| Tibor Hodos Neto | UC23100098 | GPNCETHB23T |
| Yohanna Araújo da Silva | UC23100599 | GPNCETHB23T |

## Vídeo de apresentação

Link (YouTube, não listado): VIDEO_LINK_AQUI

## Documentos

Documentos da entrega final:

| Arquivo | Conteúdo |
|---|---|
| [BMC.md](BMC.md) | Business Model Canvas atualizado do FinLog (versão editável) |
| [BMC.pdf](BMC.pdf) | Business Model Canvas atualizado (PDF) |
| [DADOS.pdf](DADOS.pdf) | Documentação do modelo de dados |
| [DADOS.md](DADOS.md) | Documentação do modelo de dados (versão editável) |
| [modelo-de-dados.mmd](modelo-de-dados.mmd) | Diagrama do modelo de dados (Mermaid, editável) |
| [fontes/](fontes/) | Scripts que geram os PDFs e diagramas acima |
| [telas/](telas/) | Prints das telas reais do sistema em funcionamento (a adicionar) |

Propostas do semestre anterior (março–abril de 2026):

| Arquivo | Conteúdo |
|---|---|
| [Documento de Visao - FinLog.pdf](Documento%20de%20Visao%20-%20FinLog.pdf) | Documento de Visão |
| [Documento de Arquitetura de Software.pdf](Documento%20de%20Arquitetura%20de%20Software.pdf) | Documento de Arquitetura de Software (DAS) |
| [Especificacao de Casos de Uso.pdf](Especificacao%20de%20Casos%20de%20Uso.pdf) | Especificação de Casos de Uso |

## Aplicações

O FinLog tem duas aplicações, cada uma com o seu README de instalação e execução:

- [api/](../api/README.md) — API e serviços de backend (inclui o banco de dados MySQL, que sobe junto via Docker Compose).
- [web/](../web/README.md) — Aplicação web, que consome a API.

O projeto não tem aplicação mobile (`app/`) nem desktop (`desktop/`).

O passo a passo completo para rodar o sistema do zero está em [COMO-RODAR.md](../COMO-RODAR.md).
