# Harmoni POS Order Service

Order, payment and reporting service for the Harmoni POS platform. It confirms
sales, records payments, snapshots the customer on each order, and produces the
settlement, sales, daily, top-product and order-volume reports.

The service owns the order database (`db-harmoni-pos-transaction`) but not the
catalogue. Product names, SKU prices, customization prices and promotion
discounts are resolved by the **menu service**; the order service stores what
the menu service priced. Caller identity and permissions arrive from the
**gateway** as headers.

## Tech stack

| Concern | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 (Web MVC on Jetty, Security, Validation) |
| Persistence | MyBatis 3.5.9 + `mybatis-spring-boot-starter` 3.0.4, PageHelper |
| Database | MySQL (`db-harmoni-pos-transaction`) |
| HTTP client | Spring WebFlux `WebClient` |
| Tokens | JJWT 0.12.5 |
| API docs | springdoc-openapi 2.5.0 (Swagger UI) |
| Encryption | `mybatis-encrypt-spring-boot-starter` (AES) |
| Build | Maven (`./mvnw` or `mvn`), GraalVM native plugin |

## Architecture

```
com.harmoni.pos.order
├── http
│   ├── controller        # OrderController, OrderPaymentController,
│   │                     # OrderCustomerController, ReportController
│   ├── handler           # @RestControllerAdvice exception handlers
│   └── response          # RestAPIResponse
├── business.service
│   ├── OrderBusinessService(Impl)        # confirm, print, void, list/page
│   ├── OrderPaymentBusinessService(Impl) # pay an order
│   ├── ReportBusinessService(Impl)       # reports
│   ├── menu              # MenuService, PromotionMenuService, UserService
│   ├── promotion         # OrderPromotionService(Impl)
│   └── rest              # RestClientService (WebClient wrapper)
├── service               # per-table services (Order, OrderDetail, ...)
├── mapper                # MyBatis mapper interfaces
├── model                 # entities, enums, report models, DTOs
├── component             # SecurityConfig, HeaderAuthoritiesFilter,
│                         # CallerScope* (report store scope)
├── config                # DbTransactionSQLConfig, MenuProperties, OpenApiConfig
├── exception             # business exceptions
└── utils                 # DateTimeUtils
```

`OrderApplication` excludes Spring Boot's default security auto-configuration;
`component/SecurityConfig` provides the filter chain used by the service.

## Data model

All tables live in `db-harmoni-pos-transaction`.

| Table | Purpose |
| --- | --- |
| `orders` | Order header: totals, store, customer, table, status, `idempotency_key` (unique) |
| `order_details` | One line per product, with product/category names snapshotted |
| `order_detail_skus` | SKU lines under a detail, with quantity, price, amount, `discount_amount` |
| `order_detail_customizations` | Customization groups chosen on a line |
| `order_detail_customization_options` | Selected options, quantity, price, amount |
| `order_item_discounts` | Promotion audit rows per SKU line (promotion code/name snapshotted) |
| `order_payments` | Payment rows for an order |
| `order_customers` | Customer snapshot per order (name/phone at sale time) |

`orders.status` is an enum: `PENDING`, `CONFIRMED`, `PAID`, `VOID`.

The `sql/` directory holds hand-applied migrations (`001`–`012`). There is no
migration tool; run them in order against `db-harmoni-pos-transaction`.

## Order lifecycle

```
PENDING ──confirm──▶ CONFIRMED ──pay──▶ PAID
   │                     │
   └──────────void───────┴──▶ VOID
```

- **Confirm** (`POST /api/v1/order`) prices and persists a new order.
- **Pay** (`PUT /api/v1/order`) marks the order `PAID` and writes `order_payments`.
- **Void** (`PUT /api/v1/order/{orderId}/void`) marks the order `VOID`.
- **Print** (`POST /api/v1/order/print`) calculates the order without persisting it.

## Confirmation flow

1. `OrderBusinessServiceImpl.calculateWithPromotions` resolves the operator's
   store from the menu service (`UserService`).
2. `OrderDetailService.populateOrderDetail` asks the menu service to validate the
   basket and return the catalogue names and prices
   (`POST /internal/v1/menu/validate-order-items`).
3. `OrderPromotionService` asks the menu service to price the basket against the
   live promotion catalogue (`POST /api/v1/promotion/price`). A failure here
   fails the sale rather than silently charging full price.
4. Totals are computed: subtotal, discount, tax (currently zero), service charge
   (currently zero), grand total.
5. `OrderService.insertWithIdempotency` persists the order and its lines in one
   transaction. A repeated `Idempotency-Key` returns the existing order; the
   unique key on `orders.idempotency_key` is the final guard under concurrency.
6. Promotion audit rows are written to `order_item_discounts` after the insert,
   because they need the generated SKU line ids.
7. A customer snapshot is written to `order_customers` for non-walk-in sales.

A walk-in sale is recorded with `orders.customer_id = -1`; `order_customers`
uses `NULL` for the same case.

## External dependencies

| Service | Used for |
| --- | --- |
| Menu service (`menu.url`) | Basket validation, promotion pricing, user/store lookup, report store-scope expansion |
| Gateway | JWT validation and `X-Username` / `X-Roles` / `X-Permissions` / `X-Scopes` headers |

`RestClientService` forwards the caller's `X-Username`, `X-Roles`,
`X-Permissions` and `X-Scopes` on internal calls, so the menu service sees the
same operator and scope.

## Security

- `HeaderAuthoritiesFilter` builds the `SecurityContext` from the gateway headers
  (`X-Username`, `X-Roles`, `X-Permissions`).
- Controllers use `@PreAuthorize` with the platform permissions
  (`order:view`, `order:approve`, `report:view`, `rbac:manage`, ...).
- `CallerScopeFilter` runs only for `/api/v1/reports/**`. It expands the caller's
  `X-Scopes` into store ids via the menu service's internal scope endpoint, so a
  brand/chain-scoped user can report across their stores. A missing header keeps
  the legacy unrestricted behaviour; an expansion failure falls back to the
  `STORE` assignments in the header rather than widening access.

## API reference

All endpoints require the gateway identity headers. `X-Username` is mandatory on
every order/report endpoint.

### Orders

| Method | Path | Authority |
| --- | --- | --- |
| `POST` | `/api/v1/order` | `order:view`, `terminal:view`, `availability:view`, `rbac:manage` |
| `POST` | `/api/v1/order/print` | same |
| `GET` | `/api/v1/order` | same — today's orders for the operator's store |
| `GET` | `/api/v1/order/page?limit=&beforeId=` | same — keyset page, newest first |
| `PUT` | `/api/v1/order/{orderId}/void` | `order:approve`, `rbac:manage` |

`POST /api/v1/order` accepts an optional `Idempotency-Key` header. The page
endpoint returns `{ orders, nextCursor, hasMore }`; pass `nextCursor` back as
`beforeId` to append the next page.

### Payments

| Method | Path | Authority |
| --- | --- | --- |
| `PUT` | `/api/v1/order` | `order:view`, `terminal:view`, `availability:view`, `rbac:manage` |

### Customer snapshots

| Method | Path | Authority |
| --- | --- | --- |
| `GET` | `/api/v1/order-customers/order/{orderId}` | `order:view`, `customer:read`, `terminal:view`, `availability:view`, `rbac:manage` |
| `GET` | `/api/v1/order-customers/customer/{customerId}/history?from=&to=` | same |

`from`/`to` are optional ISO-8601 date-times. There is no write endpoint:
snapshots are taken during confirmation.

### Reports

| Method | Path | Authority |
| --- | --- | --- |
| `GET` | `/api/v1/reports/settlement?start=&end=&storeIds=` | `report:view`, `report:read`, `rbac:manage` |
| `GET` | `/api/v1/reports/top-products?start=&end=&storeIds=&limit=` | same |
| `GET` | `/api/v1/reports/daily?start=&end=&storeIds=` | same |
| `GET` | `/api/v1/reports/sales?start=&end=&storeIds=` | same |
| `GET` | `/api/v1/reports/order-volume?start=&end=&storeIds=` | same |

`start`/`end` are ISO-8601 date-times. `storeIds` is optional; when omitted the
operator's store is used, and the result is always intersected with the caller's
report scope.

Responses use `RestAPIResponse` (`timeStamp`, `httpStatus`, `data`, `error`).
Errors are mapped by `http/handler`: `400` for validation/business input and
unreadable bodies, `204` for no-content results, and `500` for unhandled
exceptions.

## Configuration

Key properties in `src/main/resources/application.properties`:

```properties
server.port=8083
server.address=127.0.0.1

spring.datasource.transaction.mysql.jdbc-url=jdbc:mysql://localhost:3306/db-harmoni-pos-transaction
spring.datasource.transaction.mysql.username=pos-user
spring.datasource.transaction.mysql.password=forlearn27!

menu.url=http://localhost:8082
menu.url.validate-order-items=${menu.url}/internal/v1/menu/validate-order-items
menu.url.promotion-price=${menu.url}/api/v1/promotion/price
menu.url.scope-stores=${menu.url}/internal/v1/menu/scope/stores
menu.url.user=${menu.url}/api/v1/user
menu.url.store=${menu.url}/api/v1/store

harmoni.order.jwt.secret=<base64 HMAC key>
harmoni.order.jwt.expired.time=86400000
```

Swagger UI is at `/swagger-ui.html`; the OpenAPI document is at `/v3/api-docs`.
The checked-in credentials and secrets are development defaults; override them
per environment.

## Running locally

```bash
# 1. Create db-harmoni-pos-transaction and apply sql/001..012 in order
# 2. Build
./mvnw -DskipTests package

# 3. Run
java -jar target/order-0.0.1-SNAPSHOT.jar
# or
./mvnw spring-boot:run
```

The service listens on `127.0.0.1:8083`. In the full stack it is reached through
the gateway on port `8080`; the dashboard and other services call the gateway,
not this service directly.

## Testing

```bash
./mvnw test
```

Tests cover the order/payment/report business services, the menu and promotion
clients, the controllers, the per-table services and order serialization.

## Notes

- **Pricing is authoritative in the menu service.** The order service never
  computes a discount itself; it records what the promotion engine returned.
- **Idempotency** is enforced by `orders.idempotency_key` (unique). A replayed
  confirmation returns the existing order and does not re-record promotions or
  the customer snapshot.
- **Snapshots, not joins.** Product/category/SKU/customization names and prices,
  promotion code/name, and customer name/phone are copied onto the order so a
  historic receipt stays readable after the catalogue or customer changes.
- **Report scope** is resolved per request by `CallerScopeFilter`; only report
  paths pay for the menu-service expansion.
