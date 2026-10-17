import { useState } from 'react'

const COLOR_OPTIONS = ['BLUE', 'GREEN', 'ORANGE', 'PURPLE']
const COLOR_HEX = { BLUE: '#2255cc', GREEN: '#227744', ORANGE: '#cc6622', PURPLE: '#772299' }

export default function CategoryForm({ onSubmit }) {
  const [name, setName] = useState('')
  const [color, setColor] = useState('BLUE')
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    if (!name.trim()) {
      setError('El nombre es obligatorio')
      return
    }
    setError('')
    await onSubmit({ name: name.trim(), color })
    setName('')
  }

  return (
    <form style={{ border: '2px solid #000', padding: '10px', marginBottom: '10px', backgroundColor: '#f0f0f0' }} onSubmit={handleSubmit}>
      <h3>Nueva categoria</h3>
      <label htmlFor="category-name">Nombre</label>
      <br />
      <input id="category-name" type="text" value={name} maxLength={40} onChange={(e) => setName(e.target.value)} />
      <br />
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <p>Color</p>
      {COLOR_OPTIONS.map((option) => (
        <span
          key={option}
          style={{
            width: '30px', height: '30px', display: 'inline-block', marginRight: '6px', cursor: 'pointer',
            backgroundColor: COLOR_HEX[option],
            border: color === option ? '3px solid #000' : '1px solid #999',
          }}
          onClick={() => setColor(option)}
        />
      ))}
      <br />
      <button type="submit">Guardar categoria</button>
    </form>
  )
}
