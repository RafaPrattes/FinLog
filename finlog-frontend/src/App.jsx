import { useCallback, useEffect, useState } from 'react'
import api, { setOnUnauthorized } from './service/api'
import { lerToken, limparToken, salvarToken } from './service/session'

import Login from './components/Login'
import Register from './components/Register'
import Dashboard from './components/Dashboard'

function App() {
  const [screen, setScreen] = useState(() => (lerToken() ? 'loading' : 'login'))
  const [currentUser, setCurrentUser] = useState(null)
  const [aviso, setAviso] = useState('')

  function handleLogin(response, lembrar = true) {
    const { usuario, token } = response

    salvarToken(token, lembrar)

    setAviso('')
    setCurrentUser(usuario)
    setScreen('dashboard')
  }

  const handleLogout = useCallback((mensagem = '') => {
    limparToken()
    setCurrentUser(null)
    setAviso(mensagem)
    setScreen('login')
  }, [])

  /* Saída pedida pela pessoa: invalida o token no backend antes de descartá-lo */
  async function handleUserLogout() {
    try {
      await api.post('/auth/logout')
    } catch {
      /* Mesmo que a API falhe, a sessão local é encerrada */
    }
    handleLogout()
  }

  /* Sessão expirada ou token inválido: volta para o login */
  useEffect(() => {
    setOnUnauthorized(() => handleLogout('Sua sessão expirou. Entre novamente.'))
    return () => setOnUnauthorized(null)
  }, [handleLogout])

  /* Ao recarregar a página, restaura a sessão a partir do token salvo */
  useEffect(() => {
    if (screen !== 'loading') return
    api.get('/usuarios/me')
      .then(({ data }) => {
        setCurrentUser(data)
        setScreen('dashboard')
      })
      .catch(() => handleLogout())
  }, [screen, handleLogout])

  return (
    <>
      {screen === 'loading' && (
        <div className="screen active" />
      )}

      {screen === 'login' && (
        <Login
          aviso={aviso}
          goToRegister={() => setScreen('register')}
          onLogin={handleLogin}
        />
      )}

      {screen === 'register' && (
        <Register
          goToLogin={() => setScreen('login')}
        />
      )}

      {screen === 'dashboard' && (
        <Dashboard
          user={currentUser}
          onLogout={handleUserLogout}
        />
      )}
    </>
  )
}

export default App
