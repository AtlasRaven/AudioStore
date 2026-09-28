# AudioStore — thiết kế hệ thống

## Môi trường phát triển

- Frontend: Node.js 20.x, React 18 và Vite.
- Backend: Java 17, Spring Boot 3.3.5 và Maven 3.6.3 trở lên.
- Springdoc OpenAPI 2.6.0 được dùng cùng Spring Boot 3.x.

## 1. ERD

```mermaid
erDiagram
  USERS ||--o{ ADDRESSES : owns
  USERS ||--o{ ORDERS : places
  USERS ||--o{ ORDERS : sells
  USERS ||--o{ AUDIT_LOGS : acts
  CATEGORIES ||--o{ PRODUCTS : groups
  BRANDS ||--o{ PRODUCTS : makes
  PRODUCTS ||--|| INVENTORY : stocked
  PRODUCTS ||--o{ PRODUCT_IMAGES : has
  PRODUCTS ||--o{ ORDER_ITEMS : sold_as
  PRODUCTS ||--o{ PURCHASE_ITEMS : received_as
  PRODUCTS ||--o{ STOCK_MOVEMENTS : moves
  SUPPLIERS ||--o{ PURCHASES : supplies
  PURCHASES ||--o{ PURCHASE_ITEMS : contains
  ORDERS ||--o{ ORDER_ITEMS : contains
  ORDERS ||--o| SHIPMENTS : ships
  ORDERS ||--o| PAYMENTS : paid_by
  USERS ||--o{ REVIEWS : writes
  PRODUCTS ||--o{ REVIEWS : receives
  PROMOTIONS ||--o{ PROMOTION_PRODUCTS : targets
  PRODUCTS ||--o{ PROMOTION_PRODUCTS : discounted
  COUPONS ||--o{ ORDERS : applied_to
  EVENTS ||--o{ PROMOTIONS : groups
  BANNERS }o--|| USERS : managed_by
  AI_REVIEW_SUMMARIES }o--|| PRODUCTS : summarizes
  AI_LOGS }o--|| USERS : requested_by

  USERS { bigint id PK; varchar email UK; varchar password_hash; varchar full_name; varchar phone; varchar role; boolean enabled }
  ADDRESSES { bigint id PK; bigint user_id FK; varchar recipient; varchar phone; varchar province; varchar district; varchar detail; boolean default_address }
  CATEGORIES { bigint id PK; varchar name UK; varchar slug UK; boolean active }
  BRANDS { bigint id PK; varchar name UK; varchar slug UK; boolean active }
  PRODUCTS { bigint id PK; bigint category_id FK; bigint brand_id FK; varchar sku UK; varchar name; text description; json specifications; decimal sale_price; decimal cost_price; int warranty_months; boolean active }
  PRODUCT_IMAGES { bigint id PK; bigint product_id FK; varchar url; int position }
  INVENTORY { bigint product_id PK_FK; int quantity; int minimum_quantity; bigint version }
  SUPPLIERS { bigint id PK; varchar name; varchar phone; varchar email }
  PURCHASES { bigint id PK; bigint supplier_id FK; bigint created_by FK; varchar status; decimal total; datetime received_at }
  PURCHASE_ITEMS { bigint id PK; bigint purchase_id FK; bigint product_id FK; int quantity; decimal unit_cost }
  STOCK_MOVEMENTS { bigint id PK; bigint product_id FK; bigint actor_id FK; varchar type; int quantity_delta; varchar reason; bigint reference_id; datetime created_at }
  ORDERS { bigint id PK; bigint user_id FK; bigint sales_owner_id FK; bigint address_id FK; bigint coupon_id FK; varchar code UK; varchar status; varchar payment_method; decimal subtotal; decimal discount; decimal total }
  ORDER_ITEMS { bigint id PK; bigint order_id FK; bigint product_id FK; varchar product_name; int quantity; decimal unit_price; decimal cost_price }
  PAYMENTS { bigint id PK; bigint order_id FK; varchar status; varchar method; decimal amount; datetime confirmed_at; bigint confirmed_by FK }
  SHIPMENTS { bigint id PK; bigint order_id FK; varchar status; datetime shipped_at; varchar note }
  COUPONS { bigint id PK; varchar code UK; varchar discount_type; decimal discount_value; decimal minimum_order; datetime starts_at; datetime ends_at; int usage_limit; int used_count; boolean active }
  EVENTS { bigint id PK; varchar name; datetime starts_at; datetime ends_at; boolean active }
  PROMOTIONS { bigint id PK; bigint event_id FK; varchar name; decimal discount_percent; datetime starts_at; datetime ends_at; boolean active }
  PROMOTION_PRODUCTS { bigint promotion_id PK_FK; bigint product_id PK_FK }
  REVIEWS { bigint id PK; bigint product_id FK; bigint user_id FK; bigint order_id FK; int rating; text content; boolean visible; datetime created_at }
  BANNERS { bigint id PK; varchar title; varchar image_url; varchar target_url; int position; boolean active }
  AUDIT_LOGS { bigint id PK; bigint actor_id FK; varchar action; varchar entity_type; bigint entity_id; json details; datetime created_at }
  AI_REVIEW_SUMMARIES { bigint product_id PK_FK; text summary; bigint review_count; datetime updated_at }
  AI_LOGS { bigint id PK; bigint user_id FK; varchar feature; varchar status; int duration_ms; datetime created_at }
```

**Danh sách bảng:** `users`, `addresses`, `categories`, `brands`, `products`, `product_images`, `inventory`, `suppliers`, `purchases`, `purchase_items`, `stock_movements`, `orders`, `order_items`, `payments`, `shipments`, `coupons`, `events`, `promotions`, `promotion_products`, `reviews`, `banners`, `audit_logs`, `ai_review_summaries`, `ai_logs`. Giỏ hàng lưu ở trình duyệt; đơn hàng là bản chụp giá/sản phẩm tại thời điểm đặt.

## 2. REST API

Tất cả response có dạng `{success,message,data}`. `PUBLIC` nghĩa là chưa cần đăng nhập. Các role viết tắt: `A` ADMIN, `K` STAFF_KHO, `B` STAFF_BAN_HANG, `T` KE_TOAN, `M` MARKETING_CSKH, `C` CUSTOMER.

| Module | Method, URL | Quyền | Request → data response |
|---|---|---|---|
| Auth | POST `/api/auth/register` | PUBLIC | `{email,password,fullName,phone}` → user |
| Auth | POST `/api/auth/login` | PUBLIC | `{email,password}` → `{token,user}` |
| Auth | GET `/api/auth/me` | đăng nhập | — → user |
| Auth | POST `/api/auth/change-password` | đăng nhập | `{oldPassword,newPassword}` → `{changed}` |
| User | GET `/api/admin/users` | A | `?page,size` → page users |
| User | PATCH `/api/admin/users/{id}` | A | `{role,enabled}` → user |
| Product | GET `/api/products` | PUBLIC | `q,category,brand,minPrice,maxPrice,sort,page,size` → page |
| Product | GET `/api/products/{id}` | PUBLIC | — → product + tồn khả dụng |
| Product | POST `/api/admin/products` | A | product → product |
| Product | PUT `/api/admin/products/{id}` | A,B giá; M nội dung | product fields theo quyền → product |
| Product | DELETE `/api/admin/products/{id}` | A | — → `{deleted}` |
| Catalog | GET `/api/categories`, `/api/brands` | PUBLIC | — → list |
| Catalog | POST/PUT/DELETE `/api/admin/categories/{id}`, `/api/admin/brands/{id}` | A | catalog fields → item |
| Inventory | GET `/api/admin/inventory` | A,K,B,T | `q,lowStock` → list |
| Inventory | POST `/api/admin/inventory/{productId}/adjust` | A,K | `{delta,reason}` → inventory |
| Inventory | GET `/api/admin/stock-movements` | A,K | — → movement history |
| Purchase | POST `/api/admin/purchases` | A,K | `{supplierId,items:[{productId,quantity,unitCost}]}` → receipt |
| Purchase | POST `/api/admin/purchases/{id}/receive` | A,K | — → receipt; cộng tồn trong transaction |
| Purchase | GET `/api/admin/purchases` | A,K,T | `status,page,size` → page |
| Supplier | GET/POST `/api/admin/suppliers` | GET A,K,T; POST A,K | supplier → supplier |
| Stock | POST `/api/admin/stock-issues` | A,K | `{productId,quantity,reason}` → movement |
| Address | GET/POST `/api/addresses` | C | address fields → address |
| Orders | POST `/api/orders` | C | `{addressId,paymentMethod,couponCode,items}` → order; giá và tồn kho được đọc lại từ DB |
| Orders | GET `/api/orders/mine` | C | — → orders |
| Orders | GET `/api/admin/orders` | A,K,B,T,M | `status,page,size` → page |
| Orders | PATCH `/api/admin/orders/{id}/status` | A,B,K | `{status}` → order; DANG_GIAO tạo phiếu xuất và trừ hàng đã giữ |
| Payment | POST `/api/admin/orders/{id}/confirm-payment` | A,T | — → payment; chuyển khoản thành công đưa đơn về CHO_XAC_NHAN |
| Orders | POST `/api/orders/{id}/cancel` | C (chưa xác nhận), A | `{reason}` → order |
| Coupon | GET `/api/promotions/active` | PUBLIC | — → promotions |
| Promotions | GET `/api/admin/promotions`, POST/PUT/DELETE `/api/admin/promotions/{id}` | A,M | chương trình theo sản phẩm/danh mục → promotion |
| Coupons | GET `/api/admin/coupons`, POST/PUT/DELETE `/api/admin/coupons/{id}` | A,M | mã giảm giá → coupon |
| Events | GET `/api/admin/events`, POST/PUT `/api/admin/events/{id}` | A,M | sự kiện → event |
| Coupon | POST `/api/coupons/validate` | C | `{code,items}` → discount |
| Reports | GET `/api/admin/reports/revenue` | A,T,B (chỉ doanh số mình) | `from,to,groupBy` → series/totals |
| Reports | GET `/api/admin/reports/by-products`, `/by-categories`, `/top-products` | A,T,B (chỉ doanh số mình) | `from,to,limit` → danh sách |
| Reports | GET `/api/admin/reports/export` | A,T | `from,to` → XLSX |
| Content | GET `/api/banners` | PUBLIC | — → active banners |
| Content | POST/PUT/DELETE `/api/admin/banners/{id}` | A,M | banner → banner |
| Reviews | POST `/api/products/{id}/reviews` | C (đã mua, hoàn tất) | `{rating,content}` → review |
| Reviews | GET `/api/products/{id}/reviews` | PUBLIC | — → approved reviews |
| Reviews | GET `/api/admin/reviews` | A,M | `visible,page,size` → page |
| Reviews | PATCH `/api/admin/reviews/{id}` | A,M | `{visible}` → review |
| Audit | GET `/api/admin/audit-logs` | A | `from,to,actor,page,size` → page |
| AI | POST `/api/ai/chat` | PUBLIC, giới hạn tần suất | `{need,budget,room,music}` → answer + catalog products |
| AI | GET `/api/ai/related/{productId}` | PUBLIC | — → actual products |
| AI | POST `/api/admin/ai/descriptions` | A,M | `{name,specifications}` → draft |
| AI | GET `/api/ai/reviews/{productId}/summary` | PUBLIC | — → cached summary |
| AI | GET `/api/admin/ai/logs` | A,M | `page,size` → page |

Chi tiết field mở rộng được mô tả bằng OpenAPI ở các DTO/controller; phân quyền backend áp dụng `@PreAuthorize`, frontend chỉ ẩn menu và chặn route để cải thiện UX.

## 3. Design tokens và cấu trúc

`frontend/src/styles/tokens.css` là nguồn màu, spacing và radius duy nhất. Font dùng system sans-serif; khoảng cách dùng 4/8/16/24/32/48/64px.

```text
AudioStore/
├── .vscode/extensions.json
├── docs/architecture.md
├── backend/ (Spring Boot: config, auth, catalog, inventory, orders, promotions, reports, ai, common)
└── frontend/ (Vite: api, auth, components, customer, admin, styles)
```
