import { useCallback, useState } from 'react';
import { api, fecha } from '../api.js';
import Aviso from '../components/Aviso.jsx';
import Tabla from '../components/Tabla.jsx';
import { useCarga } from '../components/useCarga.js';

export default function Inventario() {
  const cargar = useCallback(async () => {
    const [existencias, productos] = await Promise.all([api.get('/api/inventario'), api.get('/api/productos')]);
    return { existencias, productos };
  }, []);
  const { datos, cargando, error, recargar } = useCarga(cargar);
  const productos = datos.productos ?? [];
  const existencias = datos.existencias ?? [];

  const [productoId, setProductoId] = useState('');
  const [cantidad, setCantidad] = useState('');
  const [referencia, setReferencia] = useState('');
  const [mensaje, setMensaje] = useState({ tipo: 'ok', texto: '' });

  const nombreDe = (id) => productos.find((p) => p.id === id)?.nombre ?? `Producto ${id}`;

  async function registrarEntrada(e) {
    e.preventDefault();
    try {
      await api.post(`/api/inventario/${productoId}/entrada`, {
        cantidad: Number(cantidad),
        referencia: referencia || undefined,
      });
      setCantidad('');
      setReferencia('');
      setMensaje({ tipo: 'ok', texto: 'Entrada registrada correctamente' });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section>
      <h2>Inventario</h2>
      <form className="formulario" onSubmit={registrarEntrada}>
        <label>Producto
          <select required value={productoId} onChange={(e) => setProductoId(e.target.value)}>
            <option value="">Seleccione...</option>
            {productos.map((p) => <option key={p.id} value={p.id}>{p.sku} - {p.nombre}</option>)}
          </select>
        </label>
        <label>Cantidad
          <input required type="number" min="1" value={cantidad} onChange={(e) => setCantidad(e.target.value)} />
        </label>
        <label>Referencia
          <input value={referencia} placeholder="Ej: COMPRA-001" onChange={(e) => setReferencia(e.target.value)} />
        </label>
        <button type="submit">Registrar entrada</button>
      </form>
      <Aviso tipo={mensaje.tipo}>{mensaje.texto}</Aviso>
      <Aviso>{error}</Aviso>
      <Tabla
        cargando={cargando}
        filas={existencias}
        vacio="Aún no hay existencias registradas"
        columnas={[
          { titulo: 'Producto', valor: (i) => nombreDe(i.productoId) },
          { titulo: 'Existencias', numerica: true, valor: (i) => i.cantidad },
          { titulo: 'Actualizado', valor: (i) => fecha(i.actualizadoEn) },
        ]}
      />
    </section>
  );
}
