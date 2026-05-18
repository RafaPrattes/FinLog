import { useState } from 'react'
import api from './service/api'

import Login from './components/Login'
import Register from './components/Register'
import Dashboard from './components/Dashboard'

function App() {
  const [screen, setScreen] = useState('login')
  const [currentUser, setCurrentUser] = useState(null)

  function handleLogin(response) {
    const { usuario, token } = response

    localStorage.setItem('token', token)
    api.defaults.headers.common.Authorization = `Bearer ${token}`

    setCurrentUser(usuario)
    setScreen('dashboard')
  }

  function handleLogout() {
    localStorage.removeItem('token')
    delete api.defaults.headers.common.Authorization
    setCurrentUser(null)
    setScreen('login')
  }

  return (
    <>
      {screen === 'login' && (
        <Login
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
          onLogout={handleLogout}
        />
      )}
    </>
  )
}

export default App