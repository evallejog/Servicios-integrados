import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// En desarrollo, cada ruta /api/<modulo> se redirige al microservicio que corre en local.
const destino = (puerto) => ({ target: `http://localhost:${puerto}`, changeOrigin: true });

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/clientes': destino(8081),
      '/api/productos': destino(8082),
      '/api/inventario': destino(8083),
      '/api/ventas': destino(8084),
      '/api/facturas': destino(8085),
    },
  },
});
