import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import api from '../service/api'
import { today } from '../utils/format'
import Dashboard from './Dashboard'

vi.mock('../service/api', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

const USER = { id: 1, nome: 'Serena Gilmore', email: 'serena@finlog.test' }

const MOVIMENTACOES = [
  { id: 1, tipo: 'RECEITA', descricao: 'Salário Mensal', valor: '6500.00', data: today(), categoria: { id: 1, nome: 'Salário' } },
  { id: 2, tipo: 'DESPESA', descricao: 'Supermercado Empório', valor: '5400.00', data: today(), categoria: { id: 2, nome: 'Alimentação' } },
  { id: 3, tipo: 'DESPESA', descricao: 'Conta antiga', valor: '10.00', data: '2020-01-15', categoria: { id: 3, nome: 'Utilidades' } },
]
const METAS = [{ id: 9, descricao: 'Entrada para a casa própria', valor: '1000.00' }]

function mockApi({ falhar = false } = {}) {
  vi.mocked(api.get).mockImplementation(url => {
    if (falhar) return Promise.reject(new Error('offline'))
    if (url === '/movimentacoes') return Promise.resolve({ data: MOVIMENTACOES })
    if (url === '/metas')         return Promise.resolve({ data: METAS })
    if (url === '/categorias')    return Promise.resolve({ data: [{ id: 1, nome: 'Salário' }, { id: 2, nome: 'Alimentação' }] })
    return Promise.reject(new Error('url inesperada ' + url))
  })
}

async function renderDashboard() {
  const user = userEvent.setup()
  render(<Dashboard user={USER} onLogout={() => {}} />)
  await screen.findAllByText('Supermercado Empório', { exact: false })
  return user
}

describe('Dashboard', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    for (const fn of Object.values(api)) vi.mocked(fn).mockReset()
    mockApi()
  })

  it('mostra a Página Inicial com totais, percentuais e metas', async () => {
    await renderDashboard()

    expect(screen.getByRole('heading', { level: 1, name: 'Página Inicial' })).toBeInTheDocument()
    expect(screen.getByText('R$ 6.500,00 · 54,6%')).toBeInTheDocument()
    expect(screen.getByText('R$ 5.410,00 · 45,4%')).toBeInTheDocument()
    expect(screen.getByText('Entrada para a casa própria')).toBeInTheDocument()
    expect(screen.getAllByText('- R$ 5.400,00').length).toBeGreaterThan(0)
  })

  it('troca de tela pelo menu lateral', async () => {
    const user = await renderDashboard()
    const nav = screen.getByRole('navigation', { name: 'Menu principal' })

    await user.click(within(nav).getByRole('button', { name: /Extrato Detalhado/ }))
    expect(screen.getByRole('heading', { level: 1, name: 'Extrato Detalhado' })).toBeInTheDocument()
    expect(within(nav).getByRole('button', { name: /Extrato Detalhado/ })).toHaveAttribute('aria-current', 'page')

    await user.click(within(nav).getByRole('button', { name: /Metas/ }))
    expect(screen.getByRole('heading', { level: 1, name: 'Metas' })).toBeInTheDocument()

    await user.click(within(nav).getByRole('button', { name: /IA Conselheira/ }))
    expect(screen.getByRole('heading', { level: 1, name: 'IA Conselheira' })).toBeInTheDocument()
    expect(screen.getByText('Online e Pronta')).toBeInTheDocument()

    await user.click(within(nav).getByRole('button', { name: /Configurações/ }))
    expect(screen.getByLabelText('E-mail')).toHaveValue('serena@finlog.test')
    expect(screen.queryByRole('button', { name: 'Sair da conta' })).not.toBeInTheDocument()

    await user.click(within(nav).getByRole('button', { name: /Página Inicial/ }))
    expect(screen.getByRole('heading', { level: 1, name: 'Página Inicial' })).toBeInTheDocument()
  })

  it('filtra o extrato por tipo e por período', async () => {
    const user = await renderDashboard()
    await user.click(screen.getByRole('button', { name: /Extrato Detalhado/ }))
    const tabela = screen.getByRole('table')

    /* Padrão do protótipo: "Este Mês" */
    expect(screen.getByRole('button', { name: 'Este Mês' })).toHaveAttribute('aria-pressed', 'true')
    expect(within(tabela).getAllByRole('row')).toHaveLength(3) // cabeçalho + 2 do mês
    expect(within(tabela).queryByText('Conta antiga')).not.toBeInTheDocument()
    expect(within(tabela).getAllByText('Hoje')).toHaveLength(2)

    await user.click(screen.getByRole('button', { name: 'Saídas' }))
    expect(within(tabela).queryByText('Salário Mensal')).not.toBeInTheDocument()
    expect(within(tabela).getByText('Supermercado Empório')).toBeInTheDocument()

    /* Personalizado sem datas mostra todo o histórico; com datas, restringe */
    await user.click(screen.getByRole('button', { name: 'Personalizado' }))
    expect(within(tabela).getByText('Conta antiga')).toBeInTheDocument()

    await user.type(screen.getByLabelText('até'), '2020-12-31')
    expect(within(tabela).getByText('Conta antiga')).toBeInTheDocument()
    expect(within(tabela).queryByText('Supermercado Empório')).not.toBeInTheDocument()
  })

  it('o atalho do cartão de saldo leva ao extrato detalhado', async () => {
    const user = await renderDashboard()

    await user.click(screen.getByRole('button', { name: 'Ver extrato detalhado' }))

    expect(screen.getByRole('heading', { level: 1, name: 'Extrato Detalhado' })).toBeInTheDocument()
  })

  it('abre o modal de lançamento já com o tipo escolhido no cartão de saldo', async () => {
    const user = await renderDashboard()

    await user.click(screen.getByRole('button', { name: /^Saída/ }))

    const dialog = screen.getByRole('dialog', { name: 'Novo lançamento' })
    expect(within(dialog).getByRole('radio', { name: 'Saída' })).toHaveAttribute('aria-checked', 'true')

    await user.keyboard('{Escape}')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('pede confirmação antes de sair pelo cartão da pessoa', async () => {
    const user = await renderDashboard()

    await user.click(screen.getByRole('button', { name: /Serena Gilmore/ }))

    expect(screen.getByRole('dialog', { name: 'Encerrar sessão' })).toBeInTheDocument()
    expect(screen.getByText('serena@finlog.test')).toBeInTheDocument()
  })

  it('mostra erro de carregamento e permite tentar novamente', async () => {
    mockApi({ falhar: true })
    const user = userEvent.setup()
    render(<Dashboard user={USER} onLogout={() => {}} />)

    const botoes = await screen.findAllByRole('button', { name: 'Tentar novamente' })
    mockApi()
    await user.click(botoes[0])

    expect(await screen.findByText('Entrada para a casa própria')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Tentar novamente' })).not.toBeInTheDocument()
  })
})
