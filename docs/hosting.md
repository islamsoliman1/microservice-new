# Hosting the Portfolio Demo

## Recommendation

For the full current stack, start with a **16 GB RAM x86-64 Linux VPS**,
Docker Compose, and an HTTPS reverse proxy. Hetzner Cloud is my first option
to evaluate for a low-traffic portfolio demo, provided account availability
and the final checkout price fit your budget.

On 2026-10-14, this project's 13 running containers used approximately **8 GiB**
in a local `docker stats --no-stream` snapshot. This is an observation, not a
peak-memory benchmark. Allow headroom for startup, the OS, builds, and traffic.
Build images in CI or sequentially to avoid build-time memory spikes.
Retest under load and set measured resource limits before choosing a smaller VM.

Choose Intel/AMD x86-64, not ARM: Microsoft's SQL Server container documentation
supports Linux x86-64 hosts, not emulation.
[Microsoft container requirements](https://learn.microsoft.com/en-us/sql/linux/install-upgrade/quickstart-install-docker?view=sql-server-ver17).

## Options

| Option | Fit for this repository | Cost/effort |
| --- | --- | --- |
| Hetzner Cloud x86 VPS | Recommended starting point; keeps the Compose architecture | Fixed VM cost; you maintain Linux, TLS, backups, and updates |
| Railway containers | Managed deployment alternative, but services/volumes/networking need adaptation | Usage-based RAM, CPU, storage, and traffic; not a $5 full-stack demo |
| Documentation only | Publish exported OpenAPI on a static host and add a recorded walkthrough | No running databases/backend; label it API reference, not a live API |

Pricing reference checked 2026-10-14: Hetzner's published EU CX43 adjustment
lists EUR 15.99/month excluding IPv4. Verify current plan RAM, availability,
taxes, IPv4, backups, and your regional checkout total before purchasing.
[Hetzner pricing reference](https://docs.hetzner.com/general/infrastructure-and-availability/price-adjustment/),
[current plans](https://www.hetzner.com/cloud/).

Railway's container pricing documentation lists RAM at $10/GB/month and CPU
at $20/vCPU/month, plus storage/egress. As an illustrative calculation, 8 GB
held all month is about $80 in RAM alone, before CPU and other usage.
The local measurement is not a prediction of Railway usage or a bill quote.
[Railway resource pricing](https://docs.railway.com/pricing/plans).

No host has been purchased, provisioned, or deployed by this documentation change.

## Public Demo Architecture

Target links after configuration:

- `https://api.your-domain.example/docs`: Swagger through the gateway.
- `https://api.your-domain.example/api/product`: public product read.
- `https://auth.your-domain.example`: public Keycloak login/issuer.
- Administration, metrics, databases, Kafka, Eureka, and individual Java services:
  private network or authenticated administrative access only.

These are placeholders. Use an actual domain you control.

## Required Before Going Public

The existing Compose file is a **local development configuration**, not a
production deployment template. Complete these items before exposing it:

- Remove published backend, SQL Server, Kafka, Eureka, monitoring, and Grafana
  ports. Bind gateway/Keycloak to loopback behind the proxy or put the proxy on
  their private Docker network. Verify port exposure externally; do not rely
  solely on host firewall assumptions.
- Expose only HTTPS and restricted administration. Do not publicly proxy the
  gateway's existing `/eureka/**` routes or unrestricted actuator endpoints.
- Replace database, Grafana, and Keycloak bootstrap passwords. Remove the public
  admin demo account; use dedicated least-privilege demo identities.
- Replace Keycloak `start-dev` with an appropriately configured production setup,
  persistent database, explicit HTTPS hostname, and restricted admin access.
  The current ephemeral realm import must not be used to store public users.
- Configure the gateway issuer to the public HTTPS realm URL, with a reachable
  JWKS URL. Both current gateway Docker properties and Compose environment values
  use localhost and must be updated together.
- For browser login, add a dedicated public Swagger client with Authorization
  Code + PKCE, exact redirect URIs/web origins, and no client secret in Swagger.
  The current documentation supports pasting bearer tokens; it does not yet
  implement this browser login flow.
- Keep the public CV demo read-only initially. A reverse-proxy policy allowing
  only GET/HEAD on the API host is one option, with a private operator path for
  seeding. Public order writes should wait for the known pricing, stock,
  fallback, and delivery issues to be fixed.
- Set request limits, rate limits, log retention, container restart policies,
  persistent volumes, backups, and spending/uptime alerts. Test restoration.
- Review SQL Server edition/licensing for the intended hosted use. Do not assume
  a development configuration is licensed for production commerce.
- Pin container versions and audit dependencies before deployment.

The hardening above is planned work, not implemented by the Swagger change.

## Acceptance Before Adding a CV Link

1. Open Swagger over HTTPS from a different network and load the combined definition
   with Products, Orders, and the Inventory internal reference.
2. Execute a public product read; confirm the request stays on the HTTPS gateway.
3. Confirm non-documentation/internal paths and direct backend ports are blocked.
4. Confirm anonymous writes are rejected and public demo identities cannot administer data.
5. Check a restart preserves data and identity configuration.
6. Verify the advertised order behavior if enabling writes; a fallback 201 must
   not be presented as a successful checkout.
7. Run `scripts/Test-ApiDocs.ps1 -GatewayUrl https://YOUR-GATEWAY -SkipAuthChecks`
   and perform role tests using non-default hosted identities separately.
8. Only then add the actual Swagger URL to the README and CV.

If the budget is zero, use documentation plus a recorded walkthrough until a
properly secured, funded live backend is available.

