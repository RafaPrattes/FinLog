import { describe, expect, it } from 'vitest'
import { lerToken, limparToken, salvarToken } from './session'

describe('sessão', () => {
  it('guarda no localStorage quando é para permanecer conectado', () => {
    salvarToken('abc', true)
    expect(localStorage.getItem('token')).toBe('abc')
    expect(sessionStorage.getItem('token')).toBeNull()
    expect(lerToken()).toBe('abc')
  })

  it('guarda no sessionStorage quando não é para permanecer conectado', () => {
    salvarToken('xyz', false)
    expect(sessionStorage.getItem('token')).toBe('xyz')
    expect(localStorage.getItem('token')).toBeNull()
    expect(lerToken()).toBe('xyz')
  })

  it('um novo login substitui o token salvo no outro armazenamento', () => {
    salvarToken('antigo', true)
    salvarToken('novo', false)
    expect(localStorage.getItem('token')).toBeNull()
    expect(lerToken()).toBe('novo')
  })

  it('limparToken remove dos dois armazenamentos', () => {
    localStorage.setItem('token', 'a')
    sessionStorage.setItem('token', 'b')
    limparToken()
    expect(lerToken()).toBeNull()
  })
})
