/* Cabeçalho das telas logadas: título, subtítulo e ações à direita */
export function PageHeader({ title, subtitle, children }) {
  return (
    <header className="page-header">
      <div>
        <h1 className="page-title">{title}</h1>
        {subtitle && <p className="page-subtitle">{subtitle}</p>}
      </div>
      {children && <div className="page-actions">{children}</div>}
    </header>
  )
}

export function SummaryCard({ label, value, tone = '' }) {
  return (
    <div className="card summary-card">
      <div className="summary-label">{label}</div>
      <div className={`summary-value${tone ? ' ' + tone : ''}`}>{value}</div>
    </div>
  )
}

/* Carregando / erro com "Tentar novamente" / lista vazia; senão, o conteúdo */
export function DataState({ loading, error, onRetry, empty, emptyMsg, children }) {
  if (loading) return <div className="empty-msg">Carregando…</div>
  if (error) {
    return (
      <div className="empty-msg" role="alert">
        Não foi possível carregar seus dados.
        <div>
          <button type="button" className="link-btn" onClick={onRetry}>Tentar novamente</button>
        </div>
      </div>
    )
  }
  if (empty) return <div className="empty-msg">{emptyMsg}</div>
  return children
}
