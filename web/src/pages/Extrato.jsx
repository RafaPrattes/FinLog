import { useState } from 'react'
import Icon from '../components/Icon'
import { DataState, PageHeader, SummaryCard } from '../components/PageHeader'
import { brl, brlSinal, fmtDate, filtrarMovimentacoes, labelData, ordenarRecentes, totais } from '../utils/format'

const TIPOS = [
  ['all', 'Todas'],
  ['entrada', 'Entradas'],
  ['saida', 'Saídas'],
]
/* "Personalizado" sem datas preenchidas mostra todo o histórico */
const PERIODOS = [
  ['mes', 'Este Mês'],
  ['30dias', 'Últimos 30 dias'],
  ['personalizado', 'Personalizado'],
]

function Pills({ options, value, onChange, label }) {
  return (
    <div className="filter-group" role="group" aria-label={label}>
      <span className="filter-label" aria-hidden="true">{label}:</span>
      {options.map(([v, rotulo]) => (
        <button
          key={v}
          type="button"
          className={`pill${value === v ? ' active' : ''}`}
          aria-pressed={value === v}
          onClick={() => onChange(v)}
        >
          {rotulo}
        </button>
      ))}
    </div>
  )
}

function Extrato({ txns, loading, loadError, onRetry, onNovoLanc, onEdit, onDelete }) {
  const [tipo,      setTipo]      = useState('all')
  const [periodo,   setPeriodo]   = useState('mes')
  const [categoria, setCategoria] = useState('all')
  const [de,        setDe]        = useState('')
  const [ate,       setAte]       = useState('')

  const cats = [...new Set(txns.map(t => t.cat))].sort((a, b) => a.localeCompare(b, 'pt-BR'))

  /* Os cartões de resumo respeitam período e categoria, mas não o tipo,
     para que "Total de Saídas" não zere ao filtrar só as entradas. */
  const noPeriodo = filtrarMovimentacoes(txns, { periodo, categoria, de, ate })
  const { entradas, saidas, saldo } = totais(noPeriodo)
  const linhas = ordenarRecentes(filtrarMovimentacoes(noPeriodo, { tipo }))

  const periodoTexto = PERIODOS.find(([v]) => v === periodo)?.[1]
  const resumoImpressao = periodo === 'personalizado'
    ? `Período: ${de ? fmtDate(de) : 'início'} a ${ate ? fmtDate(ate) : 'hoje'}`
    : `Período: ${periodoTexto}`

  return (
    <>
      <PageHeader title="Extrato Detalhado" subtitle="Acompanhe e filtre todas as suas movimentações financeiras.">
        <button type="button" className="btn btn-outline btn-sm-radius" onClick={() => onNovoLanc('entrada')}>
          <Icon name="plus" size={20} /> Novo lançamento
        </button>
        <button type="button" className="btn btn-primary btn-sm-radius" onClick={() => window.print()}>
          <Icon name="download" size={20} /> Exportar PDF
        </button>
      </PageHeader>

      <section className="summary-row" aria-label="Resumo do período">
        <SummaryCard label="Total de Entradas" value={brl(entradas)} tone="pos" />
        <SummaryCard label="Total de Saídas" value={brl(saidas)} tone="neg" />
        <SummaryCard label="Balanço Líquido" value={`${saldo < 0 ? '- ' : ''}${brl(saldo)}`} tone={saldo < 0 ? 'neg' : 'brown'} />
      </section>

      <section className="card filters" aria-label="Filtros">
        <div className="filter-group">
          <Pills label="Filtrar por" options={TIPOS} value={tipo} onChange={setTipo} />
          <label className="sr-only" htmlFor="filtro-categoria">Categoria</label>
          <select id="filtro-categoria" className="filter-select" value={categoria} onChange={e => setCategoria(e.target.value)}>
            <option value="all">Todas as categorias</option>
            {cats.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
        </div>
        <Pills label="Período" options={PERIODOS} value={periodo} onChange={setPeriodo} />
        {periodo === 'personalizado' && (
          <div className="custom-range">
            <label htmlFor="filtro-de">De</label>
            <input id="filtro-de" className="filter-date" type="date" value={de} max={ate || undefined} onChange={e => setDe(e.target.value)} />
            <label htmlFor="filtro-ate">até</label>
            <input id="filtro-ate" className="filter-date" type="date" value={ate} min={de || undefined} onChange={e => setAte(e.target.value)} />
          </div>
        )}
      </section>

      <section className="card table-card print-area" aria-label="Movimentações">
        <div className="print-only">
          <h1>FinLog · Extrato Detalhado</h1>
          <p>{resumoImpressao} · Entradas {brl(entradas)} · Saídas {brl(saidas)} · Balanço {saldo < 0 ? '- ' : ''}{brl(saldo)}</p>
        </div>
        <DataState
          loading={loading}
          error={loadError}
          onRetry={onRetry}
          empty={linhas.length === 0}
          emptyMsg={txns.length === 0 ? 'Nenhuma movimentação registrada.' : 'Nenhuma movimentação encontrada para os filtros escolhidos. Use “Personalizado” sem datas para ver todo o histórico.'}
        >
          <div className="table-scroll">
            <table className="txn-table">
              <thead>
                <tr>
                  <th className="col-dot"><span className="sr-only">Tipo</span></th>
                  <th className="col-date">Data</th>
                  <th className="col-desc">Descrição</th>
                  <th className="col-cat">Categoria</th>
                  <th className="col-val">Valor</th>
                </tr>
              </thead>
              <tbody>
                {linhas.map(t => (
                  <tr key={t.id}>
                    <td className="col-dot">
                      <span className={`dot ${t.type}`} aria-hidden="true" />
                      <span className="sr-only">{t.type === 'entrada' ? 'Entrada' : 'Saída'}</span>
                    </td>
                    <td className="col-date" title={fmtDate(t.date)}>{labelData(t.date)}</td>
                    <td className="col-desc">
                      {t.desc}
                      <div className="row-actions no-print">
                        <button type="button" className="icon-btn sm" onClick={() => onEdit(t)} aria-label={`Editar lançamento ${t.desc}`}>
                          <Icon name="edit" size={16} />
                        </button>
                        <button type="button" className="icon-btn sm danger" onClick={() => onDelete(t.id)} aria-label={`Excluir lançamento ${t.desc}`}>
                          <Icon name="trash" size={16} />
                        </button>
                      </div>
                    </td>
                    <td className="col-cat"><span className="tag">{t.cat}</span></td>
                    <td className={`col-val amount ${t.type}`}>{brlSinal(t.type, t.val)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </DataState>
      </section>
    </>
  )
}

export default Extrato
