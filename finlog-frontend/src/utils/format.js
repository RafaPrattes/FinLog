export function brl(n) {
  return 'R$' + Math.abs(n).toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
export function today() {
  return new Date().toISOString().split('T')[0]
}
export function fmtDate(d) {
  if (!d) return ''
  const [y, m, day] = d.split('-')
  return `${day}/${m}/${y}`
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
