/* Modal de lançamento, usado tanto para criar quanto para editar.
   O seletor Entrada/Saída só aparece quando onTipoChange é informado. */
function LancamentoModal({
  idPrefix,
  title,
  form,
  setForm,
  categorias,
  tipo,
  onTipoChange,
  descPlaceholder,
  valPlaceholder,
  submitLabel,
  onSubmit,
  salvando,
  onClose,
}) {
  const titleId = `${idPrefix}-modal-title`

  return (
    <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) onClose() }}>
      <div className="modal" role="dialog" aria-modal="true" aria-labelledby={titleId}>
        <button className="m-close" onClick={onClose} aria-label="Fechar">✕</button>
        <div className="m-title" id={titleId}>{title}</div>
        {onTipoChange && (
          <div className="m-type-row">
            <button className={`m-type-btn${tipo === 'entrada' ? ' e' : ''}`} onClick={() => onTipoChange('entrada')}>Entrada +</button>
            <button className={`m-type-btn${tipo === 'saida'   ? ' s' : ''}`} onClick={() => onTipoChange('saida')}>Saída –</button>
          </div>
        )}
        <div className="m-row">
          <label className="m-lbl" htmlFor={`${idPrefix}-desc`}>Descrição:</label>
          <input id={`${idPrefix}-desc`} className="m-inp" type="text" maxLength={255} placeholder={descPlaceholder} value={form.desc} onChange={e => setForm(f => ({ ...f, desc: e.target.value }))} />
        </div>
        <div className="m-row">
          <label className="m-lbl" htmlFor={`${idPrefix}-val`}>Valor:</label>
          <input id={`${idPrefix}-val`} className="m-inp" type="number" placeholder={valPlaceholder} min="0.01" step="0.01" value={form.val} onChange={e => setForm(f => ({ ...f, val: e.target.value }))} />
        </div>
        <div className="m-row">
          <label className="m-lbl" htmlFor={`${idPrefix}-date`}>Data:</label>
          <input id={`${idPrefix}-date`} className="m-inp" type="date" value={form.date} onChange={e => setForm(f => ({ ...f, date: e.target.value }))} />
        </div>
        <div className="m-row">
          <label className="m-lbl" htmlFor={`${idPrefix}-cat`}>Categoria:</label>
          <select id={`${idPrefix}-cat`} className="m-inp" value={form.catId} onChange={e => setForm(f => ({ ...f, catId: e.target.value, catNomeNovo: '' }))}>
            <option value="">Selecione uma categoria</option>
            {categorias.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
            <option value="__nova__">+ Nova categoria…</option>
          </select>
        </div>
        {form.catId === '__nova__' && (
          <div className="m-row">
            <label className="m-lbl" htmlFor={`${idPrefix}-cat-nova`}>Nova categoria:</label>
            <input id={`${idPrefix}-cat-nova`} className="m-inp" type="text" placeholder="Ex: Salário, Alimentação…" value={form.catNomeNovo} onChange={e => setForm(f => ({ ...f, catNomeNovo: e.target.value }))} />
          </div>
        )}
        <button className="m-btn" onClick={onSubmit} disabled={salvando}>{submitLabel}</button>
      </div>
    </div>
  )
}

export default LancamentoModal
