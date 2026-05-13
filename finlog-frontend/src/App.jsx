import { useState } from 'react'

import Login from './components/Login'
import Register from './components/Register'
import Dashboard from './components/Dashboard'

function App() {
  const [screen, setScreen] = useState('login')

  return (
    <>
      {screen === 'login' && (
        <Login
          goToRegister={() => setScreen('register')}
          goToDashboard={() => setScreen('dashboard')}
        />
      )}

      {screen === 'register' && (
        <Register
          goToLogin={() => setScreen('login')}
        />
      )}

      {screen === 'dashboard' && (
        <Dashboard />
      )}
    </>
  )
}

export default App