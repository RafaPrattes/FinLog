"""Gera docs/BMC.pdf: o Business Model Canvas do FinLog no layout clássico de 9 blocos.

Uso (a partir da raiz do repositório):
    pip install reportlab
    python docs/fontes/gerar_bmc_pdf.py

O conteúdo segue docs/BMC.md. Itens com marcador escuro vêm da primeira atividade;
itens com marcador amarelo foram acrescentados na atualização da entrega final.
"""

from pathlib import Path

from reportlab.lib.colors import HexColor
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph

SAIDA = Path(__file__).resolve().parent.parent / "BMC.pdf"

TINTA = HexColor("#1F1F1F")
CINZA = HexColor("#5C5C5C")
BORDA = HexColor("#9A9A9A")
FUNDO_TITULO = HexColor("#F2F2F2")
AMARELO = HexColor("#E0A800")

# (texto, novo?) — novo=True marca o que entrou na atualização
BLOCOS = {
    "parcerias": ("Parcerias principais", [
        ("Universidade Católica de Brasília.", False),
        ("Google, como fornecedor da IA (Gemini) usada na IA Conselheira. "
         "Fornecedor de serviço por chave de API, não parceria formal.", True),
    ]),
    "atividades": ("Atividades-chave", [
        ("Desenvolver e manter a plataforma web e a API.", True),
        ("Integrar e manter a IA Conselheira (resumo financeiro e metas enviados com cada pergunta).", True),
        ("Proteger os dados: senha com hash, sessão encerrada no servidor ao sair, "
         "bloqueio após 5 logins errados em 15 min, cada usuário só vê os próprios dados (LGPD).", True),
        ("Manter o banco de dados e a hospedagem.", True),
        ("Testes automatizados a cada mudança.", True),
    ]),
    "recursos": ("Recursos principais", [
        ("Equipe de desenvolvimento e GitHub (código, versões e testes automatizados).", False),
        ("Plataforma FinLog: aplicação web, API e banco de dados.", True),
        ("Acesso à IA generativa Google Gemini (chave de API).", True),
    ]),
    "proposta": ("Proposta de valor", [
        ("Solução prática e intuitiva para gestão financeira pessoal.", False),
        ("Registro e categorização de receitas e despesas, com categorias do próprio usuário.", True),
        ("Saldo automático e resumo consolidado: entradas, saídas, saldo, gráfico "
         "Entradas x Saídas, últimas movimentações e metas.", True),
        ("Extrato detalhado com filtros por tipo, categoria e período e exportação em PDF.", True),
        ("Metas de economia salvas na conta.", True),
        ("IA Conselheira: responde perguntas usando o resumo financeiro e as metas do usuário, "
         "inclusive quanto falta para cada meta.", True),
        ("Dados protegidos e acessíveis só pelo dono da conta.", True),
    ]),
    "relacionamento": ("Relacionamento com clientes", [
        ("Autoatendimento direto no aplicativo: o usuário cria a conta e gerencia lançamentos e metas.", False),
        ("Suporte dentro do app, hoje feito pela IA Conselheira (dúvidas de finanças e de uso do app).", False),
        ("Backup automático: dados guardados no servidor, ligados à conta; "
         "rotina de backup ainda não implementada.", False),
        ("Conversa com a IA por sessão: nova a cada login e apagada ao sair.", True),
    ]),
    "canais": ("Canais", [
        ("Aplicativo web acessado pelo navegador.", False),
    ]),
    "segmentos": ("Segmentos de clientes", [
        ("Pessoas físicas que buscam melhorar a organização das finanças.", False),
        ("O cidadão médio e famílias que querem entender a própria vida financeira.", True),
        ("Quem tem dificuldade de controlar os gastos ou acha complexas as ferramentas existentes.", True),
    ]),
    "custos": ("Estrutura de custos", [
        ("Tempo de desenvolvimento.", False),
        ("Hospedagem da aplicação.", False),
        ("Manutenção do banco de dados.", False),
        ("Uso da API do Google Gemini pela IA Conselheira, conforme o plano contratado.", True),
    ]),
    "receitas": ("Fontes de receita", [
        ("Plano pago para acesso a outras funcionalidades.", False),
        ("Hoje tudo está liberado para qualquer usuário cadastrado: ainda não há planos, "
         "cobrança nem preço definidos.", True),
    ]),
}


def estilo(tamanho):
    return ParagraphStyle(
        "item", fontName="Helvetica", fontSize=tamanho, leading=tamanho * 1.28,
        textColor=TINTA, leftIndent=9, spaceAfter=tamanho * 0.45,
    )


def desenhar_bloco(c, x, y, w, h, chave):
    """Desenha a caixa do bloco e encaixa os itens, reduzindo a fonte se precisar."""
    titulo, itens = BLOCOS[chave]
    c.setStrokeColor(BORDA)
    c.setLineWidth(0.8)
    c.setFillColor(FUNDO_TITULO)
    c.rect(x, y + h - 18, w, 18, stroke=0, fill=1)
    c.rect(x, y, w, h, stroke=1, fill=0)
    c.setFillColor(TINTA)
    c.setFont("Helvetica-Bold", 9.5)
    c.drawString(x + 7, y + h - 12.5, titulo)

    pad = 7
    largura = w - 2 * pad
    altura_util = h - 18 - 2 * pad
    tamanho = 8.6
    while True:
        st = estilo(tamanho)
        paragrafos = [(Paragraph(texto, st), novo) for texto, novo in itens]
        alturas = [p.wrap(largura, altura_util)[1] for p, _ in paragrafos]
        total = sum(alturas) + st.spaceAfter * (len(alturas) - 1)
        if total <= altura_util or tamanho <= 5.5:
            break
        tamanho -= 0.2

    topo = y + h - 18 - pad
    for (p, novo), alt in zip(paragrafos, alturas):
        topo -= alt
        p.drawOn(c, x + pad, topo)
        c.setFillColor(AMARELO if novo else TINTA)
        lado = tamanho * 0.5
        c.rect(x + pad + 1, topo + alt - tamanho * 0.95, lado, lado, stroke=0, fill=1)
        topo -= st.spaceAfter


def main():
    largura, altura = landscape(A4)
    c = canvas.Canvas(str(SAIDA), pagesize=(largura, altura))
    c.setTitle("Business Model Canvas - FinLog")
    c.setAuthor("Equipe FinLog")
    c.setSubject("Business Model Canvas atualizado para a entrega final")

    m = 22
    c.setFillColor(TINTA)
    c.setFont("Helvetica-Bold", 16)
    c.drawString(m, altura - m - 12, "Business Model Canvas — FinLog")
    c.setFont("Helvetica", 8.5)
    c.setFillColor(CINZA)
    c.drawString(m, altura - m - 25,
                 "Versão atualizada para a entrega final de Soluções Computacionais (2026/2), "
                 "a partir da proposta da primeira atividade (turma GPNCETHB23T). Detalhes e notas em docs/BMC.md.")

    # Legenda
    lx = largura - m - 250
    ly = altura - m - 10
    c.setFont("Helvetica", 8)
    c.setFillColor(TINTA)
    c.rect(lx, ly - 1, 6, 6, stroke=0, fill=1)
    c.drawString(lx + 10, ly, "Da primeira atividade")
    c.setFillColor(AMARELO)
    c.rect(lx + 115, ly - 1, 6, 6, stroke=0, fill=1)
    c.setFillColor(TINTA)
    c.drawString(lx + 125, ly, "Acrescentado na atualização")

    topo_grade = altura - m - 36
    base = m
    gap = 5
    alt_total = topo_grade - base
    alt_inferior = alt_total * 0.24
    alt_superior = alt_total - alt_inferior - gap
    y_sup = base + alt_inferior + gap

    col = (largura - 2 * m - 4 * gap) / 5
    xs = [m + i * (col + gap) for i in range(5)]
    util = alt_superior - gap
    # Atividades e Relacionamento têm mais itens: ficam com a parte maior da coluna
    alto, baixo = util * 0.58, util * 0.42

    desenhar_bloco(c, xs[0], y_sup, col, alt_superior, "parcerias")
    desenhar_bloco(c, xs[1], y_sup + baixo + gap, col, alto, "atividades")
    desenhar_bloco(c, xs[1], y_sup, col, baixo, "recursos")
    desenhar_bloco(c, xs[2], y_sup, col, alt_superior, "proposta")
    desenhar_bloco(c, xs[3], y_sup + baixo + gap, col, alto, "relacionamento")
    desenhar_bloco(c, xs[3], y_sup, col, baixo, "canais")
    desenhar_bloco(c, xs[4], y_sup, col, alt_superior, "segmentos")

    metade = (largura - 2 * m - gap) / 2
    desenhar_bloco(c, m, base, metade, alt_inferior, "custos")
    desenhar_bloco(c, m + metade + gap, base, metade, alt_inferior, "receitas")

    c.showPage()
    c.save()
    print(f"Gerado: {SAIDA}")


if __name__ == "__main__":
    main()
