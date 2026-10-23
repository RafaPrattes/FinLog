import Modal from './Modal'

/* Confirmação de ações irreversíveis (excluir) ou de saída da conta.
   confirm = { title?, msg, confirmLabel?, variant?: 'danger' | 'primary', cb } */
function ConfirmModal({ confirm, onClose }) {
  const variant = confirm.variant || 'danger'

  return (
    <Modal
      id="confirm-modal"
      title={confirm.title || 'Confirmar exclusão'}
      onClose={onClose}
      narrow
      describedBy="confirm-modal-msg"
    >
      <p className="conf-msg" id="confirm-modal-msg">{confirm.msg}</p>
      <div className="m-actions">
        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
        <button
          type="button"
          className={`btn ${variant === 'danger' ? 'btn-danger' : 'btn-primary'}`}
          onClick={() => { confirm.cb(); onClose() }}
        >
          {confirm.confirmLabel || 'Excluir'}
        </button>
      </div>
    </Modal>
  )
}

export default ConfirmModal
