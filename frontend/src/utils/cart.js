const key = 'audiostore.cart';
export function getCart() { try { return JSON.parse(localStorage.getItem(key) || '[]'); } catch { return []; } }
export function saveCart(items) { localStorage.setItem(key, JSON.stringify(items)); window.dispatchEvent(new Event('audiostore:cart')); }
export function addToCart(product, quantity) {
  const cart = getCart(); const line = cart.find((item) => item.productId === product.id);
  if (line) line.quantity += quantity; else cart.push({ productId: product.id, quantity, name: product.name, price: product.salePrice, imageUrl: product.imageUrl, brandName: product.brandName });
  saveCart(cart);
}
