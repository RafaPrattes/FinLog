import { useState } from 'react'

import Login    from './components/Login'
import Register from './components/Register'
import Dashboard from './components/Dashboard'

function App() {

  /*Essas duas linhas comentadas são para o fluxo normal do app, onde o usuário começa na tela de login
  eu só as tirei para poder testar a dashboard sem precisar passar pelo Login toda hora*/
  /*const [screen,      setScreen]      = useState('login')
  const [currentUser, setCurrentUser] = useState(null)*/

  /*Essas linhas abaixo é para ir direto para a dashboard*/
  const [screen,      setScreen]      = useState('dashboard')
  const [currentUser, setCurrentUser] = useState({ id: 1, nome: 'Teste Usuario' })

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
