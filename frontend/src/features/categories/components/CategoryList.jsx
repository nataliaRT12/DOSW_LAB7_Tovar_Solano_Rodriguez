const COLOR_HEX = { BLUE: '#2255cc', GREEN: '#227744', ORANGE: '#cc6622', PURPLE: '#772299' }

export default function CategoryList({ categories }) {
  return (
    <div style={{ border: '2px solid #000', padding: '10px', marginBottom: '10px', backgroundColor: '#fff' }}>
      <h3>Categorias existentes</h3>
      {categories.length === 0 && <p>No hay categorias todavia</p>}
      {categories.map((category) => (
        <div key={category.id} style={{ border: '1px solid #999', padding: '6px', marginBottom: '4px' }}>
          <span style={{ width: '16px', height: '16px', display: 'inline-block', marginRight: '8px', backgroundColor: COLOR_HEX[category.color] }} />
          {category.name}
        </div>
      ))}
    </div>
  )
}
