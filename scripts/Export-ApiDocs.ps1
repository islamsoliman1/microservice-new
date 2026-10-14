param(
    [string]$GatewayUrl = 'http://localhost:18089'
)

$ErrorActionPreference = 'Stop'
$GatewayUrl = $GatewayUrl.TrimEnd('/')
$repoRoot = Split-Path -Parent $PSScriptRoot
$output = Join-Path $repoRoot 'docs-site'
$assets = Join-Path $output 'assets'

$spec = Invoke-RestMethod -Uri "$GatewayUrl/openapi/all" -TimeoutSec 60
if ($spec.openapi -notlike '3.*' -or -not $spec.paths.'/api/product' -or -not $spec.paths.'/api/order' -or -not $spec.paths.'/api/inventory') {
    throw 'The gateway did not return the complete combined API definition.'
}

$spec.info.title = 'Microservices E-Commerce API Reference'
$spec.info.description = @'
Static API reference for a Java microservices portfolio project. Backend execution
is disabled on this hosted page. Request schemas and response examples describe
the API implemented in the source repository.

### Architecture

| Service | Responsibility |
| --- | --- |
| API Gateway | Routes requests and validates Keycloak JWTs and roles |
| Product | MongoDB product catalog |
| Order | SQL Server order persistence, inventory lookup, Kafka publication |
| Inventory | Internal SQL Server SKU stock checks |
| Notification | Kafka listener that logs notifications |
| Eureka | Service registration and discovery |
| Keycloak | Identity and access tokens |

Request flow: Client -> Gateway -> Product -> MongoDB.
Order flow: Client -> Gateway -> Order -> Inventory -> stock lookup.
Event flow: Order -> Kafka notificationTopic -> Notification.

Product reads are public through the gateway. Product creation requires ADMIN;
order placement requires USER or ADMIN. Inventory is internal.

### Current behavior

Orders use client-supplied prices and do not reserve stock. The fallback can
return HTTP 201 with a failure message. Database writes and Kafka publication
are not atomic. See the repository README for setup and known limitations.
'@
$spec.servers = @(@{
    url = 'https://backend.example.invalid'
    description = 'Reference only; no public backend is deployed'
})
$spec.components.securitySchemes.bearerAuth.description = 'JWT authorization required by protected gateway operations. This hosted reference does not accept credentials or execute requests.'

$null = New-Item -ItemType Directory -Path $assets -Force
$encoding = [Text.UTF8Encoding]::new($false)
[IO.File]::WriteAllText((Join-Path $output 'openapi.json'), ($spec | ConvertTo-Json -Depth 100), $encoding)

# Vendor the same Swagger UI assets used by the running Springdoc gateway.
foreach ($asset in @('swagger-ui.css', 'swagger-ui-bundle.js')) {
    Invoke-WebRequest -Uri "$GatewayUrl/swagger-ui/$asset" -UseBasicParsing -TimeoutSec 60 -OutFile (Join-Path $assets $asset)
}
Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/swagger-api/swagger-ui/v5.30.1/LICENSE' -UseBasicParsing -TimeoutSec 60 -OutFile (Join-Path $assets 'LICENSE.swagger-ui')
Invoke-WebRequest -Uri 'https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.30.1/swagger-ui-bundle.js.LICENSE.txt' -UseBasicParsing -TimeoutSec 60 -OutFile (Join-Path $assets 'swagger-ui-bundle.js.LICENSE.txt')
[IO.File]::WriteAllText((Join-Path $output '.nojekyll'), '', $encoding)
Write-Output "Exported API reference and Swagger UI assets to $output."
Write-Output 'Review and commit docs-site together with the Pages workflow. No tokens were exported.'
