import DonutChart from '../components/DonutChart'
import Icon from '../components/Icon'
import { DataState, PageHeader, SummaryCard } from '../components/PageHeader'
import MetaItem from '../components/MetaItem'
import { brl, brlSinal, labelData, ordenarRecentes, pct, totais } from '../utils/format'

const RECENTES = 8

function Inicio({ txns, metas, loading, loadError, onRetry, onNovoLanc, onVerExtrato, onAddMeta, onEditMeta, onDelMeta }) {
  const { entradas, saidas, saldo } = totais(txns)
  const movimentado = entradas + saidas
  const recentes = ordenarRecentes(txns).slice(0, RECENTES)

  return (
    <>
      <PageHeader title="Página Inicial" subtitle="Visão geral das suas finanças e metas." />

      <section className="summary-row" aria-label="Resumo">
        <SummaryCard label="Entradas" value={brl(entradas)} tone="pos" />
        <SummaryCard label="Saídas" value={brl(saidas)} tone="neg" />
      </section>

      <div className="home-grid">
        {/* COLUNA ESQUERDA */}
        <div className="home-col">
          <section className="card saldo-card" aria-labelledby="saldo-title">
            <div className="saldo-top">
              <div>
                <h2 className="saldo-title" id="saldo-title">Seu Saldo</h2>
                <div className={`saldo-value${saldo < 0 ? ' neg' : ''}`}>
                  {saldo < 0 ? '- ' : ''}{brl(saldo)}
                </div>
              </div>
              <button type="button" className="saldo-tile" onClick={onVerExtrato} aria-label="Ver extrato detalhado" title="Extrato detalhado">
                <Icon name="arrow-up-right" size={30} strokeWidth={2.5} />
              </button>
            </div>
            <div className="saldo-types">
              <button type="button" className="saldo-type entrada" onClick={() => onNovoLanc('entrada')}>
                Entrada
              </button>
              <button type="button" className="saldo-type saida" onClick={() => onNovoLanc('saida')}>
                Saída
              </button>
            </div>
          </section>

          <section className="card sage-border" aria-labelledby="es-title">
            <h2 className="card-title" id="es-title">Entradas x Saídas</h2>
            <div className="donut-box">
              <DonutChart entTotal={entradas} saiTotal={saidas} />
              <div className="donut-center" aria-hidden="true">
                <span className="donut-center-lbl">Total movimentado</span>
                <span className="donut-center-val">{brl(movimentado)}</span>
              </div>
            </div>
            <div className="legend">
              <div className="legend-row">
                <span className="legend-dot entrada" aria-hidden="true" />
                <span className="legend-name">Entradas</span>
                <span className="legend-val entrada">{brl(entradas)} · {pct(entradas, movimentado)}</span>
              </div>
              <div className="legend-row">
                <span className="legend-dot saida" aria-hidden="true" />
                <span className="legend-name">Saídas</span>
                <span className="legend-val saida">{brl(saidas)} · {pct(saidas, movimentado)}</span>
              </div>
            </div>
          </section>
        </div>

        {/* COLUNA CENTRAL: EXTRATO */}
        <section className="card gray-border" aria-labelledby="extrato-title">
          <div className="card-head">
            <h2 className="card-title" id="extrato-title">Seu Extrato</h2>
            <button type="button" className="square-btn" onClick={() => onNovoLanc('entrada')} aria-label="Novo lançamento">
              <Icon name="plus" size={24} strokeWidth={3} />
            </button>
          </div>
          <DataState
            loading={loading}
            error={loadError}
            onRetry={onRetry}
            empty={recentes.length === 0}
            emptyMsg="Nenhuma movimentação registrada."
          >
            <ul className="txn-list">
              {recentes.map(t => (
                <li className="txn-row" key={t.id}>
                  <span className={`dot ${t.type}`} aria-hidden="true" />
                  <div className="txn-info">
                    <div className="txn-name">
                      <span className="sr-only">{t.type === 'entrada' ? 'Entrada: ' : 'Saída: '}</span>
                      {t.desc}
                    </div>
                    <div className="txn-sub">{t.cat} · {labelData(t.date)}</div>
                  </div>
                  <div className={`amount ${t.type}`}>{brlSinal(t.type, t.val)}</div>
                </li>
              ))}
            </ul>
            {txns.length > RECENTES && (
              <button type="button" className="link-btn see-all" onClick={onVerExtrato}>
                Ver extrato completo
              </button>
            )}
          </DataState>
        </section>

        {/* COLUNA DIREITA: METAS */}
        <section className="card sage-border home-metas" aria-labelledby="metas-title">
          <div className="card-head">
            <h2 className="card-title" id="metas-title">Suas Metas</h2>
            <button type="button" className="square-btn" onClick={onAddMeta} aria-label="Adicionar meta">
              <Icon name="plus" size={24} strokeWidth={3} />
            </button>
          </div>
          <DataState
            loading={loading}
            error={loadError}
            onRetry={onRetry}
            empty={metas.length === 0}
            emptyMsg="Nenhuma meta cadastrada."
          >
            <ul className="meta-list">
              {metas.map(m => (
                <MetaItem key={m.id} meta={m} onEdit={onEditMeta} onDelete={onDelMeta} />
              ))}
            </ul>
          </DataState>
        </section>
      </div>
    </>
  )
}

export default Inicio
