import { useState } from 'react';
import { api, fecha } from '../api.js';
import Aviso from '../components/Aviso.jsx';
import Tabla from '../components/Tabla.jsx';
import { useCarga } from '../components/useCarga.js';

const vacio = { nombre: '', documento: '', email: '', telefono: '', direccion: '' };
const cargarClientes = () => api.get('/api/clientes');

export default function Clientes() {
  const { datos, cargando, error, recargar } = useCarga(cargarClientes);
  const [form, setForm] = useState(vacio);
  const [mensaje, setMensaje] = useState({ tipo: 'ok', texto: '' });

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value });

  async function guardar(e) {
    e.preventDefault();
    try {
      await api.post('/api/clientes', form);
      setForm(vacio);
      setMensaje({ tipo: 'ok', texto: 'Cliente registrado correctamente' });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  async function eliminar(cliente) {
    if (!window.confirm(`¿Eliminar a ${cliente.nombre}?`)) return;
    try {
      await api.del(`/api/clientes/${cliente.id}`);
      setMensaje({ tipo: 'ok', texto: 'Cliente eliminado' });
      recargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <section>
      <h2>Clientes</h2>
      <form className="formulario" onSubmit={guardar}>
        <label>Nombre<input required value={form.nombre} onChange={cambiar('nombre')} /></label>
        <label>Documento<input required value={form.documento} onChange={cambiar('documento')} /></label>
        <label>Correo<input required type="email" value={form.email} onChange={cambiar('email')} /></label>
        <label>Teléfono<input value={form.telefono} onChange={cambiar('telefono')} /></label>
        <label className="ancho">Dirección<input value={form.direccion} onChange={cambiar('direccion')} /></label>
        <button type="submit">Registrar cliente</button>
      </form>
      <Aviso tipo={mensaje.tipo}>{mensaje.texto}</Aviso>
      <Aviso>{error}</Aviso>
      <Tabla
        cargando={cargando}
        filas={datos}
        columnas={[
          { titulo: 'ID', valor: (c) => c.id },
          { titulo: 'Nombre', valor: (c) => c.nombre },
          { titulo: 'Documento', valor: (c) => c.documento },
          { titulo: 'Correo', valor: (c) => c.email },
          { titulo: 'Teléfono', valor: (c) => c.telefono },
          { titulo: 'Registrado', valor: (c) => fecha(c.creadoEn) },
          { titulo: '', valor: (c) => <button className="secundario" onClick={() => eliminar(c)}>Eliminar</button> },
        ]}
      />
    </section>
  );
}
