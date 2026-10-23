import Icon from './Icon'
import { brl } from '../utils/format'

/* Item de meta: descrição com ações de editar/excluir e o valor abaixo */
function MetaItem({ meta, onEdit, onDelete, as: Tag = 'li', className = 'meta-item' }) {
  return (
    <Tag className={className}>
      <div className="meta-item-top">
        <div className="meta-desc">{meta.desc}</div>
        <div className="meta-actions">
          <button type="button" className="icon-btn sm" onClick={() => onEdit(meta.id)} aria-label={`Editar meta ${meta.desc}`}>
            <Icon name="edit" size={18} />
          </button>
          <button type="button" className="icon-btn sm danger" onClick={() => onDelete(meta.id)} aria-label={`Excluir meta ${meta.desc}`}>
            <Icon name="trash" size={18} />
          </button>
        </div>
      </div>
      <div className="meta-val">{brl(meta.val)}</div>
    </Tag>
  )
}

export default MetaItem
