const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

export function brl(n) {
  return 'R$ ' + Math.abs(n).toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

/* Valor com sinal na frente, como no protótipo: "+ R$ 500,00" / "- R$ 500,00" */
export function brlSinal(type, n) {
  return `${type === 'saida' ? '-' : '+'} ${brl(n)}`
}

/* Data local no formato AAAA-MM-DD (toISOString usaria UTC e viraria o dia à noite) */
export function isoLocal(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function today() {
  return isoLocal(new Date())
}

export function fmtDate(d) {
  if (!d) return ''
  const [y, m, day] = d.split('-')
  return `${day}/${m}/${y}`
}

function somarDias(iso, dias) {
  const [y, m, d] = iso.split('-').map(Number)
  return isoLocal(new Date(y, m - 1, d + dias))
}

/* "Hoje", "Ontem", "24 Out" (ou "24 Out 2025" quando não é do ano corrente).
   A API só guarda a data, então não há horário. */
export function labelData(iso, hoje = new Date()) {
  if (!iso) return ''
  const ref = isoLocal(hoje)
  if (iso === ref) return 'Hoje'
  if (iso === somarDias(ref, -1)) return 'Ontem'
  const [y, m, d] = iso.split('-')
  const base = `${d} ${MESES[Number(m) - 1]}`
  return Number(y) === hoje.getFullYear() ? base : `${base} ${y}`
}

/* Horário das mensagens do chat: "Hoje, 10:15" ou "24 Out, 10:15" */
export function horaMensagem(date, hoje = new Date()) {
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${labelData(isoLocal(date), hoje)}, ${hh}:${mm}`
}

/* Intervalo [inicio, fim] (inclusive, AAAA-MM-DD) de um filtro de período.
   Devolve null quando não há restrição. */
export function intervaloPeriodo(periodo, hoje = new Date(), personalizado = {}) {
  const ref = isoLocal(hoje)
  if (periodo === 'mes') {
    return { inicio: `${ref.slice(0, 7)}-01`, fim: isoLocal(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0)) }
  }
  if (periodo === '30dias') {
    return { inicio: somarDias(ref, -29), fim: ref }
  }
  if (periodo === 'personalizado') {
    const { de = '', ate = '' } = personalizado
    if (!de && !ate) return null
    return { inicio: de || '0000-01-01', fim: ate || '9999-12-31' }
  }
  return null
}

/* Mais recentes primeiro: pela data e, no mesmo dia, pelo id */
export function ordenarRecentes(txns) {
  return [...txns].sort((a, b) =>
    (b.date || '').localeCompare(a.date || '') || (b.id ?? 0) - (a.id ?? 0)
  )
}

/* Filtra as movimentações por tipo, categoria e período */
export function filtrarMovimentacoes(txns, filtros = {}, hoje = new Date()) {
  const { tipo = 'all', categoria = 'all', periodo = 'tudo', de = '', ate = '' } = filtros
  const intervalo = intervaloPeriodo(periodo, hoje, { de, ate })
  return txns
    .filter(t => tipo === 'all' || t.type === tipo)
    .filter(t => categoria === 'all' || t.cat === categoria)
    .filter(t => !intervalo || (t.date >= intervalo.inicio && t.date <= intervalo.fim))
}

export function totais(txns) {
  const entradas = txns.filter(t => t.type === 'entrada').reduce((s, t) => s + t.val, 0)
  const saidas   = txns.filter(t => t.type === 'saida'  ).reduce((s, t) => s + t.val, 0)
  return { entradas, saidas, saldo: entradas - saidas }
}

/* Percentual com uma casa no padrão brasileiro: "54,6%" */
export function pct(parte, total) {
  const v = total > 0 ? (parte / total) * 100 : 0
  return v.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '%'
}

export function iniciais(nome) {
  if (!nome?.trim()) return 'U'
  return nome.trim().split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase()
}

export function mapMeta(m) {
  return { id: m.id, desc: m.descricao, val: parseFloat(m.valor) || 0 }
}
export function mapMovimentacao(m) {
  return {
    id:    m.id,
    type:  m.tipo === 'RECEITA' ? 'entrada' : 'saida',
    desc:  m.descricao,
    val:   parseFloat(m.valor) || 0,
    date:  m.data,
    cat:   m.categoria?.nome || 'Outros',
    catId: m.categoria?.id ?? null,
  }
}

export function stripMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '$1')   // **negrito**
    .replace(/\*(.*?)\*/g, '$1')        // *itálico*
    .replace(/#{1,6}\s*/g, '')          // ## títulos
    .replace(/`(.*?)`/g, '$1')          // `código inline`
    .replace(/^\s*[-*+]\s+/gm, '')      // listas com marcadores
    .replace(/^\s*\d+\.\s+/gm, '')      // listas numeradas
    .trim()
}
