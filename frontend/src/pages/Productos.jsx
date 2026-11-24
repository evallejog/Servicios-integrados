import { useState } from 'react';
import { api, moneda } from '../api.js';
import Aviso from '../components/Aviso.jsx';
import Tabla from '../components/Tabla.jsx';
import { useCarga } from '../components/useCarga.js';

const vacio = { sku: '', nombre: '', descripcion: '', precio: '' };
const cargarProductos = () => api.get('/api/productos');

export default function Productos() {
  const { datos, cargando, error, recargar } = useCarga(cargarProductos);
  const [form, setForm] = useState(vacio);
  const [mensaje, setMensaje] = useState({ tipo: 'ok', texto: '' });

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value });

  async function guardar(e) {
    e.preventDefault();
    try {
      await api.post('/api/productos', { ...form, precio: Number(form.precio) });
      setForm(vacio);
      setMensaje({ tipo: 'ok', texto: 'Producto registrado correctamente' });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  async function eliminar(producto) {
    if (!window.confirm(`¿Eliminar ${producto.nombre}?`)) return;
    try {
      await api.del(`/api/productos/${producto.id}`);
      setMensaje({ tipo: 'ok', texto: 'Producto eliminado' });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section>
      <h2>Productos</h2>
      <form className="formulario" onSubmit={guardar}>
        <label>SKU<input required value={form.sku} onChange={cambiar('sku')} /></label>
        <label>Nombre<input required value={form.nombre} onChange={cambiar('nombre')} /></label>
        <label>Precio (sin IVA)
          <input required type="number" min="0.01" step="0.01" value={form.precio} onChange={cambiar('precio')} />
        </label>
        <label className="ancho">Descripción<input value={form.descripcion} onChange={cambiar('descripcion')} /></label>
        <button type="submit">Registrar producto</button>
      </form>
      <Aviso tipo={mensaje.tipo}>{mensaje.texto}</Aviso>
      <Aviso>{error}</Aviso>
      <Tabla
        cargando={cargando}
        filas={datos}
        columnas={[
          { titulo: 'ID', valor: (p) => p.id },
          { titulo: 'SKU', valor: (p) => p.sku },
          { titulo: 'Nombre', valor: (p) => p.nombre },
          { titulo: 'Precio', numerica: true, valor: (p) => moneda(p.precio) },
          { titulo: 'Estado', valor: (p) => (p.activo ? 'Activo' : 'Inactivo') },
          { titulo: '', valor: (p) => <button className="secundario" onClick={() => eliminar(p)}>Eliminar</button> },
        ]}
      />
    </section>
  );
}
