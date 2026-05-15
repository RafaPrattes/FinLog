import { useState } from 'react'

import Login    from './components/Login'
import Register from './components/Register'
import Dashboard from './components/Dashboard'

function App() {
  const [screen,      setScreen]      = useState('login')
  const [currentUser, setCurrentUser] = useState(null)

  function handleLogin(user) {
    setCurrentUser(user)
    setScreen('dashboard')
  }

  function handleLogout() {
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
