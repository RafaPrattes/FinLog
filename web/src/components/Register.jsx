import { useState } from 'react'
import api from '../service/api'
import { mensagemErro } from '../service/errors'

function Register({ goToLogin }) {
  const [name,            setName]            = useState('')
  const [email,           setEmail]           = useState('')
  const [confirmEmail,    setConfirmEmail]    = useState('')
  const [password,        setPassword]        = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error,           setError]           = useState('')
  const [loading,         setLoading]         = useState(false)

  async function handleRegister() {
    setError('')

    const n   = name.trim()
    const em  = email.trim()
    const em2 = confirmEmail.trim()
    const p   = password
    const p2  = confirmPassword

    if (!n || !em || !em2 || !p || !p2) {
      setError('Preencha todos os campos.')
      return
    }
    if (em !== em2) {
      setError('Os e-mails não coincidem.')
      return
    }
    if (p !== p2) {
      setError('As senhas não coincidem.')
      return
    }
    if (p.length < 6) {
      setError('A senha deve ter ao menos 6 caracteres.')
      return
    }

    try {
      setLoading(true)
      await api.post('/cadastro', { nome: n, email: em, senha: p })
      goToLogin()
    } catch (err) {
      setError(mensagemErro(err, 'Erro ao cadastrar. Tente novamente.'))
    } finally {
      setLoading(false)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') handleRegister()
  }

  return (
    <div id="screen-register" className="screen active">
      <main className="auth-card register">
        <header className="auth-head">
          <h1 className="brand">FinLog</h1>
          <h2 className="sub">Cadastro</h2>
        </header>

        <div className="auth-form">
          {error && (
            <div className="err-msg" role="alert">{error}</div>
          )}

          <input
            type="text"
            className="field"
            placeholder="Nome completo"
            aria-label="Nome completo"
            autoComplete="name"
            maxLength={100}
            value={name}
            onChange={e => setName(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <input
            type="email"
            className="field"
            placeholder="Cadastre um e-mail"
            aria-label="E-mail"
            autoComplete="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <input
            type="email"
            className="field"
            placeholder="Confirme o e-mail"
            aria-label="Confirmação do e-mail"
            autoComplete="email"
            value={confirmEmail}
            onChange={e => setConfirmEmail(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <input
            type="password"
            className="field"
            placeholder="Cadastre uma senha"
            aria-label="Senha"
            autoComplete="new-password"
            maxLength={72}
            value={password}
            onChange={e => setPassword(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <input
            type="password"
            className="field"
            placeholder="Confirme a senha"
            aria-label="Confirmação da senha"
            autoComplete="new-password"
            maxLength={72}
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            onKeyDown={handleKeyDown}
          />
        </div>

        <div className="auth-footer">
          <button type="button" className="btn-main" onClick={handleRegister} disabled={loading}>
            {loading ? 'Cadastrando…' : 'Cadastrar'}
          </button>

          <button type="button" className="link-btn auth-link" onClick={goToLogin}>
            Retornar para tela de login
          </button>
        </div>
      </main>
    </div>
  )
}

export default Register
