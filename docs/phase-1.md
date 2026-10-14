# Phase 1: Startup and Build

The Maven modules, Docker build/runtime, and GitHub Actions use Java 26.
Use Maven 3.9+ for local builds. The root POM pins lifecycle plugin versions;
modules inherit the Java release and preserve parameter names for Spring.

Each service produces a regular JAR and an executable `*-exec.jar`.
Docker copies only the executable JAR. The regular inventory JAR remains
usable by the existing order tests during a reactor package build.

## Start and Verify

```powershell
docker compose config --quiet
docker compose up -d --build
docker compose ps -a
powershell -NoProfile -File scripts/Test-Startup.ps1
```

The smoke check verifies realm import, token issuance and roles for both demo
users, gateway JWT validation, and a product read backed by MongoDB. It does
not create orders or change catalog data. Eureka routing may take a short
time to register after containers start.

The SQL initialization container should finish with exit code 0. The other
containers are long-running services. Keep existing database volumes when
rebuilding; do not use `docker compose down -v` to troubleshoot startup.

## Local Demo Authentication

The realm import is a regular JSON file under `keycloak/realm-export.json`.
Compose fails if the bind source is missing instead of creating a directory.
Keycloak readiness gates gateway startup. The gateway validates the external
issuer `http://localhost:18085/realms/spring-boot-microservice-realm` and loads
signing keys from the internal `keycloak:8080` address.

Demo client: `spring-boot-microservice-client` (password grant).
Realm users: `user/password` (USER) and `admin/password` (USER, ADMIN).
Keycloak administration: `admin/admin`. These settings are for local demos.
Keycloak imports this realm on a new container; changes made in its console
are not persisted across container recreation by this configuration.

## Scope

Phase 1 repairs build and startup configuration. The existing order test
contract failures, inventory correctness, Kafka delivery, security isolation,
and monitoring integration remain tracked in the subsequent phases.

```powershell
mvn -B clean package -DskipTests
mvn -B clean verify
```

The first command builds and compiles tests without executing them. The
second runs the suite and must not be treated as passing until its known
contract and test-fixture issues are fixed.
