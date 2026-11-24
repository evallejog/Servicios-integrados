export default function Tabla({ columnas, filas, cargando, vacio = 'No hay registros todavía' }) {
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            {columnas.map((c) => (
              <th key={c.titulo} className={c.numerica ? 'num' : undefined}>{c.titulo}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.map((fila, i) => (
            <tr key={fila.id ?? fila.productoId ?? i}>
              {columnas.map((c) => (
                <td key={c.titulo} className={c.numerica ? 'num' : undefined}>{c.valor(fila)}</td>
              ))}
            </tr>
          ))}
          {!cargando && filas.length === 0 && (
            <tr><td colSpan={columnas.length} className="vacio">{vacio}</td></tr>
          )}
          {cargando && (
            <tr><td colSpan={columnas.length} className="vacio">Cargando...</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
