# Business Model Canvas — FinLog

Versão atualizada para a entrega final de Soluções Computacionais (2026/2). Este documento parte da proposta da primeira atividade (turma GPNCETHB23T) e a atualiza com o que o sistema oferece hoje.

- Versão em canvas (A4 paisagem, layout clássico de 9 blocos): [BMC.pdf](BMC.pdf)
- Fonte editável do PDF: [fontes/gerar_bmc_pdf.py](fontes/gerar_bmc_pdf.py). Para gerar de novo, rode `python docs/fontes/gerar_bmc_pdf.py` (requer `pip install reportlab`).

Convenção: os itens marcados com **(1ª atividade)** foram mantidos como estavam na proposta do grupo. Os itens marcados com **(atualização)** foram acrescentados agora (no PDF, marcador escuro indica 1ª atividade e marcador amarelo indica atualização). As mudanças e os motivos estão na seção [Notas da atualização](#notas-da-atualização).

---

## 1. Segmentos de clientes

- Pessoas físicas que buscam melhorar a organização das finanças. **(1ª atividade)**
- O cidadão médio e famílias que querem entender a própria vida financeira (público descrito no elevator pitch do grupo). **(atualização)**
- Em especial, quem tem dificuldade de manter o controle dos gastos ou acha complexas as ferramentas que já existem (Documento de Visão, seções 2.1 e 2.2). **(atualização)**

## 2. Proposta de valor

- Solução prática e intuitiva para gestão financeira pessoal. **(1ª atividade)**
- Registro e categorização de receitas e despesas, com categorias criadas pelo próprio usuário no momento do lançamento. **(atualização)**
- Saldo calculado automaticamente e resumo consolidado na Página Inicial: entradas, saídas, saldo, gráfico "Entradas x Saídas", últimas movimentações e metas. **(atualização)**
- Extrato detalhado com filtros por tipo (entradas ou saídas), por categoria e por período ("Este Mês", "Últimos 30 dias" ou intervalo personalizado), com o botão "Exportar PDF", que gera o extrato filtrado pela impressão do navegador. **(atualização)**
- Metas de economia salvas na conta do usuário (descrição e valor), com cadastro, edição e exclusão. **(atualização)**
- IA Conselheira: um chat que responde perguntas sobre finanças pessoais usando o resumo financeiro do próprio usuário (receitas, despesas, saldo, categoria com maior gasto, últimas transações) e as metas cadastradas, informando quanto falta para cada meta com base no saldo atual. **(atualização)**
- Dados protegidos: cada usuário acessa apenas os próprios lançamentos, categorias, metas e conversa. **(atualização)**

## 3. Canais

- Aplicativo web acessado pelo navegador. **(1ª atividade)**

## 4. Relacionamento com clientes

- Autoatendimento direto no aplicativo. **(1ª atividade)** Hoje o próprio usuário cria a conta (nome, e-mail e senha), faz login (com a opção "Permanecer conectado") e cadastra, edita e exclui lançamentos e metas, sem depender de ninguém.
- Suporte dentro do app. **(1ª atividade)** Na versão atual, esse suporte é feito pela IA Conselheira, que tira dúvidas sobre finanças e também explica como usar o FinLog (por exemplo, onde criar metas e lançamentos). A tela oferece sugestões rápidas: "Como criar uma Meta?", "Ver meu saldo líquido" e "Onde estou gastando mais?". Não há FAQ nem atendimento humano no app.
- Backup automático. **(1ª atividade)** Os dados do usuário ficam guardados no banco de dados do servidor, vinculados à conta, e não no navegador. Uma rotina de backup automático do banco não faz parte do código desta versão; continua prevista no modelo de negócio.
- Privacidade da conversa com a IA: a conversa vale por sessão. Ela é recuperada enquanto o usuário estiver logado, começa nova a cada login e é apagada ao sair da conta. **(atualização)**

## 5. Fontes de receita

- Plano pago para acesso a outras funcionalidades. **(1ª atividade)**
- Situação atual: nesta versão todas as funcionalidades estão liberadas para qualquer usuário cadastrado. O sistema não tem planos, cobrança nem meio de pagamento, e o grupo ainda não definiu quais funcionalidades fariam parte do plano pago nem o preço. **(atualização)**

## 6. Recursos principais

- Equipe de desenvolvimento e GitHub. **(1ª atividade)** O GitHub guarda o código e o histórico de versões e roda os testes automatizados (GitHub Actions) a cada pull request e a cada push na branch principal.
- A plataforma FinLog: aplicação web, API e banco de dados. **(atualização)**
- Acesso à IA generativa Google Gemini, por meio de uma chave de API, que alimenta a IA Conselheira. **(atualização)**

## 7. Atividades-chave

Bloco ausente na primeira atividade; preenchido agora. **(atualização)**

- Desenvolver e manter a plataforma web e a API (lançamentos, categorias, metas, extrato e resumo).
- Integrar e manter a IA Conselheira: montar o resumo financeiro e as metas que vão junto com cada pergunta e tratar falhas de conexão com a IA.
- Proteger os dados dos usuários: senha guardada com hash (BCrypt), sessão com token assinado que é invalidado no servidor ao sair, bloqueio temporário após 5 tentativas de login erradas em 15 minutos e acesso de cada usuário apenas aos próprios dados, em linha com a LGPD, como pede o Documento de Visão.
- Manter o banco de dados e a hospedagem da aplicação.
- Garantir a qualidade a cada mudança, com testes automatizados no backend e no frontend.

## 8. Parcerias principais

- Universidade Católica de Brasília. **(1ª atividade)**
- Google, como fornecedor da IA (Gemini) usada na IA Conselheira. É um fornecedor de serviço contratado por chave de API, não uma parceria formal. **(atualização)**

## 9. Estrutura de custos

- Tempo de desenvolvimento. **(1ª atividade)**
- Hospedagem da aplicação. **(1ª atividade)**
- Manutenção do banco de dados. **(1ª atividade)**
- Uso da API do Google Gemini pela IA Conselheira, conforme o plano de uso contratado. **(atualização)**

---

## Notas da atualização

O que mudou em relação à primeira atividade e por quê. Nenhum número, preço ou parceiro novo foi criado: as mudanças vêm do sistema implementado (código em `web/` e `api/`) ou dos documentos do grupo (elevator pitch e Documento de Visão).

| Bloco | Primeira atividade | O que mudou | Por quê |
|---|---|---|---|
| Segmentos de clientes | Pessoas físicas que buscam melhorar a organização das finanças | Acrescentados o cidadão médio e famílias e quem acha complexas as ferramentas existentes | Esse público já estava no elevator pitch e no Documento de Visão do grupo; só não constava no canvas |
| Proposta de valor | Solução prática e intuitiva para gestão financeira pessoal | Detalhada com o que o sistema entrega hoje: lançamentos com categorias, saldo e resumo automáticos, extrato com filtros e exportação em PDF, metas salvas, IA Conselheira e proteção dos dados | São as funcionalidades implementadas na aplicação web e na API |
| Canais | Aplicativo web acessado pelo navegador | Sem mudança | Continua sendo o único canal: o projeto tem apenas a aplicação web |
| Relacionamento com clientes | Autoatendimento, suporte dentro do app e backup automático | Itens mantidos e descritos como estão hoje: o suporte é dado pela IA Conselheira; o backup automático ainda não está implementado. Acrescentada a conversa com a IA por sessão | Deixar claro o que já funciona e o que continua só no modelo de negócio |
| Fontes de receita | Plano pago para acesso a outras funcionalidades | Mantido, com a observação de que ainda não há planos nem cobrança no sistema | Hoje todas as funcionalidades estão liberadas; o plano pago segue como fonte de receita prevista |
| Recursos principais | Equipe de desenvolvimento e GitHub | Acrescentados a plataforma FinLog (web, API e banco de dados) e o acesso à IA Gemini; descrito o uso do GitHub para testes automatizados | A plataforma e a IA passaram a existir e são necessárias para entregar a proposta de valor |
| Atividades-chave | Não preenchido | Preenchido com desenvolvimento e manutenção da plataforma e da API, integração com a IA, proteção dos dados, banco e hospedagem e testes automatizados | O bloco faltava; as atividades decorrem diretamente do que o projeto faz |
| Parcerias principais | Universidade Católica de Brasília | Acrescentado o Google como fornecedor da IA (não parceria formal) | A IA Conselheira depende do serviço Gemini |
| Estrutura de custos | Tempo de desenvolvimento, hospedagem e manutenção do banco de dados | Acrescentado o uso da API do Gemini | A IA Conselheira consome esse serviço a cada pergunta |
