param(
    [string]$GatewayUrl = 'http://localhost:18089',
    [string]$KeycloakUrl = 'http://localhost:18085',
    [int]$TimeoutSeconds = 180,
    [switch]$SkipAuthChecks
)

$ErrorActionPreference = 'Stop'
$GatewayUrl = $GatewayUrl.TrimEnd('/')
$KeycloakUrl = $KeycloakUrl.TrimEnd('/')

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Wait-ForJson {
    param([string]$Path)
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        try { return Invoke-RestMethod -Uri "$GatewayUrl$Path" -TimeoutSec 15 }
        catch {
            if ([DateTime]::UtcNow -ge $deadline) { throw }
            Start-Sleep -Seconds 2
        }
    } while ($true)
}

function Assert-Status {
    param([string]$Path, [string]$Method, [int[]]$Expected, [string]$Token = '')
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $request = @{
        Uri = "$GatewayUrl$Path"
        Method = $Method
        Headers = $headers
        TimeoutSec = 20
        UseBasicParsing = $true
    }
    # Every write probe is invalid and should be rejected before persistence.
    if ($Method -eq 'Post') {
        $request.Body = '{}'
        $request.ContentType = 'application/json'
    }
    try { $status = [int](Invoke-WebRequest @request).StatusCode }
    catch {
        if (-not $_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
    }
    Assert-True ($Expected -contains $status) "$Method $Path expected $Expected, received $status."
}

$config = Wait-ForJson '/v3/api-docs/swagger-config'
Assert-True ($config.url -eq '/openapi/all') 'Expected one combined API definition.'
Assert-True ($null -eq $config.urls -or @($config.urls).Count -eq 0) 'Service selector must be removed.'
$ui = Invoke-WebRequest -Uri "$GatewayUrl/docs" -UseBasicParsing -TimeoutSec 20
Assert-True ($ui.StatusCode -eq 200 -and $ui.Content -match 'swagger-ui') 'Swagger UI did not load.'
foreach ($asset in @('/swagger-ui/swagger-ui.css', '/swagger-ui/swagger-ui-bundle.js', '/documentation/swagger.js')) {
    $assetUri = "$GatewayUrl$asset"
    $assetResponse = Invoke-WebRequest -Uri $assetUri -UseBasicParsing -TimeoutSec 20
    Assert-True ($assetResponse.StatusCode -eq 200) "Swagger asset unavailable: $asset"
}
Write-Output 'PASS: Combined Swagger page and assets are public; no service selector configured.'

$spec = Wait-ForJson '/openapi/all'
Assert-True ($spec.openapi -like '3.*') 'Missing OpenAPI version.'
Assert-True (@($spec.servers).Count -eq 1 -and $spec.servers[0].url -eq '/') 'Try it out must use the current origin.'
Assert-True (@($spec.tags).Count -eq 3) 'Expected three API sections.'
foreach ($tag in @('Products', 'Orders', 'Inventory (Internal)')) {
    Assert-True ($spec.tags.name -contains $tag) "Missing section: $tag"
}
Assert-True (@($spec.paths.PSObject.Properties).Count -eq 4) 'Expected four paths across all services.'
Assert-True ($spec.components.securitySchemes.bearerAuth.scheme -eq 'bearer') 'Missing bearer token scheme.'
Assert-True (@($spec.components.securitySchemes.PSObject.Properties).Count -eq 1) 'Expected one shared authorization scheme.'
Assert-True ($spec.paths.'/api/product'.get.operationId -eq 'product_listProducts') 'Missing product list operation.'
Assert-True ($null -eq $spec.paths.'/api/product'.get.security -or @($spec.paths.'/api/product'.get.security).Count -eq 0) 'Product reads should be public.'
Assert-True ($null -eq $spec.security) 'Combined API must not require global authorization.'
Assert-True ($spec.paths.'/api/product'.post.security[0].PSObject.Properties.Name -contains 'bearerAuth') 'Product writes must document authentication.'
Assert-True ($spec.paths.'/api/order'.post.security[0].PSObject.Properties.Name -contains 'bearerAuth') 'Orders must document authentication.'
Assert-True ($spec.components.schemas.product_ProductRequest.properties.name.example -eq 'Wireless Keyboard') 'Missing product example.'
Assert-True ($spec.components.schemas.order_OrderRequest.properties.PSObject.Properties.Name -contains 'orderLineItemsDtoList') 'Incorrect order request field.'
Assert-True ($spec.components.schemas.order_OrderLineItemDto.properties.PSObject.Properties.Name -notcontains 'id') 'Ignored order item ID should not be advertised.'
Assert-True ($null -ne $spec.paths.'/api/order'.post.responses.'201'.content.'text/plain'.examples.Fallback) 'Order fallback limitation must be documented.'
Assert-True ($spec.paths.'/api/inventory'.get.'x-internal' -eq $true) 'Inventory must be marked internal.'
Assert-True ($spec.paths.'/api/inventory'.get.operationId -eq 'inventory_checkStock') 'Missing inventory stock operation.'
foreach ($service in @('API Gateway', 'Notification', 'Eureka', 'Keycloak', 'notificationTopic')) {
    Assert-True ($spec.info.description.Contains($service)) "Missing service overview detail: $service"
}

function Assert-References {
    param($Node)
    if ($Node -is [System.Array]) {
        foreach ($child in $Node) { Assert-References $child }
    } elseif ($Node -is [PSCustomObject]) {
        $reference = $Node.'$ref'
        if ($reference -and $reference.StartsWith('#/')) {
            $target = $spec
            foreach ($segment in $reference.Substring(2).Split('/')) {
                $name = $segment.Replace('~1', '/').Replace('~0', '~')
                $property = $target.PSObject.Properties[$name]
                Assert-True ($null -ne $property) "Unresolved schema reference: $reference"
                $target = $property.Value
            }
        }
        foreach ($property in $Node.PSObject.Properties) { Assert-References $property.Value }
    }
}
Assert-References $spec
Write-Output 'PASS: All three sections, examples, service overview, shared authorization, and schema references.'

Assert-Status '/api/product' 'Get' 200
# A hosted read-only proxy may reject requests before they reach Spring Security.
$writeDenied = if ($SkipAuthChecks) { @(401, 403, 405) } else { @(401) }
$internalDenied = if ($SkipAuthChecks) { @(401, 403, 404) } else { @(401) }
Assert-Status '/api/product' 'Post' $writeDenied
Assert-Status '/api/order' 'Post' $writeDenied
Assert-Status '/actuator/metrics' 'Get' $internalDenied
Assert-Status '/openapi/inventory' 'Get' $internalDenied
Assert-Status '/api/inventory?skuCode=iphone_15' 'Get' $writeDenied
Write-Output 'PASS: Public reads work; anonymous writes and non-public paths remain protected.'

if (-not $SkipAuthChecks) {
    $tokens = @{}
    foreach ($username in @('user', 'admin')) {
        $response = Invoke-RestMethod -Method Post -TimeoutSec 20 -Uri "$KeycloakUrl/realms/spring-boot-microservice-realm/protocol/openid-connect/token" -Body @{
            client_id = 'spring-boot-microservice-client'
            grant_type = 'password'
            username = $username
            password = 'password'
        }
        Assert-True (-not [string]::IsNullOrWhiteSpace($response.access_token)) "No token for $username."
        $tokens[$username] = $response.access_token
    }
    Assert-Status '/api/product' 'Post' 403 $tokens.user
    Assert-Status '/api/product' 'Post' 400 $tokens.admin
    Assert-Status '/api/order' 'Post' 400 $tokens.user
    Assert-Status '/api/inventory?skuCode=iphone_15' 'Get' 403 $tokens.admin
    Write-Output 'PASS: USER cannot create products; authenticated invalid writes return 400.'
}

Write-Output 'Documentation smoke check passed. No valid write requests were submitted.'

