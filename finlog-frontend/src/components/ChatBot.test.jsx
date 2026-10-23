import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import api from '../service/api'
import ChatBot from './ChatBot'

vi.mock('../service/api', () => ({
  default: { post: vi.fn() },
}))

describe('ChatBot', () => {
  beforeEach(() => {
    vi.mocked(api.post).mockReset()
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
