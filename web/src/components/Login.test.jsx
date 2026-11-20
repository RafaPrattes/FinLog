import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import api from '../service/api'
import Login from './Login'

vi.mock('../service/api', () => ({
  default: { post: vi.fn() },
}))

async function preencher(user) {
  await user.type(screen.getByLabelText('E-mail'), 'ana@exemplo.com')
  await user.type(screen.getByLabelText('Senha'), 'segredo123')
}

describe('Login', () => {
  beforeEach(() => {
    vi.mocked(api.post).mockReset()
  })

  it('pede para preencher os campos ao enviar vazio, sem chamar a API', async () => {
    const user = userEvent.setup()
    render(<Login goToRegister={() => {}} onLogin={() => {}} />)

    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(screen.getByText('Preencha todos os campos.')).toBeInTheDocument()
    expect(api.post).not.toHaveBeenCalled()
  })

  it('mostra a mensagem da API quando o login é bloqueado (429)', async () => {
    vi.mocked(api.post).mockRejectedValue({
      response: { status: 429, data: { status: 429, message: 'Muitas tentativas. Tente novamente em alguns minutos.' } },
    })
    const user = userEvent.setup()
    render(<Login goToRegister={() => {}} onLogin={() => {}} />)

    await preencher(user)
    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(await screen.findByText('Muitas tentativas. Tente novamente em alguns minutos.')).toBeInTheDocument()
    expect(screen.queryByText('Usuário ou senha inválidos.')).not.toBeInTheDocument()
  })

  it('mostra "Usuário ou senha inválidos." no 401', async () => {
    vi.mocked(api.post).mockRejectedValue({ response: { status: 401, data: { message: 'Credenciais inválidas' } } })
    const user = userEvent.setup()
    render(<Login goToRegister={() => {}} onLogin={() => {}} />)

    await preencher(user)
    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(await screen.findByText('Usuário ou senha inválidos.')).toBeInTheDocument()
  })

  it('envia e-mail e senha e repassa a escolha de permanecer conectado', async () => {
    const resposta = { usuario: { id: 1, nome: 'Ana' }, token: 't' }
    vi.mocked(api.post).mockResolvedValue({ data: resposta })
    const onLogin = vi.fn()
    const user = userEvent.setup()
    render(<Login goToRegister={() => {}} onLogin={onLogin} />)

    await preencher(user)
    await user.click(screen.getByLabelText('Permanecer Conectado'))
    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(api.post).toHaveBeenCalledWith('/auth/login', { email: 'ana@exemplo.com', senha: 'segredo123' })
    expect(onLogin).toHaveBeenCalledWith(resposta, false)
  })
})
