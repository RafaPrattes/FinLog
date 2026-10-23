import { useEffect, useRef, useState } from 'react'
import api from '../service/api'
import Icon from './Icon'
import { PageHeader } from './PageHeader'
import { horaMensagem, iniciais, stripMarkdown } from '../utils/format'

/* Sugestões rápidas do protótipo que têm resposta pelo chat
   ("Exportar essa análise" ficou de fora: não há funcionalidade por trás). */
const SUGESTOES = [
  'Como criar uma Meta?',
  'Ver meu saldo líquido',
  'Onde estou gastando mais?',
]

function ChatBot({ user }) {
  const [messages, setMessages] = useState(() => [
    {
      role: 'assistant',
      text: `Olá${user?.nome ? ', ' + user.nome.split(' ')[0] : ''}! Sou sua conselheira financeira. Como posso te ajudar hoje?`,
      at: new Date(),
    },
  ])
  const [input,   setInput]   = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  async function send(texto = input) {
    const text = texto.trim()
    if (!text || loading) return
    setMessages(prev => [...prev, { role: 'user', text, at: new Date() }])
    setInput('')
    setLoading(true)

    try {
      const { data } = await api.post('/ai/chat', { mensagem: text })
      setMessages(prev => [...prev, { role: 'assistant', text: data.resposta, at: new Date() }])
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Erro ao conectar com o assistente. Tente novamente.', at: new Date() }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <PageHeader title="IA Conselheira" subtitle="Sua assistente financeira inteligente">
        <span className="status-pill">
          <span className="status-dot" aria-hidden="true" />
          Online e Pronta
        </span>
      </PageHeader>

      <section className="card chat-container" aria-label="Conversa com a IA Conselheira">
        <div className="chat-messages" role="log" aria-live="polite">
          {messages.map((m, i) => (
            <div key={i} className={`msg-row ${m.role}`}>
              {m.role === 'assistant' && (
                <span className="ai-avatar" aria-hidden="true"><Icon name="bot" size={20} /></span>
              )}
              <div className="chat-bubble">
                <span className="sr-only">{m.role === 'user' ? 'Você: ' : 'IA Conselheira: '}</span>
                <p className="msg-text">{stripMarkdown(m.text)}</p>
                <time className="msg-time" dateTime={m.at.toISOString()}>{horaMensagem(m.at)}</time>
              </div>
              {m.role === 'user' && (
                <span className="avatar sm" aria-hidden="true">{iniciais(user?.nome)}</span>
              )}
            </div>
          ))}
          {loading && (
            <div className="msg-row assistant">
              <span className="ai-avatar" aria-hidden="true"><Icon name="bot" size={20} /></span>
              <div className="chat-bubble chat-typing">digitando…</div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <div className="suggestions" aria-label="Sugestões de perguntas">
          {SUGESTOES.map(s => (
            <button key={s} type="button" className="suggestion" onClick={() => send(s)} disabled={loading}>
              {s}
            </button>
          ))}
        </div>

        <div className="chat-input-bar">
          <input
            className="chat-input"
            type="text"
            placeholder="Digite sua pergunta..."
            aria-label="Pergunta para o assistente"
            maxLength={500}
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && send()}
          />
          <button type="button" className="btn btn-primary btn-sm-radius chat-send" onClick={() => send()} disabled={loading}>
            Enviar <Icon name="send" size={16} />
          </button>
        </div>
      </section>
    </>
  )
}

export default ChatBot
