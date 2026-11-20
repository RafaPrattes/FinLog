import Modal from './Modal'

/* Modal de lançamento, usado tanto para criar quanto para editar.
   Segue o frame "Modal novo lançamento" do protótipo: tipo, valor e data,
   descrição e, depois, a categoria (que não está no protótipo). */
function LancamentoModal({
  idPrefix,
  title,
  subtitle,
  form,
  setForm,
  categorias,
  tipo,
  onTipoChange,
  descPlaceholder = 'Ex.: Supermercado, salário, aluguel...',
  valPlaceholder = '0,00',
  submitLabel = 'Salvar lançamento',
  onSubmit,
  salvando,
  onClose,
}) {
  function submit(e) {
    e.preventDefault()
    onSubmit()
  }

  return (
    <Modal id={`${idPrefix}-modal`} title={title} subtitle={subtitle} onClose={onClose}>
      <form className="m-form" onSubmit={submit} noValidate>
        {onTipoChange && (
          <div className="seg" role="radiogroup" aria-label="Tipo do lançamento">
            {[['entrada', 'Entrada'], ['saida', 'Saída']].map(([valor, rotulo]) => (
              <button
                key={valor}
                type="button"
                role="radio"
                aria-checked={tipo === valor}
                className={`seg-btn ${valor}${tipo === valor ? ' active' : ''}`}
                onClick={() => onTipoChange(valor)}
              >
                <span className="seg-dot" aria-hidden="true" />
                {rotulo}
              </button>
            ))}
          </div>
        )}

        <div className="m-grid">
          <div className="m-field">
            <label className="m-lbl" htmlFor={`${idPrefix}-val`}>Valor</label>
            <div className="m-prefix-wrap">
              <span className="m-prefix" aria-hidden="true">R$</span>
              <input
                id={`${idPrefix}-val`}
                className="m-inp"
                type="number"
                inputMode="decimal"
                placeholder={valPlaceholder}
                min="0.01"
                step="0.01"
                value={form.val}
                onChange={e => setForm(f => ({ ...f, val: e.target.value }))}
              />
            </div>
          </div>
          <div className="m-field">
            <label className="m-lbl" htmlFor={`${idPrefix}-date`}>Data</label>
            <input
              id={`${idPrefix}-date`}
              className="m-inp"
              type="date"
              value={form.date}
              onChange={e => setForm(f => ({ ...f, date: e.target.value }))}
            />
          </div>
        </div>

        <div className="m-field">
          <label className="m-lbl" htmlFor={`${idPrefix}-desc`}>Descrição</label>
          <input
            id={`${idPrefix}-desc`}
            className="m-inp"
            type="text"
            maxLength={255}
            placeholder={descPlaceholder}
            value={form.desc}
            onChange={e => setForm(f => ({ ...f, desc: e.target.value }))}
          />
        </div>

        <div className={form.catId === '__nova__' ? 'm-grid' : undefined}>
          <div className="m-field">
            <label className="m-lbl" htmlFor={`${idPrefix}-cat`}>Categoria</label>
            <select
              id={`${idPrefix}-cat`}
              className="m-inp"
              value={form.catId}
              onChange={e => setForm(f => ({ ...f, catId: e.target.value, catNomeNovo: '' }))}
            >
              <option value="">Selecione uma categoria</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
              <option value="__nova__">+ Nova categoria…</option>
            </select>
          </div>
          {form.catId === '__nova__' && (
            <div className="m-field">
              <label className="m-lbl" htmlFor={`${idPrefix}-cat-nova`}>Nova categoria</label>
              <input
                id={`${idPrefix}-cat-nova`}
                className="m-inp"
                type="text"
                placeholder="Ex: Salário, Alimentação…"
                value={form.catNomeNovo}
                onChange={e => setForm(f => ({ ...f, catNomeNovo: e.target.value }))}
              />
            </div>
          )}
        </div>

        <div className="m-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-primary" disabled={salvando}>
            {salvando ? 'Salvando…' : submitLabel}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default LancamentoModal
