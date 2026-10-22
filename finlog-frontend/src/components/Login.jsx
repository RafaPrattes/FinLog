import { useState } from 'react'
import api from '../service/api'
import { mensagemErro } from '../service/errors'

function Login({ aviso = '', goToRegister, onLogin }) {
  const [userInput, setUserInput] = useState('')
  const [password,  setPassword]  = useState('')
  const [error,     setError]     = useState(aviso)
  const [userErr,   setUserErr]   = useState(false)
  const [passErr,   setPassErr]   = useState(false)
  const [loading,   setLoading]   = useState(false)
  const [lembrar,   setLembrar]   = useState(true)

  async function handleLogin() {
    setError('')
    setUserErr(false)
    setPassErr(false)

    const u = userInput.trim()
    const p = password

    if (!u || !p) {
      setError('Preencha todos os campos.')
      setUserErr(!u)
      setPassErr(!p)
      return
    }

    try {
      setLoading(true)
      const { data } = await api.post('/auth/login', { email: u, senha: p })
      onLogin(data, lembrar)
    } catch (err) {
      const status = err.response?.status
      if (status === 401) {
        setError('Usuário ou senha inválidos.')
        setUserErr(true)
        setPassErr(true)
      } else {
        setError(mensagemErro(err, 'Não foi possível entrar. Tente novamente.'))
      }
    } finally {
      setLoading(false)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') handleLogin()
  }

  return (
    <div id="screen-login" className="screen active">
      <div className="auth-card">
        <div className="brand">FinLog</div>
        <div className="sub">Login</div>

        {error && (
          <div className="err-msg" style={{ display: 'block' }}>{error}</div>
        )}

        <input
          type="email"
          autoComplete="email"
          className={`field${userErr ? ' err' : ''}`}
          placeholder="E-mail"
          value={userInput}
          onChange={e => setUserInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <input
          type="password"
          autoComplete="current-password"
          className={`field${passErr ? ' err' : ''}`}
          placeholder="Senha"
          value={password}
          onChange={e => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
        />

        <div className="remember-row">
          <input
            type="checkbox"
            id="remember"
            checked={lembrar}
            onChange={e => setLembrar(e.target.checked)}
          />
          <label htmlFor="remember">Permanecer conectado</label>
        </div>

        <button className="btn-main" onClick={handleLogin} disabled={loading}>
          {loading ? 'Entrando…' : 'Login'}
        </button>

        <button className="link-btn" onClick={goToRegister}>
          Não tem cadastro? Clique aqui
        </button>
      </div>
    </div>
  )
}

export default Login
