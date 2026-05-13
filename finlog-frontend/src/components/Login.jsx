import { useState } from 'react'

function Login({
  goToRegister,
  goToDashboard,
}) {

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  function handleLogin() {
    if (
      email === 'serena@finlog.com' &&
      password === '123456'
    ) {
      goToDashboard()
      return
    }

    alert('Usuário ou senha inválidos')
  }

  return (
    <div id="screen-login" className="screen active">
      <div className="auth-card">
        <div className="brand">FinLog</div>

        <div className="sub">Login</div>

        <div id="login-err" className="err-msg"></div>

        <input
          type="text"
          className="field"
          placeholder="Usuário"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          type="password"
          className="field"
          placeholder="Senha"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <div className="remember-row">
          <input
            type="checkbox"
            id="remember"
            defaultChecked
          />

          <label htmlFor="remember">
            Permanecer Conectado
          </label>
        </div>

        <button
          className="btn-main"
          onClick={handleLogin}
        >
          Login
        </button>

        <button
          className="link-btn"
          onClick={goToRegister}
        >
          Não tem cadastro? Clique aqui
        </button>

        <div className="demo-hint">
          Conta demo:
          <b> serena@finlog.com </b>
          · senha:
          <b> 123456</b>
        </div>
      </div>
    </div>
  )
}

export default Login