import Icon from './Icon'

/* Estrutura comum dos modais: fundo escurecido (clique fora fecha),
   cabeçalho com título, subtítulo e botão redondo de fechar.
   O Escape é tratado por quem abre o modal (Dashboard). */
function Modal({ id, title, subtitle, onClose, narrow = false, describedBy, children }) {
  const titleId = `${id}-title`
  const subtitleId = subtitle ? `${id}-subtitle` : undefined

  return (
    <div className="overlay on" onClick={e => { if (e.target === e.currentTarget) onClose() }}>
      <div
        className={`modal${narrow ? ' narrow' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={describedBy || subtitleId}
      >
        <div className="m-header">
          <div>
            <h2 className="m-title" id={titleId}>{title}</h2>
            {subtitle && <p className="m-subtitle" id={subtitleId}>{subtitle}</p>}
          </div>
          <button type="button" className="m-close" onClick={onClose} aria-label="Fechar">
            <Icon name="x" size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

export default Modal
