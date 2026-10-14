A containerized Java backend with a MongoDB catalog, SQL Server orders and
inventory, Kafka integration, and Keycloak JWT authorization.

### Services

| Service | Responsibility |
| --- | --- |
| API Gateway | Routes product/order requests and verifies Keycloak tokens and realm roles |
| Product | Creates and reads the MongoDB catalog |
| Order | Checks inventory, saves SQL Server orders, and publishes to notificationTopic |
| Inventory | Internal SKU stock lookup backed by SQL Server; reference only on this page |
| Notification | Kafka listener that logs order notifications; no business HTTP API |
| Eureka | Service registration and discovery for routing and internal calls |
| Keycloak | Issues access tokens with USER and ADMIN realm roles |

### Request and event flow

Client -> Gateway -> Product -> MongoDB

Client -> Gateway -> Order -> Inventory -> SQL Server stock lookup

Order -> SQL Server order persistence -> Kafka notificationTopic -> Notification listener

Services register with Eureka. The gateway verifies tokens using Keycloak signing keys.
Zipkin, Prometheus, and Grafana are included for observability; some scraping and
dashboard provisioning still need repair.

### Access and examples

Product reads are public. Product creation requires ADMIN. Order placement
requires USER or ADMIN. Use Authorize with a Keycloak access token, without
the Bearer prefix. In the local realm, user/password has USER and admin/password
has USER and ADMIN. These credentials are for local development only.
Swagger does not persist tokens across reloads.

Example product: Wireless Keyboard, Compact USB-C rechargeable keyboard, price 49.99.
An order uses orderLineItemsDtoList with skuCode, price, and quantity.
The iphone_15 SKU shown in the schema is illustrative; check existing inventory
before placing an order. Creating a catalog product does not seed inventory.

### Current demo limitations

Inventory is documented for reference and is not routed through the gateway.
Its interactive execution is disabled on this page. Local developers can use
the service-local Swagger page at http://localhost:8084/swagger-ui.html.

Order prices are client-supplied; stock is not reserved and unknown-SKU handling
needs repair. The fallback can return HTTP 201 with a failure message, so inspect
the response body. Database writes and Kafka publication are not atomic, and
notification delivery needs further verification.

The local Compose configuration publishes development service ports, which
bypass gateway authorization. See the repository's docs/api-demo.md and
docs/hosting.md for setup and deployment guidance.
