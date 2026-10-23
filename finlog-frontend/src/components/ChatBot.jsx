import { useEffect, useRef, useState } from 'react'
import api from '../service/api'
import { stripMarkdown } from '../utils/format'

function ChatBot({ user }) {
  const [messages, setMessages] = useState([
    { role: 'assistant', text: `Olá${user?.nome ? ', ' + user.nome.split(' ')[0] : ''}! Sou seu assistente financeiro. Como posso te ajudar hoje?` }
  ])
  const [input,   setInput]   = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function send() {
    const text = input.trim()
    if (!text || loading) return
    setMessages(prev => [...prev, { role: 'user', text }])
    setInput('')
    setLoading(true)

    try {
      const { data } = await api.post('/ai/chat', { mensagem: text })
      setMessages(prev => [...prev, { role: 'assistant', text: data.resposta }])
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Erro ao conectar com o assistente. Tente novamente.' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-wrap">
      <h2 className="page-title">Assistente IA</h2>
      <div className="chat-messages">
        {messages.map((m, i) => (
          <div key={i} className={`chat-bubble ${m.role}`}>{stripMarkdown(m.text)}</div>
        ))}
        {loading && <div className="chat-bubble assistant chat-typing">digitando…</div>}
        <div ref={bottomRef} />
      </div>
      <div className="chat-input-row">
        <input
          className="chat-input"
          type="text"
          placeholder="Digite sua pergunta…"
          aria-label="Pergunta para o assistente"
          maxLength={500}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
        />
        <button className="chat-send" onClick={send} disabled={loading}>Enviar</button>
      </div>
    </div>
  )
}

export default ChatBot
