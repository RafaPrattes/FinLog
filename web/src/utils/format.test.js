import { describe, expect, it } from 'vitest'
import {
  brl, brlSinal, fmtDate, filtrarMovimentacoes, horaMensagem, iniciais, intervaloPeriodo,
  isoLocal, labelData, mapMeta, mapMovimentacao, ordenarRecentes, pct, stripMarkdown, totais,
} from './format'

/* Data fixa para os testes: quinta-feira, 22/10/2026 */
const HOJE = new Date(2026, 9, 22, 15, 30)

describe('brl', () => {
  it('formata no padrão brasileiro com duas casas', () => {
    expect(brl(1234.5)).toBe('R$ 1.234,50')
    expect(brl(0)).toBe('R$ 0,00')
  })

  it('usa o valor absoluto (o sinal é mostrado à parte)', () => {
    expect(brl(-42.1)).toBe('R$ 42,10')
  })
})

describe('brlSinal', () => {
  it('prefixa + para entradas e - para saídas', () => {
    expect(brlSinal('entrada', 500)).toBe('+ R$ 500,00')
    expect(brlSinal('saida', 4000)).toBe('- R$ 4.000,00')
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

describe('isoLocal', () => {
  it('usa o fuso local, não UTC', () => {
    expect(isoLocal(new Date(2026, 0, 5, 23, 59))).toBe('2026-01-05')
  })
})

describe('labelData', () => {
  it('mostra "Hoje" e "Ontem"', () => {
    expect(labelData('2026-10-22', HOJE)).toBe('Hoje')
    expect(labelData('2026-10-21', HOJE)).toBe('Ontem')
  })

  it('mostra dia e mês abreviado no ano corrente', () => {
    expect(labelData('2026-10-20', HOJE)).toBe('20 Out')
    expect(labelData('2026-03-05', HOJE)).toBe('05 Mar')
  })

  it('inclui o ano quando é de outro ano', () => {
    expect(labelData('2025-12-31', HOJE)).toBe('31 Dez 2025')
  })

  it('reconhece "Ontem" na virada do mês', () => {
    expect(labelData('2026-09-30', new Date(2026, 9, 1))).toBe('Ontem')
  })

  it('devolve vazio sem data', () => {
    expect(labelData('', HOJE)).toBe('')
  })
})

describe('horaMensagem', () => {
  it('formata como "Hoje, HH:MM"', () => {
    expect(horaMensagem(new Date(2026, 9, 22, 10, 5), HOJE)).toBe('Hoje, 10:05')
  })
})

describe('intervaloPeriodo', () => {
  it('"Este Mês" vai do dia 1 ao último dia do mês', () => {
    expect(intervaloPeriodo('mes', HOJE)).toEqual({ inicio: '2026-10-01', fim: '2026-10-31' })
  })

  it('"Últimos 30 dias" inclui hoje e os 29 dias anteriores', () => {
    expect(intervaloPeriodo('30dias', HOJE)).toEqual({ inicio: '2026-09-23', fim: '2026-10-22' })
  })

  it('"Personalizado" usa as datas informadas, aceitando só uma das pontas', () => {
    expect(intervaloPeriodo('personalizado', HOJE, { de: '2026-01-01', ate: '2026-01-31' }))
      .toEqual({ inicio: '2026-01-01', fim: '2026-01-31' })
    expect(intervaloPeriodo('personalizado', HOJE, { de: '2026-05-01' }).fim).toBe('9999-12-31')
    expect(intervaloPeriodo('personalizado', HOJE, {})).toBeNull()
  })

  it('"tudo" não restringe', () => {
    expect(intervaloPeriodo('tudo', HOJE)).toBeNull()
  })
})

describe('filtrarMovimentacoes', () => {
  const txns = [
    { id: 1, type: 'entrada', val: 5000, date: '2026-10-20', cat: 'Salário' },
    { id: 2, type: 'saida',   val: 350,  date: '2026-10-22', cat: 'Alimentação' },
    { id: 3, type: 'saida',   val: 120,  date: '2026-09-10', cat: 'Utilidades' },
    { id: 4, type: 'entrada', val: 42.5, date: '2026-09-25', cat: 'Investimentos' },
  ]
  const ids = lista => lista.map(t => t.id)

  it('sem filtros devolve tudo', () => {
    expect(ids(filtrarMovimentacoes(txns, {}, HOJE))).toEqual([1, 2, 3, 4])
  })

  it('filtra por tipo', () => {
    expect(ids(filtrarMovimentacoes(txns, { tipo: 'entrada' }, HOJE))).toEqual([1, 4])
    expect(ids(filtrarMovimentacoes(txns, { tipo: 'saida' }, HOJE))).toEqual([2, 3])
  })

  it('filtra por período', () => {
    expect(ids(filtrarMovimentacoes(txns, { periodo: 'mes' }, HOJE))).toEqual([1, 2])
    expect(ids(filtrarMovimentacoes(txns, { periodo: '30dias' }, HOJE))).toEqual([1, 2, 4])
    expect(ids(filtrarMovimentacoes(txns, { periodo: 'personalizado', de: '2026-09-01', ate: '2026-09-30' }, HOJE))).toEqual([3, 4])
  })

  it('combina tipo, período e categoria', () => {
    expect(ids(filtrarMovimentacoes(txns, { tipo: 'saida', periodo: 'mes' }, HOJE))).toEqual([2])
    expect(ids(filtrarMovimentacoes(txns, { categoria: 'Utilidades' }, HOJE))).toEqual([3])
  })
})

describe('ordenarRecentes', () => {
  it('ordena por data decrescente e, no mesmo dia, pelo id', () => {
    const lista = [
      { id: 1, date: '2026-10-01' },
      { id: 3, date: '2026-10-05' },
      { id: 2, date: '2026-10-05' },
    ]
    expect(ordenarRecentes(lista).map(t => t.id)).toEqual([3, 2, 1])
  })
})

describe('totais e pct', () => {
  it('soma entradas, saídas e saldo', () => {
    expect(totais([
      { type: 'entrada', val: 6500 },
      { type: 'saida', val: 5400 },
    ])).toEqual({ entradas: 6500, saidas: 5400, saldo: 1100 })
  })

  it('calcula o percentual com uma casa', () => {
    expect(pct(6500, 11900)).toBe('54,6%')
    expect(pct(5400, 11900)).toBe('45,4%')
    expect(pct(0, 0)).toBe('0,0%')
  })
})

describe('iniciais', () => {
  it('usa as duas primeiras iniciais', () => {
    expect(iniciais('Serena  Gilmore Souza')).toBe('SG')
    expect(iniciais('')).toBe('U')
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
