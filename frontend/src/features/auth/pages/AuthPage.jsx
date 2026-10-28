import { useState } from 'react'
import LoginForm from '../components/LoginForm'
import RegisterForm from '../components/RegisterForm'

export default function AuthPage() {
  const [mode, setMode] = useState('login')

  return (
    <main className="tasks-page">
      <h1>ToDo Tasks</h1>
      {mode === 'login' ? (
        <LoginForm onSwitchToRegister={() => setMode('register')} />
      ) : (
        <RegisterForm onSwitchToLogin={() => setMode('login')} />
      )}
    </main>
  )
}
