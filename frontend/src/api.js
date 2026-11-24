// Cliente HTTP mínimo. Todas las rutas son relativas: en producción las atiende el gateway nginx
// y en desarrollo el proxy de Vite.

async function solicitar(ruta, opciones = {}) {
  const respuesta = await fetch(ruta, {
    headers: { 'Content-Type': 'application/json' },
    ...opciones,
  });

  if (respuesta.status === 204) return null;

  const texto = await respuesta.text();
  const datos = texto ? JSON.parse(texto) : null;

  if (!respuesta.ok) {
    const detalle = datos?.errores ? Object.values(datos.errores).join('. ') : datos?.detail;
    throw new Error(detalle || `Error ${respuesta.status}`);
  }
  return datos;
}

const enviar = (metodo) => (ruta, cuerpo) =>
  solicitar(ruta, { method: metodo, body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo) });

export const api = {
  get: (ruta) => solicitar(ruta),
  post: enviar('POST'),
  put: enviar('PUT'),
  del: (ruta) => solicitar(ruta, { method: 'DELETE' }),
};

export const moneda = (valor) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 }).format(valor ?? 0);

export const fecha = (iso) =>
  iso ? new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(iso)) : '';
