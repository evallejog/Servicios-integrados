import { useCallback, useState } from 'react';
import { api, fecha, moneda } from '../api.js';
import Aviso from '../components/Aviso.jsx';
import Tabla from '../components/Tabla.jsx';
import { useCarga } from '../components/useCarga.js';

export default function Facturas() {
  const cargar = useCallback(async () => {
    const [facturas, ventas] = await Promise.all([api.get('/api/facturas'), api.get('/api/ventas')]);
    return { facturas, ventas };
  }, []);
  const { datos, cargando, error, recargar } = useCarga(cargar);
  const facturas = datos.facturas ?? [];
  const pendientes = (datos.ventas ?? []).filter((v) => v.estado === 'REGISTRADA');

  const [ventaId, setVentaId] = useState('');
  const [mensaje, setMensaje] = useState({ tipo: 'ok', texto: '' });

  async function generar(e) {
    e.preventDefault();
    try {
      const factura = await api.post('/api/facturas', { ventaId: Number(ventaId) });
      setVentaId('');
      setMensaje({ tipo: 'ok', texto: `Factura ${factura.numero} emitida por ${moneda(factura.total)}` });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section>
      <h2>Facturación</h2>
      <form className="formulario" onSubmit={generar}>
        <label className="ancho">Venta pendiente de facturar
          <select required value={ventaId} onChange={(e) => setVentaId(e.target.value)}>
            <option value="">Seleccione...</option>
            {pendientes.map((v) => (
              <option key={v.id} value={v.id}>{v.referencia} - {v.clienteNombre} - {moneda(v.total)}</option>
            ))}
          </select>
        </label>
        <button type="submit">Generar factura</button>
      </form>
      <Aviso tipo={mensaje.tipo}>{mensaje.texto}</Aviso>
      <Aviso>{error}</Aviso>
      <Tabla
        cargando={cargando}
        filas={facturas}
        columnas={[
          { titulo: 'Número', valor: (f) => f.numero },
          { titulo: 'Cliente', valor: (f) => f.clienteNombre },
          { titulo: 'Emitida', valor: (f) => fecha(f.fechaEmision) },
          { titulo: 'Subtotal', numerica: true, valor: (f) => moneda(f.subtotal) },
          { titulo: 'IVA', numerica: true, valor: (f) => moneda(f.iva) },
          { titulo: 'Total', numerica: true, valor: (f) => moneda(f.total) },
        ]}
      />
    </section>
  );
}
