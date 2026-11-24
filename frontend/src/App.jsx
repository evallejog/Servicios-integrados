import { useState } from 'react';
import Clientes from './pages/Clientes.jsx';
import Facturas from './pages/Facturas.jsx';
import Inventario from './pages/Inventario.jsx';
import Productos from './pages/Productos.jsx';
import Ventas from './pages/Ventas.jsx';

const modulos = [
  { id: 'clientes', titulo: 'Clientes', pagina: Clientes },
  { id: 'productos', titulo: 'Productos', pagina: Productos },
  { id: 'inventario', titulo: 'Inventario', pagina: Inventario },
  { id: 'ventas', titulo: 'Ventas', pagina: Ventas },
  { id: 'facturas', titulo: 'Facturación', pagina: Facturas },
];

export default function App() {
  const [activo, setActivo] = useState('clientes');
  const Pagina = modulos.find((m) => m.id === activo).pagina;

  return (
    <div className="app">
      <header className="cabecera">
        <div className="marca">
          <div className="marca-logo" aria-hidden="true">SI</div>
          <div>
            <h1>Servicios Integrados S.A.S.</h1>
            <small>Gestión de clientes, inventario, ventas y facturación</small>
          </div>
        </div>
        <nav aria-label="Módulos">
          {modulos.map((m) => (
            <button
              key={m.id}
              className={m.id === activo ? 'activo' : undefined}
              aria-current={m.id === activo ? 'page' : undefined}
              onClick={() => setActivo(m.id)}
            >
              {m.titulo}
            </button>
          ))}
        </nav>
      </header>
      <main>
        <Pagina key={activo} />
      </main>
    </div>
  );
}
