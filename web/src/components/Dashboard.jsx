import { useState, useEffect, useRef } from 'react'
import api from '../service/api'
import { mensagemErro } from '../service/errors'
import { today, mapMeta, mapMovimentacao } from '../utils/format'
import Sidebar from './Sidebar'
import ChatBot from './ChatBot'
import LancamentoModal from './LancamentoModal'
import MetaModal from './MetaModal'
import ConfirmModal from './ConfirmModal'
import Inicio from '../pages/Inicio'
import Extrato from '../pages/Extrato'
import Metas from '../pages/Metas'
import Configuracoes from '../pages/Configuracoes'

/* ══════════════════════════════
   DASHBOARD
══════════════════════════════ */
function Dashboard({ user, onLogout }) {
  const [txns,       setTxns]       = useState([])
  const [metas,      setMetas]      = useState([])
  const [loading,    setLoading]    = useState(true)
  const [loadError,  setLoadError]  = useState(false)
  const [reloadKey,  setReloadKey]  = useState(0)
  /* Tela ativa: home | extrato | metas | chat | config */
  const [view,       setView]       = useState('home')

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
          type:  editLancModal.type,
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
      variant:      'primary',
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

  const estado = { loading, loadError, onRetry: recarregar }

  /* ══════════════════════════════
     RENDER
  ══════════════════════════════ */
  return (
    <div id="screen-dashboard" className={`screen active${view === 'chat' ? ' chat-page' : ''}`}>

      <Sidebar view={view} onNavigate={setView} user={user} onUserClick={confirmLogout} />

      <main className="workspace" key={view}>
        {view === 'home' && (
          <Inicio
            txns={txns}
            metas={metas}
            {...estado}
            onNovoLanc={openLanc}
            onVerExtrato={() => setView('extrato')}
            onAddMeta={() => openMetaModal(null)}
            onEditMeta={openMetaModal}
            onDelMeta={delMeta}
          />
        )}

        {view === 'extrato' && (
          <Extrato
            txns={txns}
            {...estado}
            onNovoLanc={openLanc}
            onEdit={openEditLanc}
            onDelete={delLanc}
          />
        )}

        {view === 'metas' && (
          <Metas
            metas={metas}
            {...estado}
            onAdd={() => openMetaModal(null)}
            onEdit={openMetaModal}
            onDelete={delMeta}
          />
        )}

        {view === 'chat' && <ChatBot user={user} />}

        {view === 'config' && <Configuracoes user={user} />}
      </main>

      {/* ═══ MODAL: NOVO LANÇAMENTO ═══ */}
      {lancModal && (
        <LancamentoModal
          idPrefix="lanc"
          title="Novo lançamento"
          subtitle="Registre uma entrada ou saída no seu extrato."
          form={lancForm}
          setForm={setLancForm}
          categorias={categorias}
          tipo={lancType}
          onTipoChange={setLancType}
          submitLabel="Salvar lançamento"
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
          subtitle="Atualize os dados deste lançamento"
          form={editLancForm}
          setForm={setEditLancForm}
          categorias={categorias}
          tipo={editLancModal.type}
          onTipoChange={type => setEditLancModal(m => ({ ...m, type }))}
          submitLabel="Salvar alterações"
          onSubmit={saveEditLanc}
          salvando={salvando}
          onClose={() => setEditLancModal(null)}
        />
      )}

      {/* ═══ MODAL: META ═══ */}
      {metaModal && (
        <MetaModal
          editando={Boolean(editingMetaId)}
          form={metaForm}
          setForm={setMetaForm}
          onSubmit={saveMeta}
          salvando={salvando}
          onClose={() => setMetaModal(false)}
        />
      )}

      {/* ═══ MODAL: CONFIRMAÇÃO ═══ */}
      {confirmModal && (
        <ConfirmModal confirm={confirmModal} onClose={() => setConfirmModal(null)} />
      )}

      {/* ═══ TOAST ═══ */}
      <div className={`toast${toastVisible ? ' show' : ''}`} role="status" aria-live="polite">{toastMsg}</div>

    </div>
  )
}

export default Dashboard
