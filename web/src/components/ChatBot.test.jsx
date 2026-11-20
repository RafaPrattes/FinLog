import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import api from '../service/api'
import ChatBot from './ChatBot'

vi.mock('../service/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}))

describe('ChatBot', () => {
  beforeEach(() => {
    vi.mocked(api.post).mockReset()
    vi.mocked(api.get).mockReset()
    vi.mocked(api.get).mockResolvedValue({ data: [] })
  })

  it('recupera a conversa da sessão ao abrir o chat', async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: [
        { papel: 'USUARIO', texto: 'Onde gasto mais?', criadaEm: '2026-10-22T13:00:00Z' },
        { papel: 'IA', texto: 'Em **Moradia**.', criadaEm: '2026-10-22T13:00:03Z' },
      ],
    })
    render(<ChatBot user={{ nome: 'Ana' }} />)

    expect(api.get).toHaveBeenCalledWith('/ai/chat')
    expect(await screen.findByText('Onde gasto mais?')).toBeInTheDocument()
    expect(screen.getByText('Em Moradia.')).toBeInTheDocument()
    const textos = screen.getAllByText(/./, { selector: '.msg-text' }).map(e => e.textContent)
    expect(textos).toEqual(['Olá, Ana! Sou sua conselheira financeira. Como posso te ajudar hoje?', 'Onde gasto mais?', 'Em Moradia.'])
  })

  it('segue só com a saudação quando não consegue buscar o histórico', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('falhou'))
    render(<ChatBot user={{ nome: 'Ana' }} />)

    expect(await screen.findByText(/Olá, Ana!/)).toBeInTheDocument()
    expect(screen.getAllByText(/./, { selector: '.msg-text' })).toHaveLength(1)
  })

  it('cumprimenta pelo primeiro nome', () => {
    render(<ChatBot user={{ nome: 'Ana Souza' }} />)
    expect(screen.getByText(/Olá, Ana!/)).toBeInTheDocument()
  })

  it('envia { mensagem } para /ai/chat e mostra a resposta sem markdown', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { resposta: '**Guarde** 10% do salário.' } })
    const user = userEvent.setup()
    render(<ChatBot user={{ nome: 'Ana' }} />)

    await user.type(screen.getByLabelText('Pergunta para o assistente'), 'Como economizar?')
    await user.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(api.post).toHaveBeenCalledWith('/ai/chat', { mensagem: 'Como economizar?' })
    expect(screen.getByText('Como economizar?')).toBeInTheDocument()
    expect(await screen.findByText('Guarde 10% do salário.')).toBeInTheDocument()
  })

  it('mostra aviso quando o assistente falha', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('falhou'))
    const user = userEvent.setup()
    render(<ChatBot user={{ nome: 'Ana' }} />)

    await user.type(screen.getByLabelText('Pergunta para o assistente'), 'Oi{Enter}')

    expect(await screen.findByText('Erro ao conectar com o assistente. Tente novamente.')).toBeInTheDocument()
  })

  it('envia o texto da sugestão rápida ao clicar nela', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { resposta: 'Seu saldo líquido é R$ 100,00.' } })
    const user = userEvent.setup()
    render(<ChatBot user={{ nome: 'Ana' }} />)

    await user.click(screen.getByRole('button', { name: 'Ver meu saldo líquido' }))

    expect(api.post).toHaveBeenCalledWith('/ai/chat', { mensagem: 'Ver meu saldo líquido' })
    expect(await screen.findByText('Seu saldo líquido é R$ 100,00.')).toBeInTheDocument()
  })

  it('mostra o horário das mensagens e as três sugestões do protótipo', () => {
    render(<ChatBot user={{ nome: 'Ana' }} />)
    expect(screen.getByText(/^Hoje, \d{2}:\d{2}$/)).toBeInTheDocument()
    for (const s of ['Como criar uma Meta?', 'Ver meu saldo líquido', 'Onde estou gastando mais?']) {
      expect(screen.getByRole('button', { name: s })).toBeInTheDocument()
    }
    expect(screen.queryByRole('button', { name: 'Exportar essa análise' })).not.toBeInTheDocument()
  })
})
