import Icon from '../components/Icon'
import MetaItem from '../components/MetaItem'
import { DataState, PageHeader, SummaryCard } from '../components/PageHeader'
import { brl } from '../utils/format'

/* Não há tela de Metas no protótipo: segue a linguagem do Extrato e do Início */
function Metas({ metas, loading, loadError, onRetry, onAdd, onEdit, onDelete }) {
  const total = metas.reduce((s, m) => s + m.val, 0)

  return (
    <>
      <PageHeader title="Metas" subtitle="Defina e acompanhe seus objetivos financeiros.">
        <button type="button" className="btn btn-primary btn-sm-radius" onClick={onAdd}>
          <Icon name="plus" size={20} /> Nova meta
        </button>
      </PageHeader>

      <section className="summary-row" aria-label="Resumo das metas">
        <SummaryCard label="Metas cadastradas" value={metas.length} tone="brown" />
        <SummaryCard label="Valor total das metas" value={brl(total)} tone="brown" />
      </section>

      <DataState
        loading={loading}
        error={loadError}
        onRetry={onRetry}
        empty={metas.length === 0}
        emptyMsg="Nenhuma meta cadastrada. Clique em “Nova meta” para começar."
      >
        <ul className="metas-grid" aria-label="Suas metas">
          {metas.map(m => (
            <MetaItem key={m.id} meta={m} onEdit={onEdit} onDelete={onDelete} className="card meta-card" />
          ))}
        </ul>
      </DataState>
    </>
  )
}

export default Metas
