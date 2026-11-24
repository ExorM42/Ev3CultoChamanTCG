import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getProducts, setProducts } from "../services/store.js";
import { API_URL } from "../services/auth.js";
import { ProductCard } from "../components/ProductCard.jsx";
export function Catalog({ onAdd, token }) {
  const [query, setQuery] = useState("");
  const [type, setType] = useState("");
  const [products, setProductList] = useState(getProducts());
  const [catalogError, setCatalogError] = useState("");
  useEffect(() => {
    let active = true;
    fetch(`${API_URL}/products`, { headers: token ? { Authorization: `Bearer ${token}` } : {} })
      .then((response) => { if (!response.ok) throw new Error("API no disponible"); return response.json(); })
      .then((items) => { if (active) { setProducts(items); setProductList(items); } })
      .catch(() => { if (active) setCatalogError("No se pudo cargar el catálogo del servidor; se muestran los datos de demostración."); });
    return () => { active = false; };
  }, [token]);
  const visible = products.filter(
    (p) =>
      p.nombre.toLowerCase().includes(query.toLowerCase()) &&
      (!type || p.tipo === type),
  );
  return (
    <>
      <section className="hero container py-5">
        <div className="row align-items-center g-4">
          <div className="col-md-7">
            <p className="eyebrow">PARA TU PRÓXIMA GRAN JUGADA</p>
            <h1>
              Tu colección.
              <br />
              <span>Tu próximo nivel.</span>
            </h1>
            <p className="lead text-secondary my-4">
              Encuentra esa carta que falta en tu mazo. Explora nuestra
              selección Pokémon TCG y lleva tu pasión a la mesa.
            </p>
            <a className="btn btn-brand btn-lg" href="#catalogo">
              Explorar cartas ↗
            </a>
            <p className="small text-secondary mt-4">
              Cartas individuales · Precios en pesos chilenos
            </p>
          </div>
          <div className="col-md-5">
            <div className="hero-art" aria-label="Colección de cartas TCG">
              <span className="orbit orbit-one" />
              <span className="orbit orbit-two" />
              <div className="collectible">
                <span>POKÉMON TCG</span>
                <div className="energy">✦</div>
                <strong>
                  La próxima carta
                  <br />
                  es tuya.
                </strong>
                <small>CULTO CHAMÁN · COLECCIONISTAS</small>
              </div>
              <span className="hero-stamp">
                JUEGA.
                <br />
                COLECCIONA.
              </span>
            </div>
          </div>
        </div>
      </section>
      <section id="catalogo" className="container py-5">
        <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
          <div>
            <p className="eyebrow">ELIGE TU SIGUIENTE CARTA</p>
            <h2>Catálogo destacado</h2>
            <p className="text-secondary mb-0">
              {visible.length} cartas para descubrir
            </p>
          </div>
          <div className="d-flex flex-wrap gap-2 filters">
            <div>
              <label className="visually-hidden" htmlFor="search">
                Buscar cartas
              </label>
              <input
                className="form-control"
                id="search"
                type="search"
                placeholder="Buscar una carta…"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>
            <div>
              <label className="visually-hidden" htmlFor="type">
                Filtrar por tipo
              </label>
              <select
                className="form-select"
                id="type"
                value={type}
                onChange={(e) => setType(e.target.value)}
              >
                <option value="">Todos los tipos</option>
                {[...new Set(products.map((p) => p.tipo))].map((t) => (
                  <option key={t}>{t}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
        {catalogError && <p className="alert alert-warning" role="status">{catalogError}</p>}
        <div className="row g-4">
          {visible.map((product) => (
            <div className="col-12 col-sm-6 col-lg-3" key={product.id}>
              <ProductCard product={product} onAdd={onAdd} />
            </div>
          ))}
        </div>
        {!visible.length && (
          <p className="empty-state" role="status">
            No encontramos cartas con esos filtros. Prueba otra búsqueda.
          </p>
        )}
      </section>
      <section className="container pb-5">
        <div className="community p-4 p-md-5 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div>
            <p className="eyebrow">EL SIGUIENTE PASO DE TU COLECCIÓN</p>
            <h2>Únete al culto de las cartas.</h2>
            <p className="mb-0 text-secondary">
              Crea tu cuenta y prepárate para tu próxima partida.
            </p>
          </div>
          <Link to="/registro" className="btn btn-dark">
            Crear mi cuenta ↗
          </Link>
        </div>
      </section>
    </>
  );
}
