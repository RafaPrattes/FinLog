# -*- coding: utf-8 -*-
"""
Gera docs/DADOS.pdf (documentação do modelo de dados do FinLog).

Como usar (na raiz do repositório):
    python docs/fontes/gerar_dados_pdf.py

Requisitos: Python 3 e reportlab (pip install reportlab).
Fontes: Segoe UI, Georgia e Consolas da pasta C:/Windows/Fonts. Em outro sistema,
ajuste FONT_DIR ou os nomes dos arquivos em registrar_fontes().

O conteúdo vem das entidades em api/src/main/java/com/ucb/finlog/model/ e dos
repositórios em api/src/main/java/com/ucb/finlog/repository/. Os tipos MySQL foram
conferidos com SHOW CREATE TABLE num banco vazio criado pela API (ddl-auto=update).
A versão em texto editável é docs/DADOS.md e o diagrama em Mermaid é
docs/modelo-de-dados.mmd. Ao mudar uma entidade, atualize os três arquivos.
"""

import os
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    CondPageBreak, Flowable, KeepTogether, PageBreak, Paragraph,
    SimpleDocTemplate, Spacer, Table, TableStyle,
)

AQUI = Path(__file__).resolve().parent
SAIDA = AQUI.parent / "DADOS.pdf"
FONT_DIR = Path(os.environ.get("WINDIR", "C:/Windows")) / "Fonts"

# ---------------------------------------------------------------- cores
TINTA = colors.HexColor("#1F2328")
SUAVE = colors.HexColor("#57606A")
LINHA = colors.HexColor("#D0D7DE")
FUNDO = colors.HexColor("#F6F8FA")
DESTAQUE = colors.HexColor("#0B5CAD")
CAB_TABELA = colors.HexColor("#24313F")
PK_COR = colors.HexColor("#9A6700")
FK_COR = colors.HexColor("#0B5CAD")


def registrar_fontes():
    fontes = {
        "Texto": "segoeui.ttf",
        "Texto-Negrito": "segoeuib.ttf",
        "Texto-Italico": "segoeuii.ttf",
        "Texto-NegritoItalico": "segoeuiz.ttf",
        "Titulo": "georgiab.ttf",
        "Mono": "consola.ttf",
        "Mono-Negrito": "consolab.ttf",
    }
    for nome, arquivo in fontes.items():
        pdfmetrics.registerFont(TTFont(nome, str(FONT_DIR / arquivo)))
    pdfmetrics.registerFontFamily(
        "Texto", normal="Texto", bold="Texto-Negrito",
        italic="Texto-Italico", boldItalic="Texto-NegritoItalico",
    )
    pdfmetrics.registerFontFamily(
        "Mono", normal="Mono", bold="Mono-Negrito",
        italic="Mono", boldItalic="Mono-Negrito",
    )


# ---------------------------------------------------------------- estilos
def estilos():
    base = ParagraphStyle(
        "base", fontName="Texto", fontSize=9.5, leading=13.5,
        textColor=TINTA, alignment=TA_LEFT, spaceAfter=5,
    )
    return {
        "base": base,
        "titulo": ParagraphStyle("titulo", parent=base, fontName="Titulo", fontSize=24, leading=29, spaceAfter=4),
        "subtitulo": ParagraphStyle("subtitulo", parent=base, fontSize=11, leading=15, textColor=SUAVE, spaceAfter=14),
        "h1": ParagraphStyle("h1", parent=base, fontName="Titulo", fontSize=15, leading=19,
                             spaceBefore=12, spaceAfter=6, textColor=TINTA),
        "h2": ParagraphStyle("h2", parent=base, fontName="Texto-Negrito", fontSize=11.5, leading=15,
                             spaceBefore=10, spaceAfter=3, textColor=DESTAQUE),
        "item": ParagraphStyle("item", parent=base, leftIndent=12, bulletIndent=2, spaceAfter=3),
        "cel": ParagraphStyle("cel", parent=base, fontSize=8.3, leading=10.6, spaceAfter=0),
        "cel_cab": ParagraphStyle("cel_cab", parent=base, fontName="Texto-Negrito", fontSize=8.3,
                                  leading=10.6, spaceAfter=0, textColor=colors.white),
        "nota": ParagraphStyle("nota", parent=base, fontSize=8.3, leading=11, textColor=SUAVE),
        "codigo": ParagraphStyle("codigo", parent=base, fontName="Mono", fontSize=6.3, leading=9.5,
                                 backColor=FUNDO, borderColor=LINHA, borderWidth=0.5,
                                 borderPadding=5, leftIndent=5, rightIndent=5,
                                 spaceBefore=4, spaceAfter=10),
    }


def c(txt):
    """Trecho de código em fonte monoespaçada dentro de um parágrafo."""
    return f'<font name="Mono" size="8.2">{txt}</font>'


def converter(txt):
    """Converte `trecho` em fonte monoespaçada (um par de crases por trecho)."""
    partes = txt.split("`")
    saida = []
    for i, parte in enumerate(partes):
        saida.append(c(parte) if i % 2 else parte)
    return "".join(saida)


# ---------------------------------------------------------------- dados
# Cada coluna: (coluna, atributo Java, tipo Java, tipo MySQL, aceita NULL?, restrições)
TABELAS = [
    {
        "nome": "usuarios",
        "entidade": "Usuario",
        "resumo": "Pessoas cadastradas no sistema. É a tabela central: todas as outras apontam para ela.",
        "colunas": [
            ("id", "id", "Long", "bigint", False, "PK, AUTO_INCREMENT"),
            ("nome", "nome", "String", "varchar(255)", False, ""),
            ("email", "email", "String", "varchar(255)", False, "UK (único)"),
            ("senha", "senha", "String", "varchar(255)", False, "hash BCrypt, não a senha"),
            ("token_version", "tokenVersion", "int", "int", False, "DEFAULT 0 (@ColumnDefault)"),
        ],
        "notas": [
            "<b>Relacionamentos:</b> `@OneToMany(mappedBy = \"usuario\")` para `Movimentacao` "
            "(lista `movimentacoes`, carregamento LAZY, sem cascata). Não há coleção mapeada para "
            "categorias, metas ou mensagens: essas ligações só existem do lado do filho.",
            "O e-mail é gravado sem espaços nas pontas e em minúsculas (`UsuarioService.cadastrar`). "
            "O login normaliza o e-mail da mesma forma antes de buscar.",
            "A senha é cifrada com `BCryptPasswordEncoder` (`SecurityConfig`). O cadastro aceita senhas "
            "de 6 a 72 caracteres.",
            "`token_version` entra no token de acesso. No logout ela é incrementada, o que invalida todos "
            "os tokens emitidos antes (`AuthService.logout` e `AuthTokenFilter`).",
            "`senha`, `tokenVersion` e `movimentacoes` têm `@JsonIgnore` e nunca saem nas respostas da API.",
            "Validação na entrada (`CadastroUsuarioRequest`): nome obrigatório com até 100 caracteres; "
            "e-mail obrigatório, em formato válido, com até 255 caracteres.",
        ],
    },
    {
        "nome": "categorias",
        "entidade": "Categoria",
        "resumo": "Categorias criadas por cada usuário para classificar as movimentações "
                  "(por exemplo, “Mercado” ou “Salário”).",
        "colunas": [
            ("id", "id", "Long", "bigint", False, "PK, AUTO_INCREMENT"),
            ("nome", "nome", "String", "varchar(100)", False, "UK junto com usuario_id"),
            ("descricao", "descricao", "String", "varchar(255)", True, ""),
            ("usuario_id", "usuario", "Usuario", "bigint", False, "FK → usuarios.id"),
        ],
        "notas": [
            "<b>Restrição única `uk_categoria_usuario_nome` (`usuario_id`, `nome`):</b> o mesmo usuário "
            "não pode ter duas categorias com o mesmo nome. Usuários diferentes podem repetir nomes.",
            "<b>Relacionamento:</b> `@ManyToOne(fetch = LAZY)` com `Usuario`, obrigatório "
            "(N categorias para 1 usuário).",
            "Antes de gravar, o serviço tira os espaços das pontas do nome e recusa um nome que já exista "
            "para o usuário, sem diferenciar maiúsculas de minúsculas "
            "(`existsByNomeIgnoreCaseAndUsuarioEmail`). Nesse caso a API responde 409.",
            "Uma categoria não pode ser excluída enquanto tiver movimentações: `CategoriaService.deletar` "
            "responde 409 (“Categoria possui movimentacoes”). A chave estrangeira em "
            "`movimentacoes.categoria_id` também impediria a exclusão.",
            "Uma categoria também é criada automaticamente quando uma movimentação é salva com um nome de "
            "categoria que o usuário ainda não tem (`MovimentacaoService.resolverCategoria`).",
            "Os totais de cada categoria (receitas, despesas e saldo) não ficam gravados: são calculados a "
            "cada consulta a partir das movimentações (`somarPorCategoriaETipo`).",
        ],
    },
    {
        "nome": "movimentacoes",
        "entidade": "Movimentacao",
        "resumo": "Lançamentos financeiros do usuário: cada linha é uma receita ou uma despesa.",
        "colunas": [
            ("id", "id", "Long", "bigint", False, "PK, AUTO_INCREMENT"),
            ("descricao", "descricao", "String", "varchar(255)", True, ""),
            ("valor", "valor", "BigDecimal", "decimal(15,2)", False, "precision = 15, scale = 2"),
            ("data", "data", "LocalDate", "date", False, ""),
            ("tipo", "tipo", "TipoMovimentacao", "enum('DESPESA', 'RECEITA')", False, "@Enumerated(STRING)"),
            ("usuario_id", "usuario", "Usuario", "bigint", False, "FK → usuarios.id"),
            ("categoria_id", "categoria", "Categoria", "bigint", True, "FK → categorias.id"),
        ],
        "notas": [
            "<b>Relacionamento com usuário:</b> `@ManyToOne(fetch = LAZY)` com `Usuario`, obrigatório "
            "(N movimentações para 1 usuário).",
            "<b>Relacionamento com categoria:</b> `@ManyToOne` com `Categoria`, <b>opcional</b>: a "
            "movimentação pode ficar sem categoria. Sem `fetch` declarado, vale o padrão do JPA para "
            "`@ManyToOne`, que é EAGER. A resposta da API (`MovimentacaoResponse`) inclui a categoria.",
            "O valor é gravado sempre positivo. É o `tipo` que diz se a movimentação entra (RECEITA) ou "
            "sai (DESPESA) do saldo. A entrada (`MovimentacaoRequest`) exige valor positivo, com até 13 "
            "dígitos inteiros e 2 decimais (cabe em `decimal(15,2)`), data e tipo obrigatórios, e "
            "descrição de até 255 caracteres.",
            "A categoria pode ser informada pelo `categoriaId` (precisa ser do mesmo usuário; senão a API "
            "responde 404) ou pelo nome. Pelo nome, a API reaproveita a categoria do usuário com esse nome, "
            "sem diferenciar maiúsculas de minúsculas, ou cria uma nova. Um nome vazio deixa a "
            "movimentação sem categoria.",
        ],
    },
    {
        "nome": "metas",
        "entidade": "Meta",
        "resumo": "Metas financeiras do usuário: um objetivo com um valor a alcançar.",
        "colunas": [
            ("id", "id", "Long", "bigint", False, "PK, AUTO_INCREMENT"),
            ("descricao", "descricao", "String", "varchar(255)", False, ""),
            ("valor", "valor", "BigDecimal", "decimal(15,2)", False, "precision = 15, scale = 2"),
            ("usuario_id", "usuario", "Usuario", "bigint", False, "FK → usuarios.id"),
        ],
        "notas": [
            "<b>Relacionamento:</b> `@ManyToOne(fetch = LAZY)` com `Usuario`, obrigatório "
            "(N metas para 1 usuário).",
            "Entrada (`MetaRequest`): descrição obrigatória com até 255 caracteres (gravada sem espaços nas "
            "pontas) e valor positivo com até 13 dígitos inteiros e 2 decimais.",
            "A tabela guarda só a descrição e o valor-alvo. O quanto falta para cada meta não é gravado. "
            "Para o contexto enviado à IA, `AIService.descreverMetas` calcula, meta por meta, o valor da "
            "meta menos o saldo atual das movimentações (saldo negativo conta como zero).",
        ],
    },
    {
        "nome": "chat_mensagens",
        "entidade": "ChatMensagem",
        "resumo": "Mensagens da conversa do usuário com o assistente de IA. Valem só durante a sessão.",
        "colunas": [
            ("id", "id", "Long", "bigint", False, "PK, AUTO_INCREMENT"),
            ("papel", "papel", "PapelMensagem", "enum('IA', 'USUARIO')", False, "@Enumerated(STRING), length = 10"),
            ("texto", "texto", "String", "varchar(4000)", False, "length = TAMANHO_MAXIMO_TEXTO (4000)"),
            ("criada_em", "criadaEm", "Instant", "datetime(6)", False, "data e hora com microssegundos"),
            ("usuario_id", "usuario", "Usuario", "bigint", False, "FK → usuarios.id"),
        ],
        "notas": [
            "<b>Relacionamento:</b> `@ManyToOne(fetch = LAZY)` com `Usuario`, obrigatório "
            "(N mensagens para 1 usuário).",
            "Cada pergunta respondida gera duas linhas: a do usuário (`USUARIO`) e a resposta (`IA`) "
            "(`AIService.salvarTroca`). Se a IA não responder, nada é gravado.",
            "Textos maiores que 4000 caracteres são cortados antes de gravar. A pergunta do usuário é "
            "limitada a 500 caracteres na entrada (`ChatRequest`).",
            "As últimas 10 mensagens (`findTop10ByUsuarioEmailOrderByIdDesc`) vão junto como histórico "
            "para a IA lembrar da conversa.",
            "<b>Ciclo de vida:</b> todas as mensagens do usuário são apagadas no login e no logout "
            "(`deleteByUsuarioId` em `AuthService`). Cada sessão começa com uma conversa nova.",
        ],
    },
]

# Diagrama: (tabela, coluna, tipo curto, marcação) na ordem das entidades.
DIAGRAMA = {
    "usuarios": [("id", "bigint", "PK"), ("nome", "varchar(255)", ""), ("email", "varchar(255)", "UK"),
                 ("senha", "varchar(255)", ""), ("token_version", "int", "")],
    "categorias": [("id", "bigint", "PK"), ("nome", "varchar(100)", "UK"), ("descricao", "varchar(255)", "?"),
                   ("usuario_id", "bigint", "FK UK")],
    "movimentacoes": [("id", "bigint", "PK"), ("descricao", "varchar(255)", "?"), ("valor", "decimal(15,2)", ""),
                      ("data", "date", ""), ("tipo", "enum", ""), ("usuario_id", "bigint", "FK"),
                      ("categoria_id", "bigint", "FK ?")],
    "metas": [("id", "bigint", "PK"), ("descricao", "varchar(255)", ""), ("valor", "decimal(15,2)", ""),
              ("usuario_id", "bigint", "FK")],
    "chat_mensagens": [("id", "bigint", "PK"), ("papel", "enum", ""), ("texto", "varchar(4000)", ""),
                       ("criada_em", "datetime(6)", ""), ("usuario_id", "bigint", "FK")],
}


# ---------------------------------------------------------------- diagrama ER
class DiagramaER(Flowable):
    """Diagrama entidade-relacionamento em notação pé de galinha, desenhado no canvas."""

    LARG_CAIXA = 200
    ALT_CAB = 17
    ALT_LINHA = 13.5

    def __init__(self, largura):
        super().__init__()
        self.largura = largura
        self.altura = 455
        m = 26                                  # margem à esquerda (corredor da linha de chat)
        x_esq = m
        x_dir = largura - self.LARG_CAIXA - 2
        self.pos = {                            # canto superior esquerdo (y medido de cima para baixo)
            "usuarios": (x_esq, 18),
            "metas": (x_esq, 175),
            "chat_mensagens": (x_esq, 300),
            "categorias": (x_dir, 18),
            "movimentacoes": (x_dir, 160),
        }

    def wrap(self, aw, ah):
        return self.largura, self.altura

    # coordenadas -------------------------------------------------------
    def _y(self, y_topo):
        return self.altura - y_topo

    def _caixa(self, nome):
        x, y = self.pos[nome]
        alt = self.ALT_CAB + self.ALT_LINHA * len(DIAGRAMA[nome]) + 4
        return x, y, self.LARG_CAIXA, alt          # x, topo, largura, altura

    def _y_linha(self, nome, indice):
        """Centro vertical (coordenada do canvas) da linha `indice` da caixa."""
        x, topo, w, h = self._caixa(nome)
        return self._y(topo + self.ALT_CAB + 2 + self.ALT_LINHA * indice + self.ALT_LINHA / 2)

    # desenho -----------------------------------------------------------
    def _desenhar_caixa(self, cv, nome):
        x, topo, w, h = self._caixa(nome)
        y0 = self._y(topo + h)
        cv.setFillColor(colors.white)
        cv.setStrokeColor(CAB_TABELA)
        cv.setLineWidth(0.9)
        cv.roundRect(x, y0, w, h, 3, stroke=1, fill=1)
        # cabeçalho
        cv.setFillColor(CAB_TABELA)
        p = cv.beginPath()
        yc = self._y(topo + self.ALT_CAB)
        p.moveTo(x, yc)
        p.lineTo(x, self._y(topo) - 3)
        p.arcTo(x, self._y(topo) - 6, x + 6, self._y(topo), startAng=180, extent=-90)
        p.lineTo(x + w - 3, self._y(topo))
        p.arcTo(x + w - 6, self._y(topo) - 6, x + w, self._y(topo), startAng=90, extent=-90)
        p.lineTo(x + w, yc)
        p.close()
        cv.drawPath(p, stroke=0, fill=1)
        cv.setFillColor(colors.white)
        cv.setFont("Texto-Negrito", 9)
        cv.drawString(x + 7, yc + 5, nome)
        # linhas
        for i, (col, tipo, marca) in enumerate(DIAGRAMA[nome]):
            yl = self._y_linha(nome, i) - 3
            if i % 2 == 1:
                cv.setFillColor(FUNDO)
                cv.rect(x + 0.6, yl - 3.6, w - 1.2, self.ALT_LINHA, stroke=0, fill=1)
            chave = marca.replace("?", "").strip()
            if "PK" in chave:
                cv.setFillColor(PK_COR)
            elif "FK" in chave:
                cv.setFillColor(FK_COR)
            else:
                cv.setFillColor(SUAVE)
            cv.setFont("Texto-Negrito", 6.6)
            cv.drawString(x + 6, yl, chave)
            cv.setFillColor(TINTA)
            cv.setFont("Mono-Negrito" if "PK" in chave else "Mono", 8)
            cv.drawString(x + 34, yl, col)
            cv.setFillColor(SUAVE)
            cv.setFont("Mono", 7.4)
            tipo_txt = tipo + ("  NULL" if "?" in marca else "")
            cv.drawRightString(x + w - 6, yl, tipo_txt)

    def _marca_um(self, cv, x, y, dx, dy, opcional=False):
        """Extremidade 'um': || (exatamente um) ou o| (zero ou um). (dx, dy) aponta para fora da caixa."""
        px, py = -dy, dx                     # perpendicular
        for d in (6, 10):
            cx, cy = x + dx * d, y + dy * d
            if opcional and d == 10:
                cv.setFillColor(colors.white)
                cv.circle(x + dx * 12, y + dy * 12, 3, stroke=1, fill=1)
            else:
                cv.line(cx - px * 4.5, cy - py * 4.5, cx + px * 4.5, cy + py * 4.5)

    def _marca_muitos(self, cv, x, y, dx, dy):
        """Extremidade 'zero ou muitos': pé de galinha com círculo."""
        px, py = -dy, dx
        base_x, base_y = x + dx * 9, y + dy * 9
        for s in (-5, 0, 5):
            cv.line(base_x, base_y, x + px * s, y + py * s)
        cv.setFillColor(colors.white)
        cv.circle(x + dx * 13, y + dy * 13, 3, stroke=1, fill=1)

    def _rotulo(self, cv, x, y, texto, alinhar="esq"):
        cv.setFont("Texto-Italico", 7.4)
        cv.setFillColor(SUAVE)
        if alinhar == "esq":
            cv.drawString(x, y, texto)
        elif alinhar == "dir":
            cv.drawRightString(x, y, texto)
        else:
            cv.drawCentredString(x, y, texto)

    def draw(self):
        cv = self.canv
        cv.saveState()
        cv.setLineWidth(0.9)
        cv.setStrokeColor(TINTA)

        ux, ut, uw, uh = self._caixa("usuarios")
        cx_, ct, cw, ch = self._caixa("categorias")
        mx, mt, mw, mh = self._caixa("movimentacoes")
        ex, et, ew, eh = self._caixa("metas")
        hx, ht, hw, hh = self._caixa("chat_mensagens")
        dir_u = ux + uw

        # 1) usuarios 1 — N categorias (horizontal reta, na altura de usuarios.id / categorias.usuario_id)
        y1 = self._y_linha("categorias", 3)
        cv.line(dir_u, y1, cx_, y1)
        self._marca_um(cv, dir_u, y1, 1, 0)
        self._marca_muitos(cv, cx_, y1, -1, 0)
        self._rotulo(cv, (dir_u + cx_) / 2, y1 + 4, "cria", "centro")

        # 2) usuarios 1 — N movimentacoes (cotovelo pelo corredor entre as colunas)
        y_saida = self._y_linha("usuarios", 4)
        y_cheg = self._y_linha("movimentacoes", 5)
        x_corr = (dir_u + mx) / 2
        cv.line(dir_u, y_saida, x_corr, y_saida)
        cv.line(x_corr, y_saida, x_corr, y_cheg)
        cv.line(x_corr, y_cheg, mx, y_cheg)
        self._marca_um(cv, dir_u, y_saida, 1, 0)
        self._marca_muitos(cv, mx, y_cheg, -1, 0)
        cv.saveState()
        cv.translate(x_corr - 4, (y_saida + y_cheg) / 2)
        cv.rotate(90)
        self._rotulo(cv, 0, 0, "registra", "centro")
        cv.restoreState()

        # 3) categorias 0..1 — N movimentacoes (vertical na coluna da direita)
        x3 = cx_ + cw - 40
        y_top = self._y(ct + ch)
        y_bot = self._y(mt)
        cv.line(x3, y_top, x3, y_bot)
        self._marca_um(cv, x3, y_top, 0, -1, opcional=True)
        self._marca_muitos(cv, x3, y_bot, 0, 1)
        self._rotulo(cv, x3 + 7, (y_top + y_bot) / 2 - 3, "classifica")

        # 4) usuarios 1 — N metas (vertical na coluna da esquerda)
        x4 = ux + uw / 2
        y_top = self._y(ut + uh)
        y_bot = self._y(et)
        cv.line(x4, y_top, x4, y_bot)
        self._marca_um(cv, x4, y_top, 0, -1)
        self._marca_muitos(cv, x4, y_bot, 0, 1)
        self._rotulo(cv, x4 + 7, (y_top + y_bot) / 2 - 3, "define")

        # 5) usuarios 1 — N chat_mensagens (pelo corredor da margem esquerda)
        y_saida = self._y_linha("usuarios", 0)
        y_cheg = self._y_linha("chat_mensagens", 4)
        x_corr = ux - 16
        cv.line(ux, y_saida, x_corr, y_saida)
        cv.line(x_corr, y_saida, x_corr, y_cheg)
        cv.line(x_corr, y_cheg, hx, y_cheg)
        self._marca_um(cv, ux, y_saida, -1, 0)
        self._marca_muitos(cv, hx, y_cheg, 1, 0)
        cv.saveState()
        cv.translate(x_corr - 4, (y_saida + y_cheg) / 2)
        cv.rotate(90)
        self._rotulo(cv, 0, 0, "conversa", "centro")
        cv.restoreState()

        for nome in DIAGRAMA:
            self._desenhar_caixa(cv, nome)

        self._legenda(cv, ex + 0, 8)
        cv.restoreState()

    def _legenda(self, cv, x, y_base):
        """Legenda da notação no canto inferior direito."""
        lx = self.largura - self.LARG_CAIXA - 2
        ly = y_base + 92
        cv.setFillColor(FUNDO)
        cv.setStrokeColor(LINHA)
        cv.roundRect(lx, y_base, self.LARG_CAIXA, 104, 3, stroke=1, fill=1)
        cv.setFillColor(TINTA)
        cv.setFont("Texto-Negrito", 8)
        cv.drawString(lx + 8, ly, "Legenda")
        cv.setStrokeColor(TINTA)
        itens = [
            ("um", "exatamente um"),
            ("um_opc", "zero ou um"),
            ("muitos", "zero ou muitos"),
        ]
        yy = ly - 16
        for tipo, texto in itens:
            x0, x1 = lx + 10, lx + 52
            cv.line(x0, yy + 2.5, x1, yy + 2.5)
            if tipo == "muitos":
                self._marca_muitos(cv, x1, yy + 2.5, -1, 0)
            else:
                self._marca_um(cv, x1, yy + 2.5, -1, 0, opcional=(tipo == "um_opc"))
            cv.setFillColor(TINTA)
            cv.setFont("Texto", 7.8)
            cv.drawString(x1 + 8, yy, texto)
            yy -= 15
        cv.setFont("Texto", 7.4)
        cv.setFillColor(PK_COR)
        cv.drawString(lx + 10, yy, "PK")
        cv.setFillColor(TINTA)
        cv.drawString(lx + 24, yy, "chave primária")
        cv.setFillColor(FK_COR)
        cv.drawString(lx + 95, yy, "FK")
        cv.setFillColor(TINTA)
        cv.drawString(lx + 109, yy, "chave estrangeira")
        yy -= 12
        cv.setFillColor(SUAVE)
        cv.drawString(lx + 10, yy, "UK")
        cv.setFillColor(TINTA)
        cv.drawString(lx + 24, yy, "valor único")
        cv.drawString(lx + 95, yy, "NULL  aceita nulo")


# ---------------------------------------------------------------- tabelas
def tabela(linhas, larguras, st, cab=True, fundo_alt=True):
    dados = []
    for i, linha in enumerate(linhas):
        estilo = st["cel_cab"] if (cab and i == 0) else st["cel"]
        dados.append([Paragraph(converter(str(v)), estilo) for v in linha])
    t = Table(dados, colWidths=larguras, repeatRows=1 if cab else 0, hAlign="LEFT")
    cmds = [
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LINEBELOW", (0, 0), (-1, -1), 0.4, LINHA),
        ("BOX", (0, 0), (-1, -1), 0.5, LINHA),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3.5),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
    ]
    if cab:
        cmds.append(("BACKGROUND", (0, 0), (-1, 0), CAB_TABELA))
    if fundo_alt:
        for i in range(1 if cab else 0, len(linhas)):
            if i % 2 == 0:
                cmds.append(("BACKGROUND", (0, i), (-1, i), FUNDO))
    t.setStyle(TableStyle(cmds))
    return t


def itens(lista, st):
    return [Paragraph(converter(txt), st["item"], bulletText="•") for txt in lista]


# ---------------------------------------------------------------- página
def rodape(cv, doc):
    cv.saveState()
    cv.setFont("Texto", 7.5)
    cv.setFillColor(SUAVE)
    cv.setStrokeColor(LINHA)
    cv.setLineWidth(0.5)
    cv.line(doc.leftMargin, 14 * mm, A4[0] - doc.rightMargin, 14 * mm)
    cv.drawString(doc.leftMargin, 10 * mm, "FinLog — Modelo de dados")
    cv.drawRightString(A4[0] - doc.rightMargin, 10 * mm, f"Página {doc.page}")
    cv.restoreState()


def montar():
    registrar_fontes()
    st = estilos()
    doc = SimpleDocTemplate(
        str(SAIDA), pagesize=A4,
        leftMargin=20 * mm, rightMargin=20 * mm, topMargin=18 * mm, bottomMargin=20 * mm,
        title="FinLog — Modelo de dados", author="Equipe FinLog",
        subject="Documentação do modelo de dados (DADOS.pdf)",
    )
    larg = doc.width
    h = []

    # ---------------- capa / visão geral
    h.append(Paragraph("FinLog — Modelo de dados", st["titulo"]))
    h.append(Paragraph("Documentação do banco de dados da API (tabelas, colunas, relacionamentos e regras de acesso)",
                       st["subtitulo"]))

    h.append(Paragraph("Sumário", st["h2"]))
    sumario = [
        "1. Visão geral e criação do esquema",
        "2. Diagrama entidade-relacionamento",
        "3. Tabelas: usuarios, categorias, movimentacoes, metas, chat_mensagens",
        "4. Enumerações",
        "5. Relacionamentos",
        "6. Isolamento dos dados por usuário",
        "7. Como conferir o esquema e regenerar este documento",
    ]
    for s in sumario:
        h.append(Paragraph(s, ParagraphStyle("sum", parent=st["base"], leftIndent=8, spaceAfter=1.5)))
    h.append(Spacer(1, 8))

    h.append(Paragraph("1. Visão geral", st["h1"]))
    h.append(Paragraph(
        "O FinLog guarda os dados financeiros de cada usuário: as <b>movimentações</b> (receitas e despesas), "
        "as <b>categorias</b> que as classificam, as <b>metas</b> de economia e as <b>mensagens da conversa "
        "com a IA</b>. Todo registro pertence a um único usuário, e as consultas sempre filtram pelo "
        "usuário autenticado (seção 6).", st["base"]))
    h.append(tabela([
        ["Item", "Valor"],
        ["Banco de dados", "MySQL 9.7 (imagem `mysql:9.7` em `api/docker-compose.yml`)"],
        ["Nome do banco", "`finlog_db` (padrão de `FINLOG_DB_URL` e `MYSQL_DATABASE`)"],
        ["Acesso aos dados", "Spring Data JPA com Hibernate ORM 7.2 (dialeto `MySQLDialect`)"],
        ["Criação do esquema", "Automática pelo Hibernate: `spring.jpa.hibernate.ddl-auto=update` "
                               "(variável `JPA_DDL_AUTO`)"],
        ["Scripts SQL / migrações", "Não há. O projeto não tem arquivos `.sql`, Flyway nem Liquibase"],
        ["Tabelas", "`usuarios`, `categorias`, `movimentacoes`, `metas`, `chat_mensagens`"],
        ["Entidades JPA", "`api/src/main/java/com/ucb/finlog/model/`"],
        ["Repositórios", "`api/src/main/java/com/ucb/finlog/repository/`"],
    ], [38 * mm, larg - 38 * mm], st))
    h.append(Spacer(1, 6))

    h.append(Paragraph("Como o esquema é criado", st["h2"]))
    h.append(Paragraph(converter(
            "Não existe script SQL de criação. Ao iniciar, a API lê as classes anotadas com `@Entity` e o "
            "Hibernate cria no MySQL as tabelas, colunas, chaves e restrições que ainda não existem "
            "(`ddl-auto=update`). A URL de conexão tem `createDatabaseIfNotExist=true`, então o próprio "
            "banco `finlog_db` também é criado se faltar."), st["base"]))
    h.extend(itens([
        "Os nomes dos atributos Java em camelCase viram colunas em snake_case (por exemplo, `tokenVersion` → "
        "`token_version` e `criadaEm` → `criada_em`).",
        "Os tipos MySQL desta documentação foram conferidos com `SHOW CREATE TABLE` em um banco vazio criado "
        "pela versão atual da API.",
        "O `update` só <b>acrescenta</b> o que falta: não remove colunas e não muda colunas que já existem. "
        "Um banco criado por uma versão anterior pode ficar diferente do mapeamento atual (por exemplo, com "
        "uma coluna aceitando NULL). Para recriar o banco do Docker do zero, use `docker compose down -v` "
        "dentro de `api/`. Esse comando <b>apaga todos os dados</b> do volume `mysql_finlog`.",
        "As tabelas usam InnoDB com `utf8mb4` e a collation `utf8mb4_0900_ai_ci`, que é o padrão do MySQL "
        "9.7 (nada disso é configurado no projeto). Essa collation compara textos sem diferenciar "
        "maiúsculas, minúsculas e acentos.",
    ], st))

    # ---------------- diagrama
    h.append(PageBreak())
    h.append(Paragraph("2. Diagrama entidade-relacionamento", st["h1"]))
    h.append(Paragraph(converter(
        "Cada caixa é uma tabela, com as colunas na ordem dos atributos da entidade. As linhas ligam a chave "
        "estrangeira à tabela referenciada e mostram a cardinalidade em notação “pé de galinha”. "
        "NULL indica coluna que aceita valor nulo. A fonte editável do diagrama é "
        "`docs/modelo-de-dados.mmd` (Mermaid)."), st["base"]))
    h.append(Spacer(1, 6))
    h.append(DiagramaER(larg))
    h.append(Spacer(1, 8))
    h.append(tabela([
        ["Ligação", "Leitura"],
        ["usuarios → categorias", "Um usuário cria zero ou muitas categorias; cada categoria tem exatamente um dono."],
        ["usuarios → movimentacoes", "Um usuário registra zero ou muitas movimentações; cada movimentação tem exatamente um dono."],
        ["categorias → movimentacoes", "Uma categoria classifica zero ou muitas movimentações; cada movimentação tem zero ou uma categoria."],
        ["usuarios → metas", "Um usuário define zero ou muitas metas; cada meta tem exatamente um dono."],
        ["usuarios → chat_mensagens", "Um usuário tem zero ou muitas mensagens na conversa com a IA; cada mensagem tem exatamente um dono."],
    ], [46 * mm, larg - 46 * mm], st))

    # ---------------- tabelas
    h.append(PageBreak())
    h.append(Paragraph("3. Tabelas", st["h1"]))
    h.append(Paragraph(converter(
        "Legenda: <b>PK</b> = chave primária, <b>FK</b> = chave estrangeira, <b>UK</b> = valor único. Todas as "
        "chaves primárias são `bigint` geradas pelo próprio MySQL (`AUTO_INCREMENT`, mapeado como "
        "`GenerationType.IDENTITY`)."), st["base"]))
    larguras = [27 * mm, 25 * mm, 29 * mm, 31 * mm, 12 * mm]
    larguras.append(larg - sum(larguras))
    for n, t in enumerate(TABELAS, start=1):
        linhas = [["Coluna", "Atributo Java", "Tipo Java", "Tipo MySQL", "Nulo", "Restrições"]]
        for col, attr, tj, tsql, nulo, restr in t["colunas"]:
            linhas.append([f"`{col}`", f"`{attr}`", f"`{tj}`", f"`{tsql}`", "sim" if nulo else "não", restr])
        bloco = [
            CondPageBreak(60 * mm),
            Paragraph(converter(f"3.{n} `{t['nome']}` <font color='#57606A' size='9'>(entidade `{t['entidade']}`)</font>"),
                      st["h2"]),
            Paragraph(t["resumo"], st["base"]),
            tabela(linhas, larguras, st),
            Spacer(1, 5),
        ]
        h.append(KeepTogether(bloco))
        h.extend(itens(t["notas"], st))
        h.append(Spacer(1, 4))

    # ---------------- enums
    h.append(CondPageBreak(80 * mm))
    h.append(Paragraph("4. Enumerações", st["h1"]))
    h.append(Paragraph(converter(
        "Os dois enums são gravados pelo <b>nome</b> (`EnumType.STRING`), e não pela posição. Por isso, mudar "
        "a ordem das constantes no Java não altera os dados. No MySQL o Hibernate cria uma coluna do tipo "
        "`enum` nativo com os nomes em ordem alfabética."), st["base"]))
    h.append(tabela([
        ["Enum (Java)", "Coluna", "Valor", "Significado"],
        ["`TipoMovimentacao`", "`movimentacoes.tipo`", "`RECEITA`", "entrada de dinheiro (soma no saldo)"],
        ["", "", "`DESPESA`", "saída de dinheiro (subtrai do saldo)"],
        ["`PapelMensagem`", "`chat_mensagens.papel`", "`USUARIO`", "mensagem escrita pelo usuário"],
        ["", "", "`IA`", "resposta gerada pelo assistente de IA"],
    ], [36 * mm, 38 * mm, 24 * mm, larg - 98 * mm], st, fundo_alt=False))
    h.append(Spacer(1, 4))
    h.append(Paragraph(converter(
        "Para incluir um valor novo num enum, é preciso ajustar a coluna no banco, porque o "
        "`ddl-auto=update` não altera colunas que já existem."), st["nota"]))

    # ---------------- relacionamentos
    h.append(CondPageBreak(50 * mm))
    h.append(Paragraph("5. Relacionamentos", st["h1"]))
    h.append(tabela([
        ["Origem (N)", "Destino (1)", "Chave estrangeira", "Cardinalidade", "Obrigatório", "Carregamento"],
        ["`categorias`", "`usuarios`", "`categorias.usuario_id`", "N : 1", "sim", "LAZY"],
        ["`movimentacoes`", "`usuarios`", "`movimentacoes.usuario_id`", "N : 1", "sim", "LAZY"],
        ["`movimentacoes`", "`categorias`", "`movimentacoes.categoria_id`", "N : 0..1", "não", "EAGER (padrão)"],
        ["`metas`", "`usuarios`", "`metas.usuario_id`", "N : 1", "sim", "LAZY"],
        ["`chat_mensagens`", "`usuarios`", "`chat_mensagens.usuario_id`", "N : 1", "sim", "LAZY"],
    ], [28 * mm, 21 * mm, 46 * mm, 23 * mm, 20 * mm, larg - 138 * mm], st))
    h.append(Spacer(1, 5))
    h.extend(itens([
        "Nenhum relacionamento tem `cascade` nem `orphanRemoval`. As chaves estrangeiras são criadas sem "
        "`ON DELETE`, então o MySQL bloqueia a exclusão de uma linha que ainda é referenciada.",
        "O lado inverso só está mapeado em `Usuario.movimentacoes` (`mappedBy = \"usuario\"`), usado apenas "
        "pelo JPA e escondido do JSON.",
        "A API não tem rota para excluir usuários.",
    ], st))

    # ---------------- isolamento
    h.append(CondPageBreak(90 * mm))
    h.append(Paragraph("6. Isolamento dos dados por usuário", st["h1"]))
    h.append(Paragraph(
        "Cada usuário só enxerga e altera os próprios registros. A regra fica nas consultas dos "
        "repositórios e não depende do que o cliente envia:", st["base"]))
    passos = [
        "O `AuthTokenFilter` valida o token, busca o usuário pelo `id` gravado no token, confere a "
        "`token_version` e coloca no contexto um `UsuarioAutenticado(id, email)`.",
        "Os controllers recebem esse usuário com `@AuthenticationPrincipal` e passam o e-mail dele aos "
        "serviços. O `id` do usuário nunca vem do corpo da requisição.",
        "Toda consulta dos repositórios filtra pelo e-mail do dono (`...AndUsuarioEmail` / "
        "`findByUsuarioEmail...`), o que gera um JOIN com `usuarios` e um `WHERE usuarios.email = ?`.",
    ]
    for i, p in enumerate(passos, start=1):
        h.append(Paragraph(converter(p), st["item"], bulletText=f"{i}."))
    h.append(Spacer(1, 4))
    h.append(tabela([
        ["Repositório", "Consulta", "O que garante"],
        ["`MovimentacaoRepository`", "`findByUsuarioEmail`", "lista só as movimentações do usuário"],
        ["", "`findByIdAndUsuarioEmail`", "buscar, editar ou excluir por id só acha registro do próprio usuário (senão 404)"],
        ["", "`findByCategoriaIdAndUsuarioEmail`<br/>`existsByCategoriaIdAndUsuarioEmail`",
         "movimentações de uma categoria, só do usuário"],
        ["", "`somarPorCategoriaETipo` (JPQL)", "soma `valor` por categoria e tipo com "
                                               "`where m.usuario.email = :email`"],
        ["`CategoriaRepository`", "`findByUsuarioEmailOrderByNomeAsc`", "lista as categorias do usuário em ordem alfabética"],
        ["", "`findByIdAndUsuarioEmail`", "impede usar ou alterar a categoria de outro usuário (404)"],
        ["", "`findByNomeIgnoreCaseAndUsuarioEmail`<br/>`existsByNomeIgnoreCaseAndUsuarioEmail`",
         "procura nomes repetidos só entre as categorias do usuário"],
        ["`MetaRepository`", "`findByUsuarioEmailOrderByIdAsc`<br/>`findByIdAndUsuarioEmail`",
         "lista e altera só as metas do usuário"],
        ["`ChatMensagemRepository`", "`findByUsuarioEmailOrderByIdAsc`<br/>`findTop10ByUsuarioEmailOrderByIdDesc`",
         "histórico da conversa só do usuário"],
        ["", "`deleteByUsuarioId`", "apaga só as mensagens do usuário que entrou ou saiu"],
        ["`UsuarioRepository`", "`findByEmail`<br/>`existsByEmail`", "localiza o dono e impede e-mail duplicado no cadastro"],
    ], [40 * mm, 66 * mm, larg - 106 * mm], st, fundo_alt=False))
    h.append(Spacer(1, 6))
    h.append(Paragraph(converter(
        "Quando um registro existe mas pertence a outro usuário, a API responde <b>404 (não encontrado)</b> "
        "em vez de 403. Assim ela nem revela que o registro existe. Ao criar registros, o serviço liga o "
        "novo registro ao usuário autenticado (`setUsuario`), sem ler nenhum dono enviado pelo cliente."),
        st["base"]))

    # ---------------- conferência
    h.append(CondPageBreak(45 * mm))
    h.append(Paragraph("7. Como conferir o esquema e regenerar este documento", st["h1"]))
    h.append(Paragraph(converter(
        "Com a API e o MySQL rodando pelo `docker compose`, estes comandos, executados na pasta `api/` "
        "em um terminal Bash (Git Bash, Linux ou macOS), listam as tabelas e mostram o DDL que o "
        "Hibernate criou:"), st["base"]))
    cmd = ("docker compose exec mysql-db sh -c 'mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" "
           "\"$MYSQL_DATABASE\" -e \"SHOW TABLES;\"'<br/>"
           "docker compose exec mysql-db sh -c 'mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" "
           "\"$MYSQL_DATABASE\" -E -e \"SHOW CREATE TABLE movimentacoes;\"'")
    h.append(Paragraph(cmd.replace('"', "&quot;"), st["codigo"]))
    h.append(Paragraph(converter(
        "Para gerar este PDF de novo depois de editar o conteúdo: `python docs/fontes/gerar_dados_pdf.py`. "
        "O texto equivalente, editável, está em `docs/DADOS.md`."), st["base"]))

    doc.build(h, onFirstPage=rodape, onLaterPages=rodape)
    print(f"PDF gerado: {SAIDA}")


if __name__ == "__main__":
    montar()
