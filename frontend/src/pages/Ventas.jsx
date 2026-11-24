import { useCallback, useState } from 'react';
import { api, fecha, moneda } from '../api.js';
import Aviso from '../components/Aviso.jsx';
import Tabla from '../components/Tabla.jsx';
import { useCarga } from '../components/useCarga.js';

export default function Ventas() {
  const cargar = useCallback(async () => {
    const [ventas, clientes, productos] = await Promise.all([
      api.get('/api/ventas'),
      api.get('/api/clientes'),
      api.get('/api/productos?soloActivos=true'),
    ]);
    return { ventas, clientes, productos };
  }, []);
  const { datos, cargando, error, recargar } = useCarga(cargar);
  const { ventas = [], clientes = [], productos = [] } = datos;

  const [clienteId, setClienteId] = useState('');
  const [lineas, setLineas] = useState([{ productoId: '', cantidad: 1 }]);
  const [mensaje, setMensaje] = useState({ tipo: 'ok', texto: '' });

  const actualizarLinea = (indice, cambios) =>
    setLineas(lineas.map((l, i) => (i === indice ? { ...l, ...cambios } : l)));

  async function registrar(e) {
    e.preventDefault();
    try {
      const venta = await api.post('/api/ventas', {
        clienteId: Number(clienteId),
        items: lineas.map((l) => ({ productoId: Number(l.productoId), cantidad: Number(l.cantidad) })),
      });
      setLineas([{ productoId: '', cantidad: 1 }]);
      setMensaje({ tipo: 'ok', texto: `Venta ${venta.referencia} registrada por ${moneda(venta.total)}` });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section>
      <h2>Ventas</h2>
      <form className="formulario" onSubmit={registrar}>
        <label className="ancho">Cliente
          <select required value={clienteId} onChange={(e) => setClienteId(e.target.value)}>
            <option value="">Seleccione...</option>
            {clientes.map((c) => <option key={c.id} value={c.id}>{c.nombre} ({c.documento})</option>)}
          </select>
        </label>
        {lineas.map((linea, i) => (
          <div className="linea" key={i}>
            <label>Producto
              <select required value={linea.productoId} onChange={(e) => actualizarLinea(i, { productoId: e.target.value })}>
                <option value="">Seleccione...</option>
                {productos.map((p) => <option key={p.id} value={p.id}>{p.nombre} - {moneda(p.precio)}</option>)}
              </select>
            </label>
            <label>Cantidad
              <input required type="number" min="1" value={linea.cantidad}
                     onChange={(e) => actualizarLinea(i, { cantidad: e.target.value })} />
            </label>
            {lineas.length > 1 && (
              <button type="button" className="secundario" onClick={() => setLineas(lineas.filter((_, j) => j !== i))}>
                Quitar
              </button>
            )}
          </div>
        ))}
        <div className="acciones">
          <button type="button" className="secundario" onClick={() => setLineas([...lineas, { productoId: '', cantidad: 1 }])}>
            Agregar producto
          </button>
          <button type="submit">Registrar venta</button>
        </div>
      </form>
      <Aviso tipo={mensaje.tipo}>{mensaje.texto}</Aviso>
      <Aviso>{error}</Aviso>
      <Tabla
        cargando={cargando}
        filas={ventas}
        columnas={[
          { titulo: 'Referencia', valor: (v) => v.referencia },
          { titulo: 'Cliente', valor: (v) => v.clienteNombre },
          { titulo: 'Productos', valor: (v) => v.items.map((i) => `${i.cantidad} x ${i.nombreProducto}`).join(', ') },
          { titulo: 'Fecha', valor: (v) => fecha(v.fecha) },
          { titulo: 'Estado', valor: (v) => v.estado },
          { titulo: 'Total', numerica: true, valor: (v) => moneda(v.total) },
        ]}
      />
    </section>
  );
}
