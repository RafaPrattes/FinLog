import { useState, useEffect, useRef } from 'react'
import api from '../service/api'

/* ══════════════════════════════
   UTILS
══════════════════════════════ */
function brl(n) {
  return 'R$' + Math.abs(n).toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
function today() {
  return new Date().toISOString().split('T')[0]
}
function fmtDate(d) {
  if (!d) return ''
  const [y, m, day] = d.split('-')
  return `${day}/${m}/${y}`
}

/* ══════════════════════════════
   DONUT CHART
══════════════════════════════ */
function DonutChart({ entTotal, saiTotal }) {
  const total = entTotal + saiTotal
  const r     = 40
  const cx    = 54
  const cy    = 54
  const circ  = 2 * Math.PI * r

  if (total === 0) {
    return (
      <svg width="108" height="108">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#DDD9D2" strokeWidth="15" />
      </svg>
    )
  }

  const entArc = (entTotal / total) * circ
  const saiArc = (saiTotal / total) * circ
  const offset = circ / 4

  return (
    <svg width="108" height="108">
      <circle
        cx={cx} cy={cy} r={r}
        fill="none" stroke="#8FA98B" strokeWidth="15"
        strokeDasharray={`${entArc} ${circ - entArc}`}
        strokeDashoffset={offset}
        strokeLinecap="round"
      />
      {saiTotal > 0 && (
        <circle
          cx={cx} cy={cy} r={r}
          fill="none" stroke="#C4A882" strokeWidth="15"
          strokeDasharray={`${saiArc} ${circ - saiArc}`}
          strokeDashoffset={offset - entArc}
          strokeLinecap="round"
        />
      )}
    </svg>
  )
}

/* ══════════════════════════════
   DASHBOARD
══════════════════════════════ */
function Dashboard({ user, onLogout }) {
  const [txns,       setTxns]       = useState([])
  const [metas,      setMetas]      = useState([])
  const [editModeOn, setEditModeOn] = useState(false)
  const [loading,    setLoading]    = useState(true)

  const [filterType, setFilterType] = useState('all')
  const [filterCat,  setFilterCat]  = useState('all')

  const [lancModal, setLancModal] = useState(false)
  const [lancType,  setLancType]  = useState('entrada')
  const [lancForm,  setLancForm]  = useState({ desc: '', val: '', date: today(), cat: '' })

  const [editLancModal, setEditLancModal] = useState(null)
  const [editLancForm,  setEditLancForm]  = useState({ desc: '', val: '', date: '', cat: '' })

  const [metaModal,     setMetaModal]     = useState(false)
  const [editingMetaId, setEditingMetaId] = useState(null)
  const [metaForm,      setMetaForm]      = useState({ desc: '', val: '' })

  const [confirmModal, setConfirmModal] = useState(null)

  const [toastMsg,     setToastMsg]     = useState('')
  const [toastVisible, setToastVisible] = useState(false)
  const toastTimer = useRef(null)

  /* ══ Carregar movimentações do usuário logado ══ */
  useEffect(() => {
    async function fetchMovimentacoes() {
      try {
        const { data } = await api.get(`/movimentacoes?usuarioId=${user.id}`)
        const mapped = data.map(m => ({
          id:   m.id,
          type: m.tipo === 'RECEITA' ? 'entrada' : 'saida',
          desc: m.descricao,
          val: parseFloat(m.valor) || 0,
          date: m.data,
          cat:  m.categoria?.nome || 'Outros',
        }))
        setTxns(mapped)
      } catch (err) {
        console.error('Erro ao carregar movimentações:', err)
      } finally {
        setLoading(false)
      }
    }
    if (user?.id) fetchMovimentacoes()
  }, [user])

  /* ══ Valores calculados ══ */
  const entTotal = txns.filter(t => t.type === 'entrada').reduce((s, t) => s + t.val, 0)
  const saiTotal = txns.filter(t => t.type === 'saida'  ).reduce((s, t) => s + t.val, 0)
  const saldo    = entTotal - saiTotal

  const cats = [...new Set(txns.map(t => t.cat))].sort()

  const filteredTxns = [...txns]
    .reverse()
    .filter(t => filterType === 'all' || t.type === filterType)
    .filter(t => filterCat  === 'all' || t.cat  === filterCat)

  const avatarText = user?.nome
    ? user.nome.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
    : 'U'

  /* ══ Toast ══ */
  function showToast(msg) {
    setToastMsg(msg)
    setToastVisible(true)
    clearTimeout(toastTimer.current)
    toastTimer.current = setTimeout(() => setToastVisible(false), 3000)
  }

  /* ══ Lançamento ══ */
  function openLanc(type) {
    setLancType(type)
    setLancForm({ desc: '', val: '', date: today(), cat: '' })
    setLancModal(true)
  }

  async function saveLanc() {
    const desc = lancForm.desc.trim()
    const val  = parseFloat(lancForm.val)
    const date = lancForm.date
    const cat  = lancForm.cat.trim() || 'Outros'

    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }
    if (!date)                  { showToast('Informe a data.');           return }

    try {
      const payload = {
        descricao: desc,
        valor:     val,
        data:      date,
        tipo:      lancType === 'entrada' ? 'RECEITA' : 'DESPESA',
        usuario:   { id: user.id },
      }
      const { data } = await api.post('/movimentacoes', payload)
      const nova = {
        id:   data.id,
        type: lancType,
        desc: data.descricao,
        val:  parseFloat(data.valor),
        date: data.data,
        cat:  data.categoria?.nome || cat,
      }
      setTxns(prev => [...prev, nova])
      setLancModal(false)
      showToast(lancType === 'entrada' ? 'Receita registrada!' : 'Despesa registrada!')
    } catch (err) {
      console.error('Erro ao salvar lançamento:', err)
      showToast('Erro ao salvar. Tente novamente.')
    }
  }

  /* ══ Editar lançamento ══ */
  function openEditLanc(t) {
    setEditLancModal(t)
    setEditLancForm({ desc: t.desc, val: t.val, date: t.date, cat: t.cat })
  }

  async function saveEditLanc() {
    const desc = editLancForm.desc.trim()
    const val  = parseFloat(editLancForm.val)
    const date = editLancForm.date
    const cat  = editLancForm.cat.trim() || 'Outros'

    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }

    try {
      const payload = {
        descricao: desc,
        valor:     val,
        data:      date,
        tipo:      editLancModal.type === 'entrada' ? 'RECEITA' : 'DESPESA',
        usuario:   { id: user.id },
      }
      await api.put(`/movimentacoes/${editLancModal.id}`, payload)
      setTxns(prev =>
        prev.map(t => t.id === editLancModal.id ? { ...t, desc, val, date, cat } : t)
      )
      setEditLancModal(null)
      showToast('Lançamento atualizado!')
    } catch (err) {
      console.error('Erro ao editar lançamento:', err)
      showToast('Erro ao editar. Tente novamente.')
    }
  }

  function delLanc(id) {
    setConfirmModal({
      msg: 'Deseja excluir este lançamento permanentemente?',
      cb: async () => {
        try {
          await api.delete(`/movimentacoes/${id}`)
          setTxns(prev => prev.filter(t => t.id !== id))
          showToast('Lançamento excluído.')
        } catch (err) {
          console.error('Erro ao excluir lançamento:', err)
          showToast('Erro ao excluir. Tente novamente.')
        }
      },
    })
  }

  /* ══ Metas (em memória) ══ */
  function openMetaModal(id) {
    setEditingMetaId(id)
    if (id) {
      const m = metas.find(x => x.id === id)
      setMetaForm({ desc: m.desc, val: m.val })
    } else {
      setMetaForm({ desc: '', val: '' })
    }
    setMetaModal(true)
  }

  function saveMeta() {
    const desc = metaForm.desc.trim()
    const val  = parseFloat(metaForm.val)
    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }

    if (editingMetaId) {
      setMetas(prev => prev.map(m => m.id === editingMetaId ? { ...m, desc, val } : m))
      showToast('Meta atualizada!')
    } else {
      setMetas(prev => [...prev, { id: Date.now(), desc, val }])
      showToast('Meta adicionada!')
    }
    setMetaModal(false)
  }

  function delMeta(id) {
    setConfirmModal({
      msg: 'Deseja excluir esta meta permanentemente?',
      cb: () => {
        setMetas(prev => prev.filter(m => m.id !== id))
        showToast('Meta excluída.')
      },
    })
  }

  /* ══ Logout ══ */
  function confirmLogout() {
    setConfirmModal({
      msg: 'Deseja encerrar a sessão?',
      cb: () => {
        showToast('Sessão encerrada.')
        setTimeout(() => onLogout(), 400)
      },
    })
  }

  /* ══ Fechar modais com Escape ══ */
  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') {
        setLancModal(false)
        setEditLancModal(null)
        setMetaModal(false)
        setConfirmModal(null)
      }
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [])

  /* ══════════════════════════════
     RENDER
  ══════════════════════════════ */
  return (
    <div id="screen-dashboard" className="screen active">

      <aside className="sidebar">
        <div className="sidebar-logo">FinLog</div>
        <div className="sidebar-user" onClick={confirmLogout} title="Sair">
          <div className="avatar">{avatarText}</div>
          <div>
            <div className="user-name">{user?.nome || 'Usuário'}</div>
            <div className="logout-label">Clique para sair</div>
          </div>
        </div>
      </aside>

      <main className="main">
        <h2 className="page-title">Página Inicial</h2>

        <div className="row row-top">
          <div className="saldo-card">
            <div className="saldo-arrow">↗</div>
            <div className="saldo-lbl">Seu Saldo</div>
            <div className={`saldo-val${saldo < 0 ? ' neg' : ''}`}>{brl(saldo)}</div>
            <div className="saldo-btns">
              <button className="saldo-btn" onClick={() => openLanc('entrada')}>Entrada +</button>
              <button className="saldo-btn" onClick={() => openLanc('saida')}>Saída –</button>
            </div>
          </div>

          <div className="summary-card">
            <div className="summary-nums">
              <div className="s-row">
                <div className="s-item">
                  <label>Entradas</label>
                  <div className="amt">{brl(entTotal)}</div>
                </div>
                <div className="s-item">
                  <label>Saídas</label>
                  <div className="amt">{brl(saiTotal)}</div>
                </div>
              </div>
            </div>
            <div className="donut-wrap">
              <DonutChart entTotal={entTotal} saiTotal={saiTotal} />
            </div>
          </div>
        </div>

        <div className="row row-bottom">
          <div className="card">
            <div className="card-head">
              <div className="card-head-title">Suas Metas</div>
              <button className="icon-btn" onClick={() => openMetaModal(null)}>+</button>
            </div>
            <div className="metas-scroll">
              {metas.length === 0 ? (
                <div className="empty-msg">Nenhuma meta cadastrada.</div>
              ) : (
                metas.map(m => (
                  <div className="meta-card" key={m.id}>
                    <div className="meta-d">{m.desc}</div>
                    <div className="meta-v">{brl(m.val)}</div>
                    <div className="meta-act">
                      <button className="edit-m" onClick={() => openMetaModal(m.id)}>✏</button>
                      <button className="del-m"  onClick={() => delMeta(m.id)}>🗑</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-head">
              <div className="card-head-title">Seu Extrato</div>
              <div className="head-btns">
                <button
                  className={`edit-toggle${editModeOn ? ' on' : ''}`}
                  onClick={() => setEditModeOn(prev => !prev)}
                >
                  {editModeOn ? '✓ Concluir' : '✏ Editar'}
                </button>
                <button className="icon-btn" onClick={() => openLanc('entrada')}>+</button>
              </div>
            </div>

            <div className="filter-bar">
              <select className="fsel" value={filterType} onChange={e => setFilterType(e.target.value)}>
                <option value="all">Todos</option>
                <option value="entrada">Entradas</option>
                <option value="saida">Saídas</option>
              </select>
              <select className="fsel" value={filterCat} onChange={e => setFilterCat(e.target.value)}>
                <option value="all">Todas categorias</option>
                {cats.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>

            <div className="extrato-scroll">
              {loading ? (
                <div className="empty-msg">Carregando…</div>
              ) : filteredTxns.length === 0 ? (
                <div className="empty-msg">Nenhuma movimentação encontrada.</div>
              ) : (
                filteredTxns.map(t => (
                  <div className="txn-row" key={t.id}>
                    <div className="txn-left">
                      <div className={`dot ${t.type}`} />
                      <div>
                        <div className="txn-name">{t.desc}</div>
                        <div className="txn-sub">{t.cat} · {fmtDate(t.date)}</div>
                      </div>
                    </div>
                    <div className="txn-right">
                      <div className="txn-val">
                        {t.type === 'saida' ? '–' : ''}{brl(t.val)}
                      </div>
                      {editModeOn && (
                        <div className="txn-actions show">
                          <button className="ea" onClick={() => openEditLanc(t)}>✏</button>
                          <button className="da" onClick={() => delLanc(t.id)}>🗑</button>
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </main>

      {/* ═══ MODAL: NOVO LANÇAMENTO ═══ */}
      {lancModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setLancModal(false) }}>
          <div className="modal">
            <button className="m-close" onClick={() => setLancModal(false)}>✕</button>
            <div className="m-title">Novo lançamento</div>
            <div className="m-type-row">
              <button className={`m-type-btn${lancType === 'entrada' ? ' e' : ''}`} onClick={() => setLancType('entrada')}>Entrada +</button>
              <button className={`m-type-btn${lancType === 'saida'   ? ' s' : ''}`} onClick={() => setLancType('saida')}>Saída –</button>
            </div>
            <div className="m-row">
              <span className="m-lbl">Descrição:</span>
              <input className="m-inp" type="text" placeholder="Digite a descrição" value={lancForm.desc} onChange={e => setLancForm(f => ({ ...f, desc: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Valor:</span>
              <input className="m-inp" type="number" placeholder="Digite o valor" min="0.01" step="0.01" value={lancForm.val} onChange={e => setLancForm(f => ({ ...f, val: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Data:</span>
              <input className="m-inp" type="date" value={lancForm.date} onChange={e => setLancForm(f => ({ ...f, date: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Categoria:</span>
              <input className="m-inp" type="text" placeholder="Ex: Salário, Alimentação…" value={lancForm.cat} onChange={e => setLancForm(f => ({ ...f, cat: e.target.value }))} />
            </div>
            <button className="m-btn" onClick={saveLanc}>Confirmar</button>
          </div>
        </div>
      )}

      {/* ═══ MODAL: EDITAR LANÇAMENTO ═══ */}
      {editLancModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setEditLancModal(null) }}>
          <div className="modal">
            <button className="m-close" onClick={() => setEditLancModal(null)}>✕</button>
            <div className="m-title">Editar lançamento</div>
            <div className="m-row">
              <span className="m-lbl">Descrição:</span>
              <input className="m-inp" type="text" value={editLancForm.desc} onChange={e => setEditLancForm(f => ({ ...f, desc: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Valor:</span>
              <input className="m-inp" type="number" min="0.01" step="0.01" value={editLancForm.val} onChange={e => setEditLancForm(f => ({ ...f, val: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Data:</span>
              <input className="m-inp" type="date" value={editLancForm.date} onChange={e => setEditLancForm(f => ({ ...f, date: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Categoria:</span>
              <input className="m-inp" type="text" value={editLancForm.cat} onChange={e => setEditLancForm(f => ({ ...f, cat: e.target.value }))} />
            </div>
            <button className="m-btn" onClick={saveEditLanc}>Salvar alterações</button>
          </div>
        </div>
      )}

      {/* ═══ MODAL: META ═══ */}
      {metaModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setMetaModal(false) }}>
          <div className="modal">
            <button className="m-close" onClick={() => setMetaModal(false)}>✕</button>
            <div className="m-title">{editingMetaId ? 'Editar Meta' : 'Nova Meta'}</div>
            <div className="m-row">
              <span className="m-lbl">Descrição:</span>
              <input className="m-inp" type="text" placeholder="Ex: Casa própria…" value={metaForm.desc} onChange={e => setMetaForm(f => ({ ...f, desc: e.target.value }))} />
            </div>
            <div className="m-row">
              <span className="m-lbl">Valor:</span>
              <input className="m-inp" type="number" placeholder="Ex: 1000" min="0.01" step="0.01" value={metaForm.val} onChange={e => setMetaForm(f => ({ ...f, val: e.target.value }))} />
            </div>
            <button className="m-btn" onClick={saveMeta}>Confirmar</button>
          </div>
        </div>
      )}

      {/* ═══ MODAL: CONFIRMAÇÃO ═══ */}
      {confirmModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setConfirmModal(null) }}>
          <div className="modal narrow">
            <div className="m-title" style={{ fontSize: '19px' }}>Confirmar exclusão</div>
            <p className="conf-msg">{confirmModal.msg}</p>
            <div className="conf-btns">
              <button className="btn-cancel" onClick={() => setConfirmModal(null)}>Cancelar</button>
              <button className="btn-del" onClick={() => { confirmModal.cb(); setConfirmModal(null) }}>Excluir</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══ TOAST ═══ */}
      <div className={`toast${toastVisible ? ' show' : ''}`}>{toastMsg}</div>
    </div>
  )
}

export default Dashboard
