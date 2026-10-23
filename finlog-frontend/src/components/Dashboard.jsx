import { useState, useEffect, useRef } from 'react'
import api from '../service/api'
import { mensagemErro } from '../service/errors'
import { brl, today, fmtDate, mapMeta, mapMovimentacao } from '../utils/format'
import DonutChart from './DonutChart'
import ChatBot from './ChatBot'
import LancamentoModal from './LancamentoModal'

/* ══════════════════════════════
   DASHBOARD
══════════════════════════════ */
function Dashboard({ user, onLogout }) {
  const [txns,       setTxns]       = useState([])
  const [metas,      setMetas]      = useState([])
  const [editModeOn, setEditModeOn] = useState(false)
  const [loading,    setLoading]    = useState(true)
  const [loadError,  setLoadError]  = useState(false)
  const [reloadKey,  setReloadKey]  = useState(0)
  const [activeTab,  setActiveTab]  = useState('home')

  const [filterType, setFilterType] = useState('all')
  const [filterCat,  setFilterCat]  = useState('all')

  const [categorias, setCategorias] = useState([])

  const [lancModal, setLancModal] = useState(false)
  const [lancType,  setLancType]  = useState('entrada')
  const [lancForm,  setLancForm]  = useState({ desc: '', val: '', date: today(), catId: '', catNomeNovo: '' })

  const [editLancModal, setEditLancModal] = useState(null)
  const [editLancForm,  setEditLancForm]  = useState({ desc: '', val: '', date: '', catId: '', catNomeNovo: '' })

  const [metaModal,     setMetaModal]     = useState(false)
  const [editingMetaId, setEditingMetaId] = useState(null)
  const [metaForm,      setMetaForm]      = useState({ desc: '', val: '' })

  const [confirmModal, setConfirmModal] = useState(null)
  const [salvando,     setSalvando]     = useState(false)

  const [toastMsg,     setToastMsg]     = useState('')
  const [toastVisible, setToastVisible] = useState(false)
  const toastTimer = useRef(null)

  /* ══ Carregar movimentações, categorias e metas do usuário logado ══ */
  useEffect(() => {
    async function fetchCategorias() {
      try {
        const { data } = await api.get('/categorias')
        setCategorias(data)
        return true
      } catch (err) {
        console.error('Erro ao carregar categorias:', err)
        return false
      }
    }
    async function fetchMovimentacoes() {
      try {
        const { data } = await api.get('/movimentacoes')
        setTxns(data.map(mapMovimentacao))
        return true
      } catch (err) {
        console.error('Erro ao carregar movimentações:', err)
        return false
      }
    }
    async function fetchMetas() {
      try {
        const { data } = await api.get('/metas')
        setMetas(data.map(mapMeta))
        return true
      } catch (err) {
        console.error('Erro ao carregar metas:', err)
        return false
      }
    }
    async function fetchTudo() {
      const resultados = await Promise.all([fetchCategorias(), fetchMovimentacoes(), fetchMetas()])
      setLoadError(resultados.includes(false))
      setLoading(false)
    }
    if (user?.id) fetchTudo()
  }, [user, reloadKey])

  function recarregar() {
    setLoading(true)
    setLoadError(false)
    setReloadKey(k => k + 1)
  }

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
    setLancForm({ desc: '', val: '', date: today(), catId: '', catNomeNovo: '' })
    setLancModal(true)
  }

  async function saveLanc() {
    const desc        = lancForm.desc.trim()
    const val         = parseFloat(lancForm.val)
    const date        = lancForm.date
    const novaCatNome = lancForm.catNomeNovo.trim()

    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }
    if (!date)                  { showToast('Informe a data.');           return }
    if (lancForm.catId === '__nova__' && !novaCatNome) {
      showToast('Informe o nome da nova categoria.')
      return
    }
    if (salvando) return

    setSalvando(true)
    try {
      const categoriaPayload =
        lancForm.catId === '__nova__' ? { categoria: { nome: novaCatNome } } :
        lancForm.catId               ? { categoriaId: Number(lancForm.catId) } :
        { categoria: { nome: 'Outros' } }

      const payload = {
        descricao: desc,
        valor:     val,
        data:      date,
        tipo:      lancType === 'entrada' ? 'RECEITA' : 'DESPESA',
        ...categoriaPayload,
      }
      await api.post('/movimentacoes', payload)
      setLancModal(false)
      showToast(lancType === 'entrada' ? 'Receita registrada!' : 'Despesa registrada!')
      // Recarrega lista do backend para garantir IDs corretos (necessário para deletar)
      const { data: updated } = await api.get('/movimentacoes')
      setTxns(updated.map(mapMovimentacao))
      if (lancForm.catId === '__nova__') {
        const { data: cats } = await api.get('/categorias')
        setCategorias(cats)
      }
    } catch (err) {
      console.error('Erro ao salvar lançamento:', err)
      showToast(mensagemErro(err, 'Erro ao salvar. Tente novamente.'))
    } finally {
      setSalvando(false)
    }
  }

  /* ══ Editar lançamento ══ */
  function openEditLanc(t) {
    setEditLancModal(t)
    setEditLancForm({ desc: t.desc, val: t.val, date: t.date, catId: t.catId ? String(t.catId) : '', catNomeNovo: '' })
  }

  async function saveEditLanc() {
    const desc        = editLancForm.desc.trim()
    const val         = parseFloat(editLancForm.val)
    const date        = editLancForm.date
    const novaCatNome = editLancForm.catNomeNovo.trim()

    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }
    if (editLancForm.catId === '__nova__' && !novaCatNome) {
      showToast('Informe o nome da nova categoria.')
      return
    }
    if (salvando) return

    setSalvando(true)
    try {
      const categoriaPayload =
        editLancForm.catId === '__nova__' ? { categoria: { nome: novaCatNome } } :
        editLancForm.catId               ? { categoriaId: Number(editLancForm.catId) } :
        { categoria: { nome: 'Outros' } }

      const payload = {
        descricao: desc,
        valor:     val,
        data:      date,
        tipo:      editLancModal.type === 'entrada' ? 'RECEITA' : 'DESPESA',
        ...categoriaPayload,
      }
      const { data: updated } = await api.put(`/movimentacoes/${editLancModal.id}`, payload)
      setTxns(prev =>
        prev.map(t => t.id === editLancModal.id ? {
          ...t,
          desc,
          val,
          date,
          cat:   updated.categoria?.nome || 'Outros',
          catId: updated.categoria?.id ?? null,
        } : t)
      )
      setEditLancModal(null)
      showToast('Lançamento atualizado!')
      if (editLancForm.catId === '__nova__') {
        const { data: cats } = await api.get('/categorias')
        setCategorias(cats)
      }
    } catch (err) {
      console.error('Erro ao editar lançamento:', err)
      showToast(mensagemErro(err, 'Erro ao editar. Tente novamente.'))
    } finally {
      setSalvando(false)
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
          showToast(mensagemErro(err, 'Erro ao excluir. Tente novamente.'))
        }
      },
    })
  }

  /* ══ Metas ══ */
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

  async function saveMeta() {
    const desc = metaForm.desc.trim()
    const val  = parseFloat(metaForm.val)
    if (!desc)                  { showToast('Informe a descrição.');     return }
    if (isNaN(val) || val <= 0) { showToast('Informe um valor válido.'); return }
    if (salvando) return

    setSalvando(true)
    try {
      const payload = { descricao: desc, valor: val }
      if (editingMetaId) {
        const { data } = await api.put(`/metas/${editingMetaId}`, payload)
        setMetas(prev => prev.map(m => m.id === editingMetaId ? mapMeta(data) : m))
        showToast('Meta atualizada!')
      } else {
        const { data } = await api.post('/metas', payload)
        setMetas(prev => [...prev, mapMeta(data)])
        showToast('Meta adicionada!')
      }
      setMetaModal(false)
    } catch (err) {
      console.error('Erro ao salvar meta:', err)
      showToast(mensagemErro(err, 'Erro ao salvar meta. Tente novamente.'))
    } finally {
      setSalvando(false)
    }
  }

  function delMeta(id) {
    setConfirmModal({
      msg: 'Deseja excluir esta meta permanentemente?',
      cb: async () => {
        try {
          await api.delete(`/metas/${id}`)
          setMetas(prev => prev.filter(m => m.id !== id))
          showToast('Meta excluída.')
        } catch (err) {
          console.error('Erro ao excluir meta:', err)
          showToast(mensagemErro(err, 'Erro ao excluir meta. Tente novamente.'))
        }
      },
    })
  }

  /* ══ Logout ══ */
  function confirmLogout() {
    setConfirmModal({
      title:        'Encerrar sessão',
      confirmLabel: 'Sair',
      msg:          'Deseja encerrar a sessão?',
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

      {/* ─── SIDEBAR ─── */}
      <aside className="sidebar">
        <div className="sidebar-logo-box">FinLog</div>
        <nav className="sidebar-nav">
          <button
            className={`nav-item${activeTab === 'home' ? ' active' : ''}`}
            onClick={() => setActiveTab('home')}
          >
            🏠 Início
          </button>
          <button
            className={`nav-item${activeTab === 'chat' ? ' active' : ''}`}
            onClick={() => setActiveTab('chat')}
          >
            🤖 Assistente IA
          </button>
        </nav>
        <button type="button" className="sidebar-user" onClick={confirmLogout} title="Sair">
          <div className="avatar" aria-hidden="true">{avatarText}</div>
          <div>
            <div className="user-name">{user?.nome || 'Usuário'}</div>
            <div className="logout-label">Clique para sair</div>
          </div>
        </button>
      </aside>

      {/* ─── MAIN ─── */}
      <main className="main">

        {/* ABA: CHATBOT */}
        {activeTab === 'chat' && <ChatBot user={user} />}

        {/* ABA: HOME */}
        {activeTab === 'home' && (
          <>
            <h2 className="page-title">Página Inicial</h2>

            {/* TOP ROW */}
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

            {/* BOTTOM ROW */}
            <div className="row row-bottom">

              {/* METAS */}
              <div className="card">
                <div className="card-head">
                  <div className="card-head-title">Suas Metas</div>
                  <button className="icon-btn" onClick={() => openMetaModal(null)} aria-label="Adicionar meta">+</button>
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
                          <button className="edit-m" onClick={() => openMetaModal(m.id)} aria-label={`Editar meta ${m.desc}`}>✏</button>
                          <button className="del-m"  onClick={() => delMeta(m.id)} aria-label={`Excluir meta ${m.desc}`}>🗑</button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* EXTRATO */}
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
                    <button className="icon-btn" onClick={() => openLanc('entrada')} aria-label="Novo lançamento">+</button>
                  </div>
                </div>

                <div className="filter-bar">
                  <select className="fsel" aria-label="Filtrar por tipo" value={filterType} onChange={e => setFilterType(e.target.value)}>
                    <option value="all">Todos</option>
                    <option value="entrada">Entradas</option>
                    <option value="saida">Saídas</option>
                  </select>
                  <select className="fsel" aria-label="Filtrar por categoria" value={filterCat} onChange={e => setFilterCat(e.target.value)}>
                    <option value="all">Todas categorias</option>
                    {cats.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>

                <div className="extrato-scroll">
                  {loading ? (
                    <div className="empty-msg">Carregando…</div>
                  ) : loadError ? (
                    <div className="empty-msg" role="alert">
                      Não foi possível carregar seus dados.
                      <div>
                        <button className="link-btn" onClick={recarregar}>Tentar novamente</button>
                      </div>
                    </div>
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
                              <button className="ea" onClick={() => openEditLanc(t)} aria-label={`Editar lançamento ${t.desc}`}>✏</button>
                              <button className="da" onClick={() => delLanc(t.id)} aria-label={`Excluir lançamento ${t.desc}`}>🗑</button>
                            </div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>

            </div>
          </>
        )}

      </main>

      {/* ═══ MODAL: NOVO LANÇAMENTO ═══ */}
      {lancModal && (
        <LancamentoModal
          idPrefix="lanc"
          title="Novo lançamento"
          form={lancForm}
          setForm={setLancForm}
          categorias={categorias}
          tipo={lancType}
          onTipoChange={setLancType}
          descPlaceholder="Digite a descrição"
          valPlaceholder="Digite o valor"
          submitLabel="Confirmar"
          onSubmit={saveLanc}
          salvando={salvando}
          onClose={() => setLancModal(false)}
        />
      )}

      {/* ═══ MODAL: EDITAR LANÇAMENTO ═══ */}
      {editLancModal && (
        <LancamentoModal
          idPrefix="edit-lanc"
          title="Editar lançamento"
          form={editLancForm}
          setForm={setEditLancForm}
          categorias={categorias}
          submitLabel="Salvar alterações"
          onSubmit={saveEditLanc}
          salvando={salvando}
          onClose={() => setEditLancModal(null)}
        />
      )}

      {/* ═══ MODAL: META ═══ */}
      {metaModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setMetaModal(false) }}>
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="meta-modal-title">
            <button className="m-close" onClick={() => setMetaModal(false)} aria-label="Fechar">✕</button>
            <div className="m-title" id="meta-modal-title">{editingMetaId ? 'Editar Meta' : 'Nova Meta'}</div>
            <div className="m-row">
              <label className="m-lbl" htmlFor="meta-desc">Descrição:</label>
              <input id="meta-desc" className="m-inp" type="text" maxLength={255} placeholder="Ex: Casa própria…" value={metaForm.desc} onChange={e => setMetaForm(f => ({ ...f, desc: e.target.value }))} />
            </div>
            <div className="m-row">
              <label className="m-lbl" htmlFor="meta-val">Valor:</label>
              <input id="meta-val" className="m-inp" type="number" placeholder="Ex: 1000" min="0.01" step="0.01" value={metaForm.val} onChange={e => setMetaForm(f => ({ ...f, val: e.target.value }))} />
            </div>
            <button className="m-btn" onClick={saveMeta} disabled={salvando}>Confirmar</button>
          </div>
        </div>
      )}

      {/* ═══ MODAL: CONFIRMAÇÃO ═══ */}
      {confirmModal && (
        <div className="overlay on" onClick={e => { if (e.target.classList.contains('overlay')) setConfirmModal(null) }}>
          <div className="modal narrow" role="dialog" aria-modal="true" aria-labelledby="confirm-modal-title" aria-describedby="confirm-modal-msg">
            <div className="m-title" id="confirm-modal-title" style={{ fontSize: '19px' }}>{confirmModal.title || 'Confirmar exclusão'}</div>
            <p className="conf-msg" id="confirm-modal-msg">{confirmModal.msg}</p>
            <div className="conf-btns">
              <button className="btn-cancel" onClick={() => setConfirmModal(null)}>Cancelar</button>
              <button className="btn-del" onClick={() => { confirmModal.cb(); setConfirmModal(null) }}>{confirmModal.confirmLabel || 'Excluir'}</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══ TOAST ═══ */}
      <div className={`toast${toastVisible ? ' show' : ''}`} role="status" aria-live="polite">{toastMsg}</div>

    </div>
  )
}

export default Dashboard
