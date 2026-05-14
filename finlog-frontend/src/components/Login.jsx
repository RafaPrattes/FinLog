import { useState } from 'react'

function Login({ goToRegister, onLogin }) {
  const [userInput, setUserInput] = useState('')
  const [password,  setPassword]  = useState('')
  const [error,     setError]     = useState('')
  const [userErr,   setUserErr]   = useState(false)
  const [passErr,   setPassErr]   = useState(false)

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

    /*
     * TODO: integração com a API
     * Substituir o bloco abaixo por:
     *
     * try {
     *   const { data } = await axios.post('/api/auth/login', { email: u, password: p })
     *   onLogin(data.user) // ou data conforme o retorno do backend
     * } catch (err) {
     *   setError('Usuário ou senha inválidos.')
     *   setUserErr(true)
     *   setPassErr(true)
     * }
     */
    setError('Autenticação com backend ainda não implementada.')
    setUserErr(true)
    setPassErr(true)
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
          type="text"
          className={`field${userErr ? ' err' : ''}`}
          placeholder="Usuário"
          value={userInput}
          onChange={e => setUserInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />

        <input
          type="password"
          className={`field${passErr ? ' err' : ''}`}
          placeholder="Senha"
          value={password}
          onChange={e => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
        />

        <div className="remember-row">
          <input type="checkbox" id="remember" defaultChecked />
          <label htmlFor="remember">Permanecer Conectado</label>
        </div>

        <button className="btn-main" onClick={handleLogin}>
          Login
        </button>

        <button className="link-btn" onClick={goToRegister}>
          Não tem cadastro? Clique aqui
        </button>

        {/* TODO: remover após integração com backend */}
      </div>
    </div>
  )
}

export default Login
