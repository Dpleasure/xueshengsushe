export const API_BASE_URL = (() => {
  const configured = import.meta.env?.VITE_API_BASE_URL;
  if (configured) return configured.replace(/\/$/, '');

  if (typeof window !== 'undefined') {
    const { protocol, hostname } = window.location;
    if (hostname && hostname !== 'localhost' && hostname !== '127.0.0.1') {
      return `${protocol}//${hostname}:8080`;
    }
  }

  return 'http://localhost:8080';
})();

export const withApiBaseUrl = (path) => {
  if (!path) return API_BASE_URL;
  if (/^https?:\/\//i.test(path)) return path;
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
};
