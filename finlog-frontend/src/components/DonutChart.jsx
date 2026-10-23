/* Rosca Entradas x Saídas. O conteúdo central (total) é sobreposto em HTML
   por quem usa o gráfico. As cores vêm das classes em styles.css. */
function DonutChart({ entTotal, saiTotal }) {
  const total = entTotal + saiTotal
  const size  = 240
  const sw    = 28
  const r     = (size - sw) / 2
  const c     = size / 2
  const circ  = 2 * Math.PI * r

  const label = total === 0
    ? 'Sem movimentações'
    : `Entradas ${Math.round((entTotal / total) * 100)}%, saídas ${Math.round((saiTotal / total) * 100)}%`

  if (total === 0) {
    return (
      <svg viewBox={`0 0 ${size} ${size}`} role="img" aria-label={label}>
        <circle cx={c} cy={c} r={r} fill="none" className="donut-empty" strokeWidth={sw} />
      </svg>
    )
  }

  const entArc = (entTotal / total) * circ
  const saiArc = (saiTotal / total) * circ

  return (
    <svg viewBox={`0 0 ${size} ${size}`} role="img" aria-label={label}>
      <g transform={`rotate(-90 ${c} ${c})`}>
        {entTotal > 0 && (
          <circle
            cx={c} cy={c} r={r}
            fill="none" className="donut-ent" strokeWidth={sw}
            strokeDasharray={`${entArc} ${circ - entArc}`}
          />
        )}
        {saiTotal > 0 && (
          <circle
            cx={c} cy={c} r={r}
            fill="none" className="donut-sai" strokeWidth={sw}
            strokeDasharray={`${saiArc} ${circ - saiArc}`}
            strokeDashoffset={-entArc}
          />
        )}
      </g>
    </svg>
  )
}

export default DonutChart
