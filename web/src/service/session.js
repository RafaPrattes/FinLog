const CHAVE = 'token'

/* "Permanecer conectado": localStorage sobrevive ao fechar o navegador,
   sessionStorage dura só enquanto a aba estiver aberta */
export function salvarToken(token, lembrar = true) {
  limparToken()
  const storage = lembrar ? localStorage : sessionStorage
  storage.setItem(CHAVE, token)
}

export function lerToken() {
  return localStorage.getItem(CHAVE) || sessionStorage.getItem(CHAVE)
}

export function limparToken() {
  localStorage.removeItem(CHAVE)
  sessionStorage.removeItem(CHAVE)
}
