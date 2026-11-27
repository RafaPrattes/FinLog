/* Extrai a mensagem de erro enviada pela API ({ status, error, message, path }) */
export function mensagemErro(err, fallback) {
  const message = err?.response?.data?.message
  return typeof message === 'string' && message.trim() ? message : fallback
}
