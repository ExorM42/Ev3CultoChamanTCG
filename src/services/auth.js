import { KEYS, read, save } from "./store.js";

export const API_URL = globalThis.CULTO_API_URL || "http://localhost:8081/api";
export function getSession() { return read(KEYS.session, null, sessionStorage); }
export function getUsers() { return []; }
async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options.headers },
  });
  const body = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || body?.error || "No se pudo conectar con el servidor.");
  return body;
}
function persistSession(data) {
  const session = { token: data.token, email: data.email, fullName: data.fullName, role: data.role };
  save(KEYS.session, session, sessionStorage);
  return session;
}
export async function registerUser(values) {
  return request("/auth/register", { method: "POST", body: JSON.stringify({
    fullName: values.fullName.trim(), email: values.email.trim().toLowerCase(),
    password: values.password, phone: values.phone.trim(), region: values.region,
    commune: values.commune,
  }) });
}
export async function loginUser(email, password) {
  return persistSession(await request("/auth/login", { method: "POST", body: JSON.stringify({ email: email.trim().toLowerCase(), password }) }));
}
export function logoutUser() { sessionStorage.removeItem(KEYS.session); }
export async function restoreSession(session) {
  if (!session?.token) return null;
  try {
    const user = await request("/auth/me", { headers: { Authorization: `Bearer ${session.token}` } });
    return { ...session, ...user };
  } catch { logoutUser(); return null; }
}
