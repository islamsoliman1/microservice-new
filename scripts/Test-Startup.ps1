param(
    [string]$GatewayUrl = 'http://localhost:18089',
    [string]$KeycloakUrl = 'http://localhost:18085',
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
$realm = 'spring-boot-microservice-realm'
$issuer = "$KeycloakUrl/realms/$realm"

function Wait-ForJson {
    param([string]$Uri)
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        try { return Invoke-RestMethod -Uri $Uri -TimeoutSec 10 }
        catch {
            if ([DateTime]::UtcNow -ge $deadline) { throw }
            Start-Sleep -Seconds 2
        }
    } while ($true)
}

$discovery = Wait-ForJson "$issuer/.well-known/openid-configuration"
if ($discovery.issuer -ne $issuer) { throw 'Unexpected Keycloak issuer.' }
Write-Output 'PASS: Keycloak realm imported and discoverable.'

$adminToken = $null
foreach ($username in @('user', 'admin')) {
    $token = Invoke-RestMethod -Method Post -Uri "$issuer/protocol/openid-connect/token" -TimeoutSec 20 -Body @{
        client_id = 'spring-boot-microservice-client'
        username = $username
        password = 'password'
        grant_type = 'password'
    }
    if (-not $token.access_token) { throw "No access token returned for $username." }
    $payload = $token.access_token.Split('.')[1].Replace('-', '+').Replace('_', '/')
    $payload = $payload.PadRight($payload.Length + (4 - $payload.Length % 4) % 4, '=')
    $claims = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload)) | ConvertFrom-Json
    if ($claims.realm_access.roles -notcontains 'USER') { throw "Missing USER role for $username." }
    if ($username -eq 'admin') {
        if ($claims.realm_access.roles -notcontains 'ADMIN') { throw 'Missing ADMIN role.' }
        $adminToken = $token.access_token
    }
    Write-Output "PASS: Token issued with expected roles for $username."
}

$null = Wait-ForJson "$GatewayUrl/actuator/health"
$null = Invoke-RestMethod -Uri "$GatewayUrl/actuator/metrics" -TimeoutSec 20 -Headers @{
    Authorization = "Bearer $adminToken"
}
Write-Output 'PASS: Gateway accepts the Keycloak token.'

$products = @(Wait-ForJson "$GatewayUrl/api/product")
Write-Output "PASS: Gateway product route reads MongoDB ($($products.Count) products)."
