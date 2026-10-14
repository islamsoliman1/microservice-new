# API Documentation and Local Demo

## Open Swagger

Run the project from its repository root:

```powershell
docker compose up -d --build
powershell -NoProfile -File scripts/Test-ApiDocs.ps1
```

Open http://localhost:18089/docs. Products, Orders, and Inventory (Internal)
appear together, with no service selector. The combined JSON is available at
`/openapi/all`. The gateway fetches the three generated service definitions
using Eureka and merges their paths and components. Schema names and operation
IDs have service prefixes so identical names cannot overwrite each other.
Security schemes are shared only when their definitions agree.
If any service definition cannot be loaded, the combined endpoint returns 503
instead of presenting an incomplete reference.

The original public definitions remain available for compatibility:

| Definition | JSON endpoint | Upstream |
| --- | --- | --- |
| Products | `/openapi/products` | `product-service /v3/api-docs` |
| Orders | `/openapi/orders` | `order-service /v3/api-docs` |

The OpenAPI server URL is `/`, so **Try it out** calls the same origin that
served the documentation. Gateway Swagger therefore calls the gateway,
not a Docker hostname or a directly exposed backend port. The current setup
assumes the gateway is hosted at the origin root, not under a URL prefix.
Download `/openapi/all` or import it into an API client to reuse the contract.

Inventory's schema and endpoint are included as an internal reference, with
interactive execution disabled by the documentation UI. The gateway also denies
`/api/inventory` requests even with a valid token. Local developers can use
http://localhost:8084/swagger-ui.html for direct inventory checks.
The page's overview describes Gateway, Eureka, Keycloak, Notification, data stores,
and the request/event flow; it does not invent HTTP endpoints for those components.

## Public Read Demo

1. Open the Products section.
2. Expand `GET /api/product`, select **Try it out**, then **Execute**.
3. Expect HTTP 200 and a JSON array (possibly empty).
4. Copy an actual returned `id` into `GET /api/product/{id}`.
5. An unknown ID returns 404.

This works without a token and does not modify data. New databases may have
no products; the project does not automatically seed a demo catalog.

## Authorize Protected Operations

The local realm is `spring-boot-microservice-realm`.
The password grant below is only for the existing local demo client.
A public deployment should use a dedicated Authorization Code + PKCE client.

```powershell
$token = Invoke-RestMethod -Method Post -Uri 'http://localhost:18085/realms/spring-boot-microservice-realm/protocol/openid-connect/token' -Body @{
    client_id = 'spring-boot-microservice-client'
    grant_type = 'password'
    username = 'admin'
    password = 'password'
}
$token.access_token | Set-Clipboard
```

In Swagger, select **Authorize**, paste the access token without `Bearer `,
then select **Authorize** and close the dialog. Do not paste tokens into source
files or commit them. Clear the clipboard after pasting with `Set-Clipboard -Value ''`.
Swagger authorization is not persisted across reloads. Reauthorize if the token
expires. A single bearer authorization applies to protected operations on this page.

| Local user | Password | Gateway permissions |
| --- | --- | --- |
| `user` | `password` | Product reads, order placement |
| `admin` | `password` | Product reads, product creation, order placement |

Gateway responses: 401 for a missing/invalid token, 403 for an insufficient role.
Direct service ports do not enforce these rules; use the gateway for the demo.

## Create a Product

This action writes to MongoDB. Authorize as admin and use:

```json
{
  "name": "Wireless Keyboard",
  "description": "Compact USB-C rechargeable keyboard",
  "price": 49.99
}
```

`POST /api/product` returns HTTP 201 with no body. Read the product list again
to find the new item and its generated ID. There is no delete endpoint, so
do not repeatedly execute this request unless extra demo records are intended.
Blank names/descriptions or nonpositive prices return 400.

## Order Request

Authorize as user or admin. The request field is **orderLineItemsDtoList**:

```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_15",
      "price": 999.99,
      "quantity": 1
    }
  ]
}
```

The SKU is illustrative, not guaranteed to be seeded. Check an existing SKU
through local inventory documentation first. Creating a product does not create
inventory or a SKU mapping. Do not rely on unknown-SKU rejection in this version.

Executing this request can create a SQL Server order and publish to
`notificationTopic`. It is not idempotent. Current responses are plain text:

| HTTP status | Body | Meaning |
| --- | --- | --- |
| 201 | `Order Placed Success` | Order service completed its request path; not proof of notification delivery |
| 201 | `Oops something went wrong. Try again later` | Current fallback failure; not a successful order |
| 400 | Validation error | Invalid request fields |

The existing fallback does not guarantee a 503 for unavailable inventory.
The stock lookup currently includes a deliberate delay. Prices are supplied
by the client and stock is not reserved. Treat this endpoint as an educational
demo until order correctness and resilience are repaired.

## Automated Documentation Check

```powershell
powershell -NoProfile -File scripts/Test-ApiDocs.ps1
```

Checks UI assets, the combined definition, resolved schema references, internal
inventory metadata, relative server URLs, operation security,
request examples, a public product read, 401/403 boundaries, and validation
with authenticated requests. It submits only invalid write payloads, so it
does not intentionally create products or orders. Demo credentials are used
only for local checks and tokens are never printed.

To check only public documentation against another root-hosted gateway:

```powershell
powershell -NoProfile -File scripts/Test-ApiDocs.ps1 -GatewayUrl https://api.example.com -SkipAuthChecks
```

This placeholder domain is not a deployed demo.

## Troubleshooting

- **401 loading Swagger:** rebuild/recreate the gateway with the updated security rules.
- **503 loading a definition:** wait for service registration in Eureka and check
  `docker compose logs --tail=100 api-gateway product-service order-service inventory-service`.
- **401 on a protected API:** obtain a fresh token from the issuer configured on
  the gateway; tokens from another realm or hostname will not match.
- **403 creating a product:** use the realm admin user, not the Keycloak
  administration account from the master realm.
- **An old UI after rebuilding:** hard-refresh the browser and inspect
  `/openapi/all`. Navigate to `/docs` to open the combined view.
- **Public hosting:** follow [hosting.md](hosting.md) first. Opening Swagger access
  does not make the surrounding development stack safe to expose.

