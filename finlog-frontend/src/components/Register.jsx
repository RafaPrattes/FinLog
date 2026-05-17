import { useState } from 'react'
import api from '../service/api'

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
      const msg = err.response?.data || 'Erro ao cadastrar. Tente novamente.'
      setError(typeof msg === 'string' ? msg : 'Erro ao cadastrar. Tente novamente.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div id="screen-register" className="screen active">
      <div className="auth-card">
        <div className="brand">FinLog</div>
        <div className="sub">Cadastro</div>

        {error && (
          <div className="err-msg" style={{ display: 'block' }}>{error}</div>
        )}

        <input
          type="text"
          className="field"
          placeholder="Nome completo"
          value={name}
          onChange={e => setName(e.target.value)}
        />
        <input
          type="email"
          className="field"
          placeholder="Cadastre um e-mail"
          value={email}
          onChange={e => setEmail(e.target.value)}
        />
        <input
          type="email"
          className="field"
          placeholder="Confirme o e-mail"
          value={confirmEmail}
          onChange={e => setConfirmEmail(e.target.value)}
        />
        <input
          type="password"
          className="field"
          placeholder="Cadastre uma senha"
          value={password}
          onChange={e => setPassword(e.target.value)}
        />
        <input
          type="password"
          className="field"
          placeholder="Confirme a senha"
          value={confirmPassword}
          onChange={e => setConfirmPassword(e.target.value)}
        />

        <button className="btn-main" onClick={handleRegister} disabled={loading}>
          {loading ? 'Cadastrando…' : 'Cadastrar'}
        </button>

        <button className="link-btn" onClick={goToLogin}>
          Retornar para tela de login
        </button>
      </div>
    </div>
  )
}

export default Register
