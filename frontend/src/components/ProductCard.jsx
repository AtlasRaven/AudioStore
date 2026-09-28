import { Link } from 'react-router-dom';
import { addToCart } from '../utils/cart';

const currency = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 });
export function money(value) { return currency.format(Number(value || 0)); }

export default function ProductCard({ product }) {
  return <article className="product-card">
    <Link className="product-card__image" to={`/san-pham/${product.id}`} aria-label={`Xem ${product.name}`}>
      {product.imageUrl ? <img src={product.imageUrl} alt={product.name} loading="lazy" /> : <span>{product.categoryName}</span>}
    </Link>
    <div className="product-card__body">
      <p className="eyebrow">{product.brandName} · {product.categoryName}</p>
      <Link className="product-card__title" to={`/san-pham/${product.id}`}>{product.name}</Link>
      <p className="product-card__price">{money(product.salePrice)}</p>
      <div className="product-card__bottom">
        <span className="muted">{product.availableQuantity > 0 ? 'Còn hàng' : 'Tạm hết hàng'}</span>
        <button className="button button--small button--outline" type="button" disabled={!product.availableQuantity} onClick={() => addToCart(product, 1)}>Thêm vào giỏ</button>
      </div>
    </div>
  </article>;
}
