function DonutChart({ entTotal, saiTotal }) {
  const total = entTotal + saiTotal
  const r     = 40
  const cx    = 54
  const cy    = 54
  const circ  = 2 * Math.PI * r

  if (total === 0) {
    return (
      <svg width="108" height="108">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#DDD9D2" strokeWidth="15" />
      </svg>
    )
  }

  const entArc = (entTotal / total) * circ
  const saiArc = (saiTotal / total) * circ
  const offset = circ / 4

  return (
    <svg width="108" height="108">
      <circle
        cx={cx} cy={cy} r={r}
        fill="none" stroke="#8FA98B" strokeWidth="15"
        strokeDasharray={`${entArc} ${circ - entArc}`}
        strokeDashoffset={offset}
        strokeLinecap="round"
      />
      {saiTotal > 0 && (
        <circle
          cx={cx} cy={cy} r={r}
          fill="none" stroke="#C4A882" strokeWidth="15"
          strokeDasharray={`${saiArc} ${circ - saiArc}`}
          strokeDashoffset={offset - entArc}
          strokeLinecap="round"
        />
      )}
    </svg>
  )
}

export default DonutChart
