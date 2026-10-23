import { describe, expect, it } from 'vitest'
import { brl, fmtDate, mapMeta, mapMovimentacao, stripMarkdown } from './format'

describe('brl', () => {
  it('formata no padrão brasileiro com duas casas', () => {
    expect(brl(1234.5)).toBe('R$1.234,50')
    expect(brl(0)).toBe('R$0,00')
  })

  it('usa o valor absoluto (o sinal é mostrado à parte)', () => {
    expect(brl(-42.1)).toBe('R$42,10')
  })
})

describe('fmtDate', () => {
  it('converte AAAA-MM-DD para DD/MM/AAAA', () => {
    expect(fmtDate('2026-03-07')).toBe('07/03/2026')
  })

  it('devolve string vazia sem data', () => {
    expect(fmtDate('')).toBe('')
    expect(fmtDate(null)).toBe('')
  })
})

describe('mapMovimentacao', () => {
  it('converte a resposta da API para o formato da tela', () => {
    const m = mapMovimentacao({
      id: 7,
      tipo: 'RECEITA',
      descricao: 'Salário',
      valor: '3500.00',
      data: '2026-10-05',
      categoria: { id: 2, nome: 'Trabalho' },
    })
    expect(m).toEqual({
      id: 7,
      type: 'entrada',
      desc: 'Salário',
      val: 3500,
      date: '2026-10-05',
      cat: 'Trabalho',
      catId: 2,
    })
  })

  it('trata despesa sem categoria e valor inválido', () => {
    const m = mapMovimentacao({ id: 1, tipo: 'DESPESA', descricao: 'X', valor: 'abc', data: '2026-01-01' })
    expect(m.type).toBe('saida')
    expect(m.val).toBe(0)
    expect(m.cat).toBe('Outros')
    expect(m.catId).toBeNull()
  })
})

describe('mapMeta', () => {
  it('converte a meta da API', () => {
    expect(mapMeta({ id: 3, descricao: 'Viagem', valor: '1500.5' })).toEqual({ id: 3, desc: 'Viagem', val: 1500.5 })
  })

  it('usa 0 quando o valor não é numérico', () => {
    expect(mapMeta({ id: 4, descricao: 'Y', valor: null }).val).toBe(0)
  })
})

describe('stripMarkdown', () => {
  it('remove negrito, títulos e marcadores de lista', () => {
    expect(stripMarkdown('## Dica\n- **Economize** 10%')).toBe('Dica\nEconomize 10%')
  })
})
