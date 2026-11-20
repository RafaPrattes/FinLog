import Modal from './Modal'

/* Modal de criação/edição de meta, no mesmo padrão do modal de lançamento */
function MetaModal({ editando, form, setForm, onSubmit, salvando, onClose }) {
  function submit(e) {
    e.preventDefault()
    onSubmit()
  }

  return (
    <Modal
      id="meta-modal"
      title={editando ? 'Editar meta' : 'Nova meta'}
      subtitle={editando ? 'Atualize o objetivo e o valor da meta' : 'Defina um objetivo financeiro e o valor a alcançar'}
      onClose={onClose}
    >
      <form className="m-form" onSubmit={submit} noValidate>
        <div className="m-field">
          <label className="m-lbl" htmlFor="meta-desc">Descrição</label>
          <input
            id="meta-desc"
            className="m-inp"
            type="text"
            maxLength={255}
            placeholder="Ex: Entrada para a casa própria"
            value={form.desc}
            onChange={e => setForm(f => ({ ...f, desc: e.target.value }))}
          />
        </div>
        <div className="m-field">
          <label className="m-lbl" htmlFor="meta-val">Valor</label>
          <div className="m-prefix-wrap">
            <span className="m-prefix" aria-hidden="true">R$</span>
            <input
              id="meta-val"
              className="m-inp"
              type="number"
              inputMode="decimal"
              placeholder="0,00"
              min="0.01"
              step="0.01"
              value={form.val}
              onChange={e => setForm(f => ({ ...f, val: e.target.value }))}
            />
          </div>
        </div>
        <div className="m-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-primary" disabled={salvando}>
            {salvando ? 'Salvando…' : 'Salvar meta'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default MetaModal
