import { describe, expect, it } from 'vitest'
import { mensagemErro } from './errors'

describe('mensagemErro', () => {
  it('usa a mensagem enviada pela API', () => {
    const err = { response: { data: { status: 409, error: 'Conflict', message: 'E-mail já cadastrado', path: '/api/cadastro' } } }
    expect(mensagemErro(err, 'Erro genérico')).toBe('E-mail já cadastrado')
  })

  it('usa o texto padrão sem resposta (erro de rede)', () => {
    expect(mensagemErro(new Error('Network Error'), 'Erro genérico')).toBe('Erro genérico')
  })

  it('usa o texto padrão quando message não é uma string útil', () => {
    expect(mensagemErro({ response: { data: 'texto solto' } }, 'Padrão')).toBe('Padrão')
    expect(mensagemErro({ response: { data: { message: 123 } } }, 'Padrão')).toBe('Padrão')
    expect(mensagemErro({ response: { data: { message: '  ' } } }, 'Padrão')).toBe('Padrão')
    expect(mensagemErro(undefined, 'Padrão')).toBe('Padrão')
  })
})
