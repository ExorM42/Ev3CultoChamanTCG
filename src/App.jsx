import React, { useEffect, useState } from "react";
import {
  HashRouter,
  Routes,
  Route,
  NavLink,
  Link,
  useLocation,
} from "react-router-dom";
import { Catalog } from "./pages/Catalog.jsx";
import { Cart } from "./pages/Cart.jsx";
import { Register } from "./pages/Register.jsx";
import { Login } from "./pages/Login.jsx";
import { AdminProducts } from "./pages/AdminProducts.jsx";
import { getSession, logoutUser, restoreSession } from "./services/auth.js";
import {
  KEYS,
  read,
  save,
  normalizeCart,
  changeCart,
} from "./services/store.js";
function Header({ count, session, onLogout }) {
  const [expanded, setExpanded] = useState(false);
  const location = useLocation();
  React.useEffect(() => {
    setExpanded(false);
    window.scrollTo(0, 0);
  }, [location.pathname]);
  return (
    <>
      <a className="skip-link" href="#main">
        Saltar al contenido
      </a>
      <div className="announcement text-center py-2">
        El punto de encuentro para tu colección Pokémon TCG
      </div>
      <header className="border-bottom bg-white">
        <nav
          className="navbar navbar-expand-lg container py-3"
          aria-label="Navegación principal"
        >
          <Link className="navbar-brand d-flex align-items-center gap-2" to="/">
            <span className="brand-mark" aria-hidden="true">
              ✦
            </span>
            <span className="brand-name">
              CULTO CHAMÁN<small>TCG STORE</small>
            </span>
          </Link>
          <button
            className="navbar-toggler"
            type="button"
            aria-controls="navigation"
            aria-expanded={expanded}
            aria-label="Abrir menú"
            onClick={() => setExpanded(!expanded)}
          >
            <span className="navbar-toggler-icon" />
          </button>
          <div
            className={`collapse navbar-collapse ${expanded ? "show" : ""}`}
            id="navigation"
          >
            <div className="navbar-nav mx-auto gap-lg-3">
              <NavLink className="nav-link" to="/" end>
                Inicio
              </NavLink>
              <NavLink className="nav-link" to="/registro">
                Crear cuenta
              </NavLink>
            </div>
            <div className="d-flex flex-wrap align-items-center gap-3 py-2 py-lg-0">
              {session ? (
                <>
                  <span className="small">Hola, {session.fullName}</span>
                  {session.role === "ADMIN" && <NavLink className="nav-link" to="/admin">Administrar</NavLink>}
                  <button
                    className="btn btn-sm btn-outline-dark"
                    onClick={onLogout}
                  >
                    Cerrar sesión
                  </button>
                </>
              ) : (
                <NavLink className="nav-link" to="/login">
                  Iniciar sesión
                </NavLink>
              )}
              <NavLink
                className="btn btn-dark"
                to="/carrito"
                aria-label={`Carrito, ${count} unidades`}
              >
                Carrito <span className="cart-count">{count}</span>
              </NavLink>
            </div>
          </div>
        </nav>
      </header>
    </>
  );
}
export function StoreApp() {
  const [cart, setCart] = useState(() => normalizeCart(read(KEYS.cart, [])));
  const [session, setSession] = useState(() => getSession());
  useEffect(() => { let active = true; restoreSession(getSession()).then((value) => { if (active) setSession(value); }); return () => { active = false; }; }, []);
  const [notice, setNotice] = useState("");
  function update(id, delta) {
    try {
      const next = changeCart(cart, id, delta);
      save(KEYS.cart, next);
      setCart(next);
    } catch {
      setNotice(
        "No se pudo guardar el carrito. Revisa el almacenamiento de tu navegador.",
      );
    }
  }
  function add(id) {
    try {
      const next = changeCart(cart, id, 1);
      save(KEYS.cart, next);
      setCart(next);
      setNotice("Carta agregada. Tu carrito está actualizado.");
    } catch {
      setNotice(
        "No se pudo guardar el carrito. Revisa el almacenamiento de tu navegador.",
      );
    }
  }
  function checkout() {
    save(KEYS.cart, []);
    setCart([]);
  }
  function logout() {
    logoutUser();
    setSession(null);
    setNotice("Cerraste tu sesión.");
  }
  return (
    <>
      <Header
        count={cart.reduce((sum, item) => sum + item.cantidad, 0)}
        session={session}
        onLogout={logout}
      />
      <main id="main">
        <Routes>
          <Route path="/" element={<Catalog onAdd={add} token={session?.token} />} />
          <Route path="/registro" element={<Register />} />
          <Route path="/login" element={<Login onSession={setSession} />} />
          <Route
            path="/carrito"
            element={
              <Cart cart={cart} onChange={update} onCheckout={checkout} />
            }
          />
          <Route path="/admin" element={session?.role === "ADMIN" ? <AdminProducts session={session} /> : <Login onSession={setSession} />} />
          <Route
            path="*"
            element={
              <div className="container py-5">
                <h1>Página no encontrada</h1>
                <Link to="/">Volver al catálogo</Link>
              </div>
            }
          />
        </Routes>
      </main>
      <footer className="site-footer py-5">
        <div className="container">
          <div className="row g-4">
            <div className="col-md-6">
              <h2 className="h5">
                CULTO CHAMÁN <span className="text-secondary">/ TCG STORE</span>
              </h2>
              <p className="text-secondary">
                Para quienes siempre tienen una carta más por descubrir.
              </p>
            </div>
            <div className="col-6 col-md-3">
              <h3 className="h6">Explora</h3>
              <Link to="/">Catálogo Pokémon</Link>
              <br />
              <Link to="/carrito">Mi carrito</Link>
            </div>
            <div className="col-6 col-md-3">
              <h3 className="h6">Tu comunidad</h3>
              <Link to="/registro">Crear una cuenta</Link>
              <br />
              <Link to="/login">Iniciar sesión</Link>
            </div>
          </div>
          <div className="border-top mt-4 pt-3 small text-secondary">
            Proyecto académico · Compra de demostración · Sin afiliación con
            Pokémon.
          </div>
        </div>
      </footer>
      <div className="notice" role="status" aria-live="polite">
        {notice && (
          <div className="d-flex align-items-center gap-3">
            {notice}
            <button aria-label="Cerrar aviso" onClick={() => setNotice("")}>
              ×
            </button>
          </div>
        )}
      </div>
    </>
  );
}
export function App() {
  return (
    <HashRouter>
      <StoreApp />
    </HashRouter>
  );
}
