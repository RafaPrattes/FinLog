import { useState } from 'react'

/* Usuários demo — futuramente virão do backend */
const USERS = [
  { name: 'Serena Gilmore', email: 'serena@finlog.com', password: '123456' },
]

function Login({ goToRegister, onLogin }) {
  const [userInput, setUserInput] = useState('')
  const [password,  setPassword]  = useState('')
  const [error,     setError]     = useState('')
  const [userErr,   setUserErr]   = useState(false)
  const [passErr,   setPassErr]   = useState(false)

  function handleLogin() {
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

    const found = USERS.find(x =>
      (x.email === u || x.name === u || x.email.split('@')[0] === u) &&
      x.password === p
    )

    if (!found) {
      setError('Usuário ou senha inválidos.')
      setUserErr(true)
      setPassErr(true)
      return
    }

    onLogin(found)
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

        <div className="demo-hint">
          Conta demo: <b>serena@finlog.com</b> · senha: <b>123456</b>
        </div>
      </div>
    </div>
  )
}

export default Login
