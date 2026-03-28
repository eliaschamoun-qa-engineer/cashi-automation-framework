# Cashi Test Automation Platform: Architectural Design Document

---

> Disclaimer
>
> This repository is a sample architectural scaffold intended to demonstrate how a scalable fintech automation framework can be structured across web and mobile platforms.
> It is designed as a reference for framework shape, module boundaries, patterns, and engineering decisions.
> Some classes, tests, workflows, and integrations are intentionally simplified or illustrative and should not be treated as a production-ready, fully executable automation baseline without further hardening.

## 1. Executive Summary

The Cashi Test Automation Platform is an enterprise-grade, multi-module Maven ecosystem designed to validate web and mobile applications across a microservices architecture. Built on Java 17+, Playwright, Appium, and Pact JVM, the framework is engineered for high-volume parallel execution, strict thread safety, and DevSecOps integration. It strictly decouples the shared test engine from UI-specific driver management and service-boundary contract validation, ensuring that no module carries dependencies it does not need.

---

## 2. Core Architecture Overview

The repository follows a multi-module monorepo structure to enforce the DRY (Don't Repeat Yourself) principle and prevent dependency bloat:

| Module | Purpose | UI Dependencies |
|---|---|---|
| `cashi-core` | UI-agnostic engine: API clients, data factories, configuration, logging, shared utilities | **None** |
| `cashi-web` | Playwright-driven web UI testing: Page Objects, browser lifecycle, web tests | Playwright |
| `cashi-mobile` | Appium-driven mobile testing: Screen Objects, device management, mobile tests | Appium Java Client 9.x |
| `cashi-contract-testing` | Consumer-driven contract tests between microservices using Pact JVM; generates and verifies pact files | **None** |

### 2.1 Dependency Flow

```text
cashi-core  <──compile──  cashi-web
    │                         │
    │                         ├── Playwright
    │                         └── JUnit 5
    │
    ├── REST Assured
    ├── Jackson
    ├── JDBC
    ├── Allure           cashi-core  <──compile──  cashi-mobile
    ├── AssertJ                                        │
    ├── SLF4J/Logback                                  ├── Appium Java Client 9.x
    └── JUnit 5                                        └── JUnit 5

                         cashi-core  <──compile──  cashi-contract-testing
                                                        │
                                                        ├── Pact JVM Consumer (junit5)
                                                        ├── Pact JVM Provider (junit5)
                                                        └── JUnit 5
```

**Architectural Invariant:** `cashi-web`, `cashi-mobile`, and `cashi-contract-testing` are siblings — they never depend on each other. `cashi-core` has zero UI framework dependencies. `cashi-contract-testing` has zero UI framework dependencies.

### 2.2 Directory Structure

```text
cashi-automation-framework/
│
├── .github/workflows/
│   ├── nightly-regression.yml
│   ├── pr-smoke-tests.yml
│   ├── prod-sanity-checks.yml
│   ├── security-scan.yml
│   └── contract-tests.yml
│
├── infrastructure/
│   ├── Dockerfile.playwright
│   ├── docker-compose.zap.yml
│   ├── zap-config/
│   │   └── zap-rules.conf
│   └── stubs/
│       ├── sms-gateway/
│       └── payment-provider/
│
├── pom.xml
│
├── cashi-core/
│   ├── src/main/java/com/cashi/core/
│   │   ├── api/
│   │   │   └── ApiClient.java
│   │   ├── config/
│   │   │   └── ConfigManager.java
│   │   ├── data/
│   │   │   ├── factories/
│   │   │   │   └── UserFactory.java
│   │   │   └── cleanup/
│   │   │       └── DataCleanup.java
│   │   ├── db/
│   │   │   └── JdbcTemplate.java
│   │   ├── models/
│   │   │   ├── UserAccount.java
│   │   │   └── TransferReceipt.java
│   │   └── utils/
│   │       ├── BaseCoreTest.java
│   │       ├── retry/
│   │       │   └── RetryExtension.java
│   │       ├── extensions/
│   │       │   └── AllureLifecycleExtension.java
│   │       ├── assertions/
│   │       │   └── TransactionAssert.java
│   │       └── security/
│   │           ├── SecurityPayloads.java
│   │           └── HeaderValidator.java
│   ├── src/main/resources/
│   │   ├── config/
│   │   │   ├── dev.properties
│   │   │   ├── staging.properties
│   │   │   └── prod.properties
│   │   ├── security/
│   │   │   └── owasp-payloads.json
│   │   ├── logback.xml
│   │   └── allure.properties
│   └── pom.xml
│
├── cashi-web/
│   ├── src/main/java/com/cashi/web/
│   │   └── pages/
│   │       ├── LoginPage.java
│   │       ├── DashboardPage.java
│   │       └── TransferPage.java
│   ├── src/test/java/com/cashi/web/
│   │   ├── BaseWebTest.java
│   │   └── tests/
│   │       ├── login/
│   │       │   └── LoginTest.java
│   │       ├── transfers/
│   │       │   └── SendMoneyTest.java
│   │       └── security/
│   │           ├── AuthBoundaryTest.java
│   │           ├── SessionTest.java
│   │           ├── InputSanitizationTest.java
│   │           └── SecurityHeadersTest.java
│   ├── src/test/resources/
│   │   ├── testdata/
│   │   │   └── login-credentials.json
│   │   └── junit-platform.properties
│   └── pom.xml
│
└── cashi-mobile/
    ├── src/main/java/com/cashi/mobile/
    │   ├── screens/
    │   │   ├── LoginScreen.java
    │   │   ├── HomeScreen.java
    │   │   └── BiometricsScreen.java
    │   └── drivers/
    │       └── DeviceManager.java
    ├── src/test/java/com/cashi/mobile/
    │   ├── BaseMobileTest.java
    │   └── tests/
    │       ├── login/
    │       │   └── MobileLoginTest.java
    │       ├── biometrics/
    │       │   └── FingerprintTest.java
    │       └── security/
    │           ├── CertPinningTest.java
    │           ├── RootDetectionTest.java
    │           └── DeepLinkInjectionTest.java
    ├── src/test/resources/
    │   ├── capabilities/
    │   │   ├── android-emulator.json
    │   │   └── ios-simulator.json
    │   ├── testdata/
    │   │   └── biometrics-mock.json
    │   └── junit-platform.properties
    └── pom.xml

└── cashi-contract-testing/
    ├── src/main/java/com/cashi/contract/
    │   └── model/
    │       ├── LedgerEntryRequest.java
    │       ├── LedgerEntryResponse.java
    │       ├── LedgerBalanceResponse.java
    │       └── TransferResponse.java
    ├── src/test/java/com/cashi/contract/
    │   ├── BaseContractTest.java
    │   ├── consumer/
    │   │   ├── TransferServiceLedgerConsumerTest.java
    │   │   └── NotificationServiceTransferConsumerTest.java
    │   └── provider/
    │       ├── LedgerProviderVerificationTest.java
    │       └── TransferProviderVerificationTest.java
    ├── src/test/resources/
    │   ├── pacts/
    │   │   ├── TransferService-LedgerService.json
    │   │   └── NotificationService-TransferService.json
    │   └── junit-platform.properties
    └── pom.xml
```

---

## 3. Requirements Coverage Matrix

### Requirement 1: Support Multiple Environments (Dev, Staging, Production)

The framework uses a centralized configuration strategy to transition between environments without code modifications.

**Implementation:**

- A singleton `ConfigManager` inside `cashi-core` reads the target environment via Maven system properties (e.g., `mvn verify -Denv=staging`).
- Environment-specific variables (base URLs, database connection strings, API gateway URLs) are isolated in `cashi-core/src/main/resources/config/` across `dev.properties`, `staging.properties`, and `prod.properties`.
- The `prod.properties` file uses a database user with **SELECT-only** permissions.

**Production Safety:**

Tests executing against the `prod` environment trigger programmatic safeguards within the framework:

- `BaseCoreTest` blocks all test execution in `prod` unless `-Dallow.prod.readonly=true` is explicitly passed.
- `DataCleanup` hard-fails if `env=prod`, preventing any truncation or deletion against production databases.
- The `prod-sanity-checks.yml` GitHub Actions workflow requires **manual approval** via GitHub Environment protection rules before any test touches production infrastructure.
- REST Assured API clients used for data seeding are disabled in production mode — only read-oriented test flows execute.

**Execution:**

```bash
mvn verify -pl cashi-web -Denv=staging
mvn verify -pl cashi-web -Denv=prod -Dallow.prod.readonly=true
```

---

### Requirement 2: Handle Different Test Data Sets

The framework uses a dual strategy: **dynamic generation** for mutable test data and **static JSON files** for immutable reference data.

**Dynamic Data Generation:**

- `cashi-core/data/factories` contains fluent builders (`UserFactory`, `TransactionBuilder`) that use REST Assured to seed data before each test.
- Example: `UserFactory.aDefaultUser().withEmail("test@cashi.com").build()`
- Every test creates its own unique dataset in `@BeforeEach` and destroys it via automated JDBC truncations or API teardown hooks in `@AfterEach`, ensuring complete state isolation.

**Static Payload Management:**

- Complex, non-mutating datasets (JSON schemas, mock biometric payloads, OWASP security vectors) are stored in module-specific `src/test/resources/testdata/` directories.
- `cashi-web` and `cashi-mobile` maintain separate `testdata/` folders, ensuring one module does not load another's data into memory.
- Security payloads (`owasp-payloads.json`) are centralized in `cashi-core/src/main/resources/security/` for cross-module reuse.

**Data-Driven Testing:**

- JUnit 5 `@ParameterizedTest` combined with `@MethodSource` enables iteration over strongly-typed Java objects rather than primitive strings.
- `cashi-core` uses Jackson to deserialize JSON arrays directly into POJOs, feeding them into test method signatures with full type safety and IDE autocomplete.

**Example:**

```java
@ParameterizedTest(name = "Transfer {0} → expects {1}")
@MethodSource("transferAmounts")
@Tag("regression")
@Epic("Payments")
void shouldTransferMoney(BigDecimal amount, String expectedStatus) {
    // ...
}

static Stream<Arguments> transferAmounts() {
    return Stream.of(
        Arguments.of(new BigDecimal("100.00"), "COMPLETED"),
        Arguments.of(new BigDecimal("0.01"), "COMPLETED"),
        Arguments.of(new BigDecimal("999999.99"), "LIMIT_EXCEEDED")
    );
}
```

---

### Requirement 3: Provide Detailed Reporting and Logging

Traceability is implemented at the thread level to ensure that parallel executions produce readable, filterable output.

**Reporting (Allure):**

- Allure JUnit 5 is integrated globally via the Parent `pom.xml` (`allure-maven` plugin in `<pluginManagement>`).
- Every test class requires dual annotations:
  - `@Tag` — JUnit 5 execution filter (e.g., `@Tag("smoke")`, `@Tag("regression")`, `@Tag("security")`)
  - `@Epic` / `@Feature` — Allure report grouping mapped to business capabilities
- Every Page Object and Screen Object action is annotated with `@Step` for step-level traceability.
- A custom JUnit 5 extension (`AllureLifecycleExtension`) automatically captures and attaches screenshots, DOM snapshots, and network logs upon test failure.
- Allure results are written to `target/allure-results` (configured in `allure.properties`).

**Logging (SLF4J + Logback):**

- A centralized `logback.xml` in `cashi-core` enforces a strict thread-safe logging pattern:
  ```
  [%thread] %-5level %logger{36} - %msg%n
  ```
- The framework standardizes on SLF4J for all logging. Direct console output (`System.out`) is discouraged by convention.
- Engineers debugging CI failures can filter logs by the exact parallel thread ID.

---

### Requirement 4: Integrate with CI/CD Pipeline

The framework is designed as "Infrastructure as Code," with execution environments explicitly version-controlled.

**Pipeline Orchestration:**

`.github/workflows/` contains five targeted pipelines:

| Workflow | Trigger | Scope | Environment |
|---|---|---|---|
| `pr-smoke-tests.yml` | Pull Request | `@Tag("smoke")` — fast feedback | Staging |
| `nightly-regression.yml` | Scheduled (nightly) | `@Tag("regression")` — full suite | Dev / Staging |
| `prod-sanity-checks.yml` | Manual dispatch | `@Tag("smoke")` — read-only | Production (gated) |
| `security-scan.yml` | Scheduled (weekly) | OWASP ZAP baseline + active scan | Staging |
| `contract-tests.yml` | PR + push to main + nightly + manual | Consumer pacts (PR); provider verification (main/nightly) | Dev / Staging |

**Execution Commands:**

```bash
# PR gate
mvn verify -pl cashi-web -Dgroups=smoke -Denv=staging

# Nightly regression (web)
mvn verify -pl cashi-web -Dgroups=regression -Denv=staging -Dtest.parallelism=4

# Nightly regression (mobile)
mvn verify -pl cashi-mobile -Dgroups=regression -Denv=staging -Ddevice.provider=browserstack

# Prod sanity (after manual approval)
mvn verify -pl cashi-web -Dgroups=smoke -Denv=prod -Dallow.prod.readonly=true

# Security suite
mvn verify -pl cashi-web -Dgroups=security -Denv=staging

# Contract tests — consumer only (mock server, no real services)
mvn test -pl cashi-contract-testing -Denv=dev

# Contract tests — provider verification against staging
mvn test -pl cashi-contract-testing -Denv=staging -Dtest=LedgerProviderVerificationTest
mvn test -pl cashi-contract-testing -Denv=staging -Dtest=TransferProviderVerificationTest

# Dependency audit
mvn dependency-check:check
```

**Containerized Execution:**

- The Playwright module executes inside the official `mcr.microsoft.com/playwright/java` Docker container (`Dockerfile.playwright`), guaranteeing exact browser binary matches across local and CI environments.

**Third-Party Stubbing:**

- To bypass rate limits and costs associated with external SMS or KYC providers, the `infrastructure/stubs/` directory contains version-controlled WireMock mappings organized by service (`sms-gateway/`, `payment-provider/`).
- Stubs serve deterministic API responses during staging and CI runs.

---

### Requirement 5: Support Parallel Execution

The architecture overrides JUnit 5's default sequential execution to reduce pipeline duration, implementing strict memory and thread isolation.

**Thread Safety:**

Playwright and Appium drivers are not thread-safe. The framework isolates them using `ThreadLocal<>`:

- `BaseWebTest` in `cashi-web` creates and manages a `ThreadLocal<BrowserContext>`, ensuring every parallel test instance receives its own isolated Playwright browser context.
- `BaseMobileTest` in `cashi-mobile` creates and manages a `ThreadLocal<AppiumDriver>` via `DeviceManager`, ensuring every parallel test instance receives its own isolated device session.
- `BaseCoreTest` in `cashi-core` contains **no driver management code** — it handles only cross-cutting concerns (Allure lifecycle, logging, config loading, data cleanup hooks).

**Inheritance Chain:**

```text
BaseCoreTest (cashi-core)
    ├── BaseWebTest (cashi-web)       → Playwright browser/context lifecycle
    └── BaseMobileTest (cashi-mobile) → Appium driver lifecycle via DeviceManager
```

**Per-Module Configuration:**

Each module has its own `junit-platform.properties` in `src/test/resources/`:

**cashi-web** (browsers are moderately resource-heavy):

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=${test.parallelism:-4}
```

**cashi-mobile** (emulators are resource-heavy):

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=${test.parallelism:-2}
```

**CI Override:** Pipelines inject `-Dtest.parallelism=N` to tune for runner hardware. Classes run concurrently; methods within a class run sequentially. This is the safe default for both Playwright (browser context per class) and Appium (driver per class).

---

### Requirement 6: Include Data-Driven Testing Capabilities

Data-Driven Testing is implemented using JUnit 5's native parameterization combined with strongly-typed Java object streams.

| Mechanism | Use Case | Location |
|---|---|---|
| `@ParameterizedTest` + `@MethodSource` | Complex Java objects, combinatorial data | Test classes |
| `@ParameterizedTest` + `@CsvSource` | Simple tabular inline data | Test classes |
| Jackson JSON deserialization | External JSON → POJO mapping | `cashi-core` (Jackson) + `testdata/` directories |
| `SecurityPayloads.java` + `owasp-payloads.json` | OWASP XSS/SQLi vectors for security DDT | `cashi-core/utils/security/` |
| `UserFactory` / `TransactionBuilder` | Programmatic data generation | `cashi-core/data/factories/` |

**Security DDT Example:**

The `cashi-web/tests/security/InputSanitizationTest` uses data-driven testing to iterate over hundreds of OWASP payloads through form fields:

```java
@ParameterizedTest(name = "XSS payload #{index}: {0}")
@MethodSource("xssPayloads")
@Tag("security")
@Epic("Security")
@Feature("Input Sanitization")
void shouldNotExecuteXssInSearchField(String payload) {
    SearchPage search = new SearchPage(getPage());
    search.enterQuery(payload);
    search.submit();

    assertThat(search.getResultsHtml())
        .doesNotContain("<script>")
        .doesNotContain("onerror=")
        .doesNotContain("javascript:");
}

static Stream<String> xssPayloads() {
    return SecurityPayloads.xssVectors().stream();
}
```

---

### Requirement 7: Automated Security Checks

Security testing is implemented across three layers: build-time dependency scanning, dynamic application security testing (DAST), and functional security test cases.

**Layer 1: Dependency Vulnerability Scanning (Build-Time)**

- The OWASP Dependency-Check Maven plugin is configured in the Parent `pom.xml` under `<pluginManagement>`.
- Runs during every `mvn verify` execution across all modules.
- Fails the build if any dependency has a known CVE with a score >= 7.
- Generates an HTML report alongside Allure results.

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.3</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

**Layer 2: DAST via OWASP ZAP (Weekly)**

- `infrastructure/docker-compose.zap.yml` provisions a ZAP proxy container targeting the staging application.
- `security-scan.yml` runs weekly: ZAP baseline scan (passive) followed by an active scan against staging.
- Results are published as CI artifacts for security team review.

**Layer 3: Functional Security Tests (Nightly)**

Tests that validate security behavior requiring business logic context — things automated scanners cannot cover.

**Web Security Tests** (`cashi-web/tests/security/`):

| Test Class | Validates |
|---|---|
| `AuthBoundaryTest` | Unauthorized access to protected routes returns 401/403 |
| `SessionTest` | Token expiry, session fixation, concurrent session handling |
| `InputSanitizationTest` | XSS payloads in form fields do not execute (data-driven via `SecurityPayloads`) |
| `SecurityHeadersTest` | CSP, X-Frame-Options, HSTS, X-Content-Type-Options on all page responses |

**Mobile Security Tests** (`cashi-mobile/tests/security/`):

| Test Class | Validates |
|---|---|
| `CertPinningTest` | SSL certificate pinning prevents MITM interception |
| `RootDetectionTest` | App correctly detects and handles rooted/jailbroken devices |
| `DeepLinkInjectionTest` | Malicious deep link URI schemes are rejected |

**Shared Utilities** (`cashi-core/utils/security/`):

| Utility | Purpose |
|---|---|
| `SecurityPayloads.java` | Provides OWASP XSS, SQLi, and path traversal payload lists for parameterized tests |
| `HeaderValidator.java` | AssertJ-style fluent validation: `assertSecurityHeaders(response)` |

**Execution:**

```bash
# Functional security tests
mvn verify -pl cashi-web -Dgroups=security -Denv=staging
mvn verify -pl cashi-mobile -Dgroups=security -Denv=staging

# Dependency audit (standalone)
mvn dependency-check:check
```

---

### Requirement 8: Consumer-Driven Contract Testing

Contract testing validates the API boundaries between Cashi microservices without requiring a fully integrated environment. It guarantees that a provider service never silently breaks its consumer's expectations.

**Technology:** Pact JVM 4.6.5 (Pact Specification 3.0) via the `cashi-contract-testing` Maven module.

**Contracts Implemented:**

| Consumer | Provider | Interactions |
|---|---|---|
| **TransferService** | **LedgerService** | `POST /v1/ledger/entries` (post double-entry journal), `GET /v1/ledger/balance/{accountId}` (pre-transfer balance check), `GET /v1/ledger/entries/{transactionId}` (audit lookup) |
| **NotificationService** | **TransferService** | `GET /v1/transfers/{transferId}` (fetch transfer for confirmation alert), `GET /v1/transfers?accountId=&status=COMPLETED` (batch digest notification) |

**Two-Phase Execution Model:**

- **Phase 1 — Consumer tests** (`*ConsumerTest`): Pact starts an in-process mock server. The consumer-side code sends real HTTP requests to this mock server. Pact validates the requests match the agreed interaction shape and generates a pact JSON file in `target/pacts/`. No real service is required — these run on every PR in CI using only `cashi-contract-testing` with `-Denv=dev`.

- **Phase 2 — Provider verification** (`*VerificationTest`): Pact loads the committed pact files from `src/test/resources/pacts/` and replays each interaction against the real running service (resolved from `ConfigManager` using `ledger.service.host`, `ledger.service.port`, etc.). Provider state setup methods (`@State`) seed the necessary preconditions before each interaction. This phase runs on merge to `main` and nightly against staging.

**Pact Files (committed fixtures):**

- `src/test/resources/pacts/TransferService-LedgerService.json` — three interactions with Pact Specification 3.0 matching rules
- `src/test/resources/pacts/NotificationService-TransferService.json` — two interactions

**Pact Broker (optional):**

The `cashi-contract-testing/pom.xml` configures the `pact:publish` Maven goal. Pact files can be published to a Pact Broker after consumer tests pass, enabling provider teams to pull and verify the latest contracts without committing them to the repository.

```bash
mvn -pl cashi-contract-testing pact:publish \
  -Dpact.broker.url=https://your-broker.example.com \
  -Dpact.broker.auth.token=${PACT_BROKER_TOKEN}
```

**Provider State Configuration:**

Service endpoints are resolved from environment config keys added to all `*.properties` files:

```properties
# config/staging.properties (example)
transfer.service.host=transfer-service.staging.cashi.internal
transfer.service.port=8080
ledger.service.host=ledger-service.staging.cashi.internal
ledger.service.port=8085
notification.service.host=notification-service.staging.cashi.internal
notification.service.port=8090
```

**Execution:**

```bash
# Consumer tests (mock server — no real services needed)
mvn test -pl cashi-contract-testing -Denv=dev

# Provider verification — LedgerService must be running
mvn test -pl cashi-contract-testing -Denv=staging -Dtest=LedgerProviderVerificationTest

# Provider verification — TransferService must be running
mvn test -pl cashi-contract-testing -Denv=staging -Dtest=TransferProviderVerificationTest
```

---

## 4. Architectural Invariants (Non-Negotiable Rules)

| Rule | Enforcement Mechanism |
|---|---|
| `cashi-core` has **zero** UI framework dependencies | `pom.xml` review — no Playwright, no Appium artifacts |
| `BaseCoreTest` contains **no** driver or browser code | Code review + ArchUnit rule |
| Tests that mutate data **cannot run in prod** | `BaseCoreTest` throws `IllegalStateException` if `env=prod` without explicit read-only flag |
| `DataCleanup` **hard-fails** if `env=prod` | Programmatic guard in cleanup logic |
| Every test class has **both** `@Tag` (execution) and `@Epic` (reporting) | Code review convention |
| Page Objects and Screen Objects contain **no assertions** | Pages act, tests assert |
| `prod-sanity-checks.yml` requires **manual approval** | GitHub Environment protection rules |
| `cashi-web` and `cashi-mobile` never depend on each other | Maven module dependency graph — siblings only |
| `cashi-contract-testing` has **zero** UI framework dependencies | `pom.xml` review — no Playwright, no Appium artifacts |
| Contract tests are **blocked against prod** | `BaseContractTest` throws `IllegalStateException` if `env=prod` |
| Consumer pact files are **committed to source control** | `src/test/resources/pacts/` tracked in Git; provider verifications read from this path |
| `cashi-web`, `cashi-mobile`, and `cashi-contract-testing` **never depend on each other** | Maven module dependency graph — all three are siblings of `cashi-core` |

---

## 5. Tagging Convention

All test classes must use JUnit 5 `@Tag` annotations for execution filtering **and** Allure annotations for report grouping. These serve different purposes and are both required.

| Annotation | Purpose | Example |
|---|---|---|
| `@Tag("smoke")` | JUnit 5: selects tests for PR gate pipeline | Fast, critical-path tests |
| `@Tag("regression")` | JUnit 5: selects tests for nightly full suite | Comprehensive coverage |
| `@Tag("security")` | JUnit 5: selects tests for security suite | OWASP, auth, session tests |
| `@Tag("contract")` | JUnit 5: selects tests for contract testing pipeline | Consumer pacts + provider verification |
| `@Tag("web")` / `@Tag("mobile")` | JUnit 5: platform filter (optional) | Cross-cutting selection |
| `@Epic("Payments")` | Allure: top-level report grouping | Business domain |
| `@Feature("Send Money")` | Allure: feature-level report grouping | Specific capability |
| `@Step("Enter amount")` | Allure: step-level traceability | Page/Screen object methods |

**Example:**

```java
@Tag("smoke")
@Tag("web")
@Epic("Authentication")
@Feature("Login")
class LoginTest extends BaseWebTest {

    @Test
    @Step("Verify successful login with valid credentials")
    void shouldLoginWithValidCredentials() {
        // ...
    }
}
```

---

## 6. Technology Stack

| Component | Technology | Version Strategy |
|---|---|---|
| Language | Java 17+ | LTS |
| Build | Maven | Multi-module with Parent POM `<dependencyManagement>` |
| Web UI | Playwright for Java | Pinned in Parent POM |
| Mobile UI | Appium Java Client | 9.x (not legacy 8.x) |
| Contract Testing | Pact JVM (Consumer + Provider) | 4.6.5, Pact Spec 3.0, pinned in Parent POM |
| API Client | REST Assured | Data seeding only; API functional testing handled by Postman |
| Assertions | AssertJ | Fluent, readable assertions across all modules |
| Serialization | Jackson | JSON to POJO mapping |
| Database | JDBC | Connection pooling, query helpers |
| Test Runner | JUnit 5 | Managed via `junit-bom` in Parent POM |
| Reporting | Allure | JUnit 5 integration, global via Parent POM |
| Logging | SLF4J + Logback | Thread-safe, centralized in `cashi-core` |
| Security Scan | OWASP Dependency-Check + OWASP ZAP | Build-time CVE + weekly DAST |
| CI/CD | GitHub Actions | 5 workflow files |
| Containers | Docker | Playwright execution + ZAP proxy |
| Stubbing | WireMock | Versioned mappings in `infrastructure/stubs/` |
