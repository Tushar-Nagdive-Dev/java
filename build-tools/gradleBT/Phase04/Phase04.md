# Phase 4A – Feature Flag Service (Real‑World Example) ✅

**Goal:** Learn *true* integration testing with Gradle by building a small, practical **Feature Flag Service** (aka Toggle service). We’ll use:

* **Multi‑module** layout you already have: `service-core` (domain, repo, service) and `service-api` (Spring Boot app + REST).
* **HSQLDB (in‑memory)** for fast, isolated tests (no Docker).
* A separate **`integrationTest`** Gradle source set wired into `check`.

> Why Feature Flags? It’s a very real thing teams use to enable/disable features at runtime per environment—much more realistic than Product/Order demos.

---

## 0) Learning Outcomes

By the end, you’ll know how to:

1. Model a real domain (Feature Flag) with JPA.
2. Structure service + controller across modules.
3. Configure **HSQLDB** with a **test profile** only.
4. Create a dedicated **integrationTest** task + source set.
5. Write end‑to‑end tests that start the app, call REST, hit DB, and assert state.
6. Add unit & slice tests for a clean test pyramid.

---

## 1) Domain & API Design

**Entity: `FeatureFlag`**

* `id: Long`
* `key: String` (unique, required) – e.g., `checkout.newFlow`
* `enabled: boolean` (default false)
* `description: String?` (optional, max 255)

**Use cases**

* Create a flag
* List all flags
* Toggle a flag on/off
* Evaluate a flag (return enabled/disabled)

**Endpoints** (under `/api/flags`)

* `POST /api/flags` – create flag `{key, description?, enabled?}` → returns created flag
* `GET  /api/flags` – list flags
* `POST /api/flags/{key}/toggle` – toggle flag `{enabled}` → returns updated flag
* `GET  /api/flags/{key}` – get single flag by key (evaluate)

---

## 2) Gradle Wiring (service-api/build.gradle.kts)

**Add/confirm these pieces** so HSQLDB is used *only* for tests and `integrationTest` is separate.

```kotlin
plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    application
}

dependencies {
    implementation(project(":service-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Runtime driver for real DBs (not used by tests with HSQLDB)
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.hsqldb:hsqldb:2.7.2")
}

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}
configurations {
    create("integrationTestImplementation") { extendsFrom(configurations.testImplementation.get()) }
    create("integrationTestRuntimeOnly")   { extendsFrom(configurations.testRuntimeOnly.get()) }
}

val integrationTest by tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.named("check") { dependsOn(integrationTest) }
```

**Why this matters**

* Keeps unit tests (`src/test`) fast.
* Puts slower, boot‑the‑app tests into `integrationTest` and runs them on `check`.

---

## 3) HSQLDB Test Profile

Create **only for tests** so your dev/prod configs remain clean.

**`service-api/src/integrationTest/resources/application.yml`**

```yaml
spring:
  config:
    activate:
      on-profile: test
  datasource:
    url: jdbc:hsqldb:mem:testdb
    username: sa
    password:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate.dialect: org.hibernate.dialect.HSQLDialect
```

> If you also want to run Spring tests from `src/test/java`, copy the same file to `src/test/resources/`.

---

## 4) `service-core` – Domain, Repo, Service

**`service-core/src/main/java/org/phase03/core/flag/FeatureFlag.java`**

```java
package org.phase03.core.flag;

import jakarta.persistence.*;

@Entity
@Table(name = "feature_flags", uniqueConstraints = @UniqueConstraint(name = "uk_flag_key", columnNames = "flag_key"))
public class FeatureFlag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flag_key", nullable = false, length = 120)
    private String key;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(length = 255)
    private String description;

    protected FeatureFlag() {}

    public FeatureFlag(String key, boolean enabled, String description) {
        this.key = key; this.enabled = enabled; this.description = description;
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public boolean isEnabled() { return enabled; }
    public String getDescription() { return description; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDescription(String description) { this.description = description; }
}
```

**`service-core/src/main/java/org/phase03/core/flag/FeatureFlagRepository.java`**

```java
package org.phase03.core.flag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByKey(String key);
    boolean existsByKey(String key);
}
```

**`service-core/src/main/java/org/phase03/core/flag/FeatureFlagService.java`**

```java
package org.phase03.core.flag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeatureFlagService {
    private final FeatureFlagRepository repo;

    public FeatureFlagService(FeatureFlagRepository repo) { this.repo = repo; }

    public FeatureFlag create(String key, boolean enabled, String description) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key is required");
        if (repo.existsByKey(key)) throw new IllegalStateException("flag key already exists: " + key);
        return repo.save(new FeatureFlag(key, enabled, description));
    }

    public FeatureFlag toggle(String key, boolean enabled) {
        var flag = repo.findByKey(key).orElseThrow(() -> new IllegalArgumentException("not found: " + key));
        flag.setEnabled(enabled);
        return repo.save(flag);
    }
}
```

> **Why a service?** Keeps business rules (uniqueness, validation, toggling) in one place; controller stays thin.

---

## 5) `service-api` – Boot App + Controller

**`service-api/src/main/java/org/phase03/api/Phase03ApiApplication.java`**

```java
package org.phase03.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication(scanBasePackages = "org.phase03")
public class Phase03ApiApplication {
    public static void main(String[] args) { SpringApplication.run(Phase03ApiApplication.class, args); }
}
```

**`service-api/src/main/java/org/phase03/api/web/FeatureFlagController.java`**

```java
package org.phase03.api.web;

import org.phase03.core.flag.FeatureFlag;
import org.phase03.core.flag.FeatureFlagRepository;
import org.phase03.core.flag.FeatureFlagService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flags")
public class FeatureFlagController {
    private final FeatureFlagService service;
    private final FeatureFlagRepository repo;

    public FeatureFlagController(FeatureFlagService service, FeatureFlagRepository repo) {
        this.service = service; this.repo = repo;
    }

    public record CreateFlagRequest(String key, Boolean enabled, String description) {}
    public record ToggleRequest(Boolean enabled) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlag create(@RequestBody CreateFlagRequest req) {
        boolean enabled = req.enabled() != null && req.enabled();
        return service.create(req.key(), enabled, req.description());
    }

    @GetMapping
    public List<FeatureFlag> list() { return repo.findAll(); }

    @GetMapping("/{key}")
    public FeatureFlag get(@PathVariable String key) {
        return repo.findByKey(key).orElseThrow(() -> new IllegalArgumentException("not found: " + key));
    }

    @PostMapping("/{key}/toggle")
    public FeatureFlag toggle(@PathVariable String key, @RequestBody ToggleRequest req) {
        boolean enabled = req.enabled() != null && req.enabled();
        return service.toggle(key, enabled);
    }
}
```

---

## 6) Integration Test (HSQLDB) – End‑to‑End

**`service-api/src/integrationTest/java/org/phase03/api/FeatureFlagFlowIT.java`**

```java
package org.phase03.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FeatureFlagFlowIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void create_toggle_list_get() {
        // Create a flag
        var createBody = Map.of("key", "checkout.newFlow", "enabled", false, "description", "experiment");
        var created = rest.postForEntity("/api/flags", createBody, Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().get("key")).isEqualTo("checkout.newFlow");

        // Toggle it on
        var toggleBody = Map.of("enabled", true);
        var toggleResp = rest.postForEntity("/api/flags/checkout.newFlow/toggle", toggleBody, Map.class);
        assertThat(toggleResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggleResp.getBody().get("enabled")).isEqualTo(true);

        // List flags
        var list = rest.getForEntity("/api/flags", List.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).isNotEmpty();

        // Get single
        var get = rest.getForEntity("/api/flags/checkout.newFlow", Map.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().get("enabled")).isEqualTo(true);
    }
}
```

**What this proves**

* Spring app boots with **test profile** → uses **HSQLDB** in‑memory DB.
* Repository + Service + Controller are wired correctly.
* REST → Service → DB full flow works.

---

## 7) Unit & Slice Tests (Bonus)

**Unit test** (`src/test/java`) for service rules:

```java
package org.phase03.core.flag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeatureFlagServiceTest {
    @Test
    void rejects_duplicate_key() {
        var repo = mock(FeatureFlagRepository.class);
        when(repo.existsByKey("a.b")).thenReturn(true);
        var svc = new FeatureFlagService(repo);
        assertThrows(IllegalStateException.class, () -> svc.create("a.b", false, null));
    }
}
```

**Controller slice** (`@WebMvcTest`) example:

```java
package org.phase03.api.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.phase03.core.flag.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@WebMvcTest(FeatureFlagController.class)
class FeatureFlagControllerTest {
    @Autowired MockMvc mvc;
    @MockBean FeatureFlagService service;
    @MockBean FeatureFlagRepository repo; // controller calls repo in list/get

    @Test
    void create_requires_key() throws Exception {
        mvc.perform(post("/api/flags").contentType("application/json").content("{}"))
           .andExpect(status().is4xxClientError()); // Spring will return 400 because key is missing
    }
}
```

---

## 8) How to Run

```bash
# Unit tests only
./gradlew :service-api:test

# Integration tests (HSQLDB, boots app)
./gradlew :service-api:integrationTest

# Everything
./gradlew :service-api:check
```

---

## 9) Common Pitfalls & Fixes

* **HSQLDB vs Postgres differences:** For advanced SQL (JSONB, sequences), keep a Postgres Testcontainers job in CI later.
* **Profiles leaking:** Ensure `spring.profiles.active=test` is set by the integrationTest task; do not load `.env` into tests by default.
* **Controller validation:** Add Spring validation (`@Validated`, `@NotBlank`) if you want stricter payload checks.
* **Package scanning:** `@SpringBootApplication(scanBasePackages = "org.phase03")` ensures both modules are scanned.

---

## 10) Next Steps (pick any)

* Add **Bean Validation** to DTOs + exception handlers (`@ControllerAdvice`) for 400 errors.
* Replace `ddl-auto` with **Flyway** migrations and assert schema in tests.
* Add **version catalog** (`libs.versions.toml`) and move deps there.
* Add **GitHub Actions** CI workflow (`check` + cache) and a **Jacoco** coverage report.
