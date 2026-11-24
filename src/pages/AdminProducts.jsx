import React, { useEffect, useState } from "react";
import { API_URL } from "../services/auth.js";

const empty = { id: "", nombre: "", tipo: "", atributo: "", descripcion: "", precio: 0, imagen: "" };
export function AdminProducts({ session }) {
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(empty);
  const [editing, setEditing] = useState(false);
  const [status, setStatus] = useState("");
  async function call(path, options = {}) {
    const response = await fetch(`${API_URL}${path}`, { ...options, headers: { "Content-Type": "application/json", Authorization: `Bearer ${session.token}`, ...options.headers } });
    if (!response.ok) { const body = await response.json().catch(() => ({})); throw new Error(body.message || body.error || "La operación no se pudo completar."); }
    return response.status === 204 ? null : response.json();
  }
  async function refresh() { setProducts(await call("/products")); }
  useEffect(() => { refresh().catch((error) => setStatus(error.message)); }, []);
  function edit(product) { setForm(product); setEditing(true); window.scrollTo(0, 0); }
  async function submit(event) {
    event.preventDefault(); setStatus("");
    try {
      await call(editing ? `/admin/products/${form.id}` : "/admin/products", { method: editing ? "PUT" : "POST", body: JSON.stringify({ ...form, precio: Number(form.precio) }) });
      setForm(empty); setEditing(false); await refresh(); setStatus("Producto guardado.");
    } catch (error) { setStatus(error.message); }
  }
  async function remove(id) {
    if (!window.confirm(`¿Eliminar ${id} del catálogo?`)) return;
    try { await call(`/admin/products/${id}`, { method: "DELETE" }); await refresh(); setStatus("Producto eliminado."); }
    catch (error) { setStatus(error.message); }
  }
  return <section className="container py-5">
    <p className="eyebrow">PANEL ADMINISTRADOR</p><h1>Gestionar catálogo</h1>
    <p className="text-secondary">Solo el rol ADMIN puede crear, editar y eliminar productos.</p>
    {status && <p className="alert alert-info" role="status">{status}</p>}
    <form className="card card-body mb-4" onSubmit={submit}>
      <h2 className="h4">{editing ? `Editar ${form.nombre}` : "Crear producto"}</h2>
      <div className="row g-3">
        {[["id","Id"],["nombre","Nombre"],["tipo","Tipo"],["atributo","Atributo"],["descripcion","Descripción"],["precio","Precio CLP"],["imagen","URL de imagen"]].map(([key,label]) => <div className="col-md-6" key={key}>
          <label className="form-label" htmlFor={`product-${key}`}>{label}</label>
          <input id={`product-${key}`} className="form-control" required type={key === "precio" ? "number" : "text"} min={key === "precio" ? "0" : undefined} value={form[key] ?? ""} disabled={key === "id" && editing} onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
        </div>)}
      </div>
      <div className="d-flex gap-2 mt-3"><button className="btn btn-brand">{editing ? "Guardar cambios" : "Crear"}</button>{editing && <button type="button" className="btn btn-outline-secondary" onClick={() => { setForm(empty); setEditing(false); }}>Cancelar</button>}</div>
    </form>
    <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Producto</th><th>Tipo</th><th>Precio</th><th>Acciones</th></tr></thead><tbody>{products.map((p) => <tr key={p.id}><td>{p.nombre}<small className="d-block text-secondary">{p.id}</small></td><td>{p.tipo}</td><td>{p.precio.toLocaleString("es-CL")} CLP</td><td><button className="btn btn-sm btn-outline-dark me-2" onClick={() => edit(p)}>Editar</button><button className="btn btn-sm btn-outline-danger" onClick={() => remove(p.id)}>Eliminar</button></td></tr>)}</tbody></table></div>
  </section>;
}
