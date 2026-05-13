import { useState } from 'react'

function Register({ goToLogin }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [confirmEmail, setConfirmEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  function handleRegister() {
    console.log({
      name,
      email,
      confirmEmail,
      password,
      confirmPassword,
    })
  }

  return (
    <div id="screen-register" className="screen active">
      <div className="auth-card">
        <div className="brand">FinLog</div>

        <div className="sub">Cadastro</div>

        <div className="err-msg"></div>

        <input
          type="text"
          className="field"
          placeholder="Nome completo"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <input
          type="email"
          className="field"
          placeholder="Cadastre um e-mail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          type="email"
          className="field"
          placeholder="Confirme o e-mail"
          value={confirmEmail}
          onChange={(e) => setConfirmEmail(e.target.value)}
        />

        <input
          type="password"
          className="field"
          placeholder="Cadastre uma senha"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <input
          type="password"
          className="field"
          placeholder="Confirme a senha"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />

        <button
          className="btn-main"
          onClick={handleRegister}
        >
          Cadastrar
        </button>

        <button
          className="link-btn"
          onClick={goToLogin}
        >
          Retornar para tela de login
        </button>
      </div>
    </div>
  )
}

export default Register