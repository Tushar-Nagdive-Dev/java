# 📌 Phase 3 – Advanced Gradle

## 1. 🔹 Multi-Module Projects

Large systems (like microservices or layered apps) often use **multi-module builds**.

📂 Example structure:

```
my-company-app/
 ├── settings.gradle(.kts)
 ├── build.gradle(.kts)   // root
 ├── common/              // shared code (utilities, DTOs)
 │   └── build.gradle(.kts)
 ├── service-api/         // REST controllers
 │   └── build.gradle(.kts)
 └── service-core/        // business logic
     └── build.gradle(.kts)
```

### `settings.gradle.kts`

```kotlin
rootProject.name = "my-company-app"
include("common", "service-api", "service-core")
```

### Example dependency sharing

```kotlin
// service-api/build.gradle.kts
dependencies {
    implementation(project(":common"))
    implementation(project(":service-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

✅ Benefit: Shared logic in `common`, no code duplication.

---

## 2. 🔹 Task Customization & Dependencies

Tasks can depend on each other and be customized.

### Example 1: Custom task

#### Groovy DSL

```groovy
task printEnv {
    doLast {
        println "Running in environment: ${project.findProperty("env") ?: "dev"}"
    }
}
```

#### Kotlin DSL

```kotlin
tasks.register("printEnv") {
    doLast {
        println("Running in environment: ${project.findProperty("env") ?: "dev"}")
    }
}
```

Run with:

```bash
./gradlew printEnv -Penv=qa
```

---

### Example 2: Task dependencies

```kotlin
tasks.register("prepare") {
    doLast { println("Preparing...") }
}

tasks.register("deploy") {
    dependsOn("prepare")
    doLast { println("Deploying...") }
}
```

➡️ Running `./gradlew deploy` first runs `prepare` then `deploy`.

---

## 3. 🔹 Build Cache & Performance

Gradle is fast because of **incremental builds**.

* **Incremental compilation** → only changed code is rebuilt.
* **Build cache** → reuse results from previous builds (even across machines in CI/CD).
* **Parallel execution** → `./gradlew build --parallel`.
* **Daemon process** → Gradle runs in background for faster startup.

Enable build cache in `settings.gradle(.kts)`:

```kotlin
buildCache {
    local {
        isEnabled = true
    }
}
```

---

## 4. 🔹 Profiles & Environments

You can control environment-specific builds via **`gradle.properties`** or CLI args.

### `gradle.properties`

```properties
env=dev
db.url=jdbc:postgresql://localhost:5432/devdb
```

### Access in `build.gradle.kts`

```kotlin
val env: String by project
println("Environment = $env")
```

Run with:

```bash
./gradlew build -Penv=prod
```

---

## 5. ✅ Key Takeaways (Phase 3)

* **Multi-module builds** help structure large projects (like microservices).
* **Custom tasks** let you script business-specific automation.
* **Task dependencies** ensure workflows run in correct order.
* **Build cache, parallelism, daemon** = speed.
* **Profiles/properties** let you handle multiple environments (Dev/QA/Prod).

---

👉 Next Steps:
In **Phase 4: Integration & Real-World Use**, we’ll learn:

* Gradle with **Spring Boot + Angular integration**.
* **CI/CD pipelines** (Jenkins/GitHub Actions).
* **Artifact publishing** (Nexus, Artifactory).
* Automating Docker builds with Gradle.

---

# 1) Mental model: Task graph (DAG)

* A **task** = a named unit of work (compile, copy, zip, run tests, deploy, etc.).
* Gradle builds a **directed acyclic graph** of tasks based on relationships you define.
* You *request* one or more tasks (`./gradlew build`), Gradle figures out everything it must run, in the right order, skipping up-to-date ones.

---

# 2) Register vs configure (performance!)

Prefer **`tasks.register("name", Type::class)`** instead of `tasks.create` or `tasks.named(...).configure` everywhere possible.

### Kotlin DSL

```kotlin
// Lazy registration: task is created/configured only if needed
val hello by tasks.register("hello") {
    doLast { println("Hello, Gradle!") }
}
```

### Groovy DSL

```groovy
tasks.register('hello') {
    doLast { println 'Hello, Gradle!' }
}
```

Why: **configuration avoidance** → faster builds, especially with many tasks.

---

# 3) Dependencies vs ordering

There are **two different ideas**:

* **`dependsOn(A)`**: B *needs* A’s outputs; A will be executed before B, and if A fails, B won’t run.
* **Ordering-only**:

    * **`mustRunAfter(A)`**: B must be scheduled after A *if both are present*; does **not** force A to run.
    * **`shouldRunAfter(A)`**: soft preference; Gradle may violate to break cycles.
    * **`finalizedBy(C)`**: after B finishes (success or failure), run C (good for cleanup).

### Kotlin DSL

```kotlin
val prepare by tasks.register("prepare") {
    doLast { println("Preparing…") }
}

val deploy by tasks.register("deploy") {
    dependsOn(prepare)            // hard dependency
    mustRunAfter("test")          // only ordering
    doLast { println("Deploying!") }
}

val cleanup by tasks.register("cleanup") {
    doLast { println("Cleaning temp files…") }
}
deploy.finalizedBy(cleanup)       // always run cleanup after deploy
```

### Groovy DSL

```groovy
def prepare = tasks.register('prepare') {
    doLast { println 'Preparing…' }
}
def deploy = tasks.register('deploy') {
    dependsOn prepare
    mustRunAfter 'test'
    doLast { println 'Deploying!' }
}
tasks.register('cleanup') {
    doLast { println 'Cleaning temp files…' }
}
deploy.configure { finalizedBy 'cleanup' }
```

**Rule of thumb**:

* Use **`dependsOn`** when the output of one task is truly needed by another.
* Use **`mustRunAfter/shouldRunAfter`** for sequencing without creating false dependencies.
* Use **`finalizedBy`** for cleanup/notifications.

---

# 4) Incremental builds: `inputs` and `outputs`

Tell Gradle what a task reads/writes so it can **skip** work when nothing changed.

### Kotlin DSL

```kotlin
abstract class StampTask : DefaultTask() {

    @get:Input
    abstract val message: Property<String>

    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    @TaskAction
    fun write() {
        targetFile.get().asFile.writeText("STAMP: ${message.get()}")
    }
}

tasks.register<StampTask>("stamp") {
    message.set("build-${project.version}")
    targetFile.set(layout.buildDirectory.file("stamp.txt"))
}
```

### Groovy DSL

```groovy
abstract class StampTask extends DefaultTask {
    @Input
    abstract Property<String> getMessage()
    @OutputFile
    abstract RegularFileProperty getTargetFile()

    @TaskAction
    void write() {
        targetFile.get().asFile.text = "STAMP: ${message.get()}"
    }
}

tasks.register('stamp', StampTask) {
    message.set("build-${project.version}")
    targetFile.set(layout.buildDirectory.file("stamp.txt"))
}
```

Now `./gradlew stamp` will be **up-to-date** if neither `message` nor the file changed.

---

# 5) Only run when needed: `onlyIf` and `enabled`

### Kotlin DSL

```kotlin
tasks.register("publishIfProd") {
    onlyIf { project.findProperty("env") == "prod" }
    doLast { println("Publishing to PROD…") }
}
```

### Groovy DSL

```groovy
tasks.register('publishIfProd') {
    onlyIf { project.findProperty('env') == 'prod' }
    doLast { println 'Publishing to PROD…' }
}
```

Run: `./gradlew publishIfProd -Penv=prod`

You can also disable a task:

```kotlin
tasks.named("jar") { enabled = false }
```

---

# 6) Use built-in task types (do more with less code)

* **Copy**: file sync
* **Zip/Tar**: archive
* **JavaExec**: run a JVM main class
* **Exec**: run external commands (npm, docker, etc.)

### Kotlin DSL

```kotlin
tasks.register<Copy>("copyFrontend") {
    from("../frontend/dist")
    into(layout.buildDirectory.dir("frontend"))
}

tasks.register<Zip>("distZip") {
    from("src/dist")
    archiveFileName.set("bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
    dependsOn("copyFrontend")
}

tasks.register<JavaExec>("runTool") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.example.tools.SchemaGenerator")
    args("—format=json")
}
```

---

# 7) Wiring tasks across **modules** (multi-project)

You can make a task in one module depend on a task in another.

### Kotlin DSL (in `service-api`)

```kotlin
// Ensure :frontend:build runs before processResources in a Boot app
tasks.named("processResources") {
    dependsOn(gradle.includedBuilds) // (for included builds) OR:
    dependsOn(":frontend:npmBuild")
}
```

### Groovy DSL

```groovy
tasks.named('processResources') {
    dependsOn ':frontend:npmBuild'
}
```

You can also reference tasks strongly:

```kotlin
val npmBuild = project(":frontend").tasks.named("npmBuild")
tasks.named("processResources") { dependsOn(npmBuild) }
```

---

# 8) Passing outputs between tasks (Providers)

Use **`Provider`** APIs so values/files are resolved lazily.

### Kotlin DSL

```kotlin
val generateConfig by tasks.register("generateConfig") {
    val outFile = layout.buildDirectory.file("gen/app.conf")
    outputs.file(outFile)
    doLast { outFile.get().asFile.writeText("port=8080") }
}

tasks.register<Copy>("packageConfig") {
    from(tasks.named("generateConfig").map { it.outputs.files })
    into(layout.buildDirectory.dir("package"))
}
```

This avoids early evaluation and keeps configuration fast.

---

# 9) `doFirst` / `doLast` hooks

* **`doFirst {}`** runs before main actions.
* **`doLast {}`** runs after.

```kotlin
tasks.register("deploy") {
    doFirst { println("Checking credentials…") }
    doLast { println("Deployment done.") }
}
```

Use sparingly; prefer typed tasks or custom task classes for bigger logic.

---

# 10) Default tasks & grouping

Set a **default task** so `./gradlew` with no args runs something useful.

```kotlin
defaultTasks("clean", "build")

tasks.register("deploy") { group = "release"; description = "Deploys to target env" }
```

---

# 11) Real-world workflow example (Angular + Spring Boot)

**Goal:** Build frontend, copy to backend `resources/static`, then create a Boot jar.

### Kotlin DSL (in `service-api` module)

```kotlin
val npmInstall by tasks.register<Exec>("npmInstall") {
    workingDir = file("../frontend")
    commandLine("npm", "ci")
    inputs.file("../frontend/package-lock.json")
    outputs.dir("../frontend/node_modules")
}

val npmBuild by tasks.register<Exec>("npmBuild") {
    workingDir = file("../frontend")
    commandLine("npm", "run", "build")
    dependsOn(npmInstall)
    inputs.dir("../frontend/src")
    outputs.dir("../frontend/dist")
}

tasks.register<Copy>("copyFrontend") {
    from("../frontend/dist")
    into("src/main/resources/static")
    dependsOn(npmBuild)
}

// Ensure resources include frontend files before jar/bootJar
tasks.named("processResources") { dependsOn("copyFrontend") }
```

This creates a clean, incremental pipeline:
`npmInstall` → `npmBuild` → `copyFrontend` → `processResources` → `bootJar`.

---

# 12) Custom task classes vs ad-hoc tasks

* Use **ad-hoc** (`tasks.register { doLast { … } }`) for small glue steps.
* Use **custom task classes** (`DefaultTask` + `@TaskAction`) when:

    * You have inputs/outputs.
    * You want reuse across modules.
    * Logic is non-trivial.

Put shared task classes in **`buildSrc`** or a **conventions plugin** for reuse.

---

# 13) Common pitfalls (and fixes)

* ❌ Using `dependsOn` when you only need order → creates fake dependencies and slows builds.
  ✅ Use `mustRunAfter`/`shouldRunAfter`.
* ❌ Eager `tasks.create` everywhere.
  ✅ Use `tasks.register` + `named(...).configure`.
* ❌ Not declaring `inputs/outputs`.
  ✅ Declare them for incremental builds.
* ❌ Hard-coded paths.
  ✅ Use `layout.buildDirectory`, `project.layout`, `providers`.
* ❌ Massive build files.
  ✅ Extract conventions to root or `buildSrc`.

---

## TL;DR cheat sheet

* **Define** tasks with `tasks.register`.
* **Connect** with `dependsOn`, **order** with `mustRunAfter`, clean up with `finalizedBy`.
* **Speed** with configuration avoidance + `inputs/outputs`.
* **Reuse** with built-in types (`Copy`, `Zip`, `Exec`, `JavaExec`) or custom task classes.
* **Scale** across modules using cross-project `dependsOn` and Providers.

---

# 1) Environment configuration (dev / test / prod)

## A. Use Spring Profiles first (the standard way)

Create a single `application.yml` in `service-api/src/main/resources` with profile sections:

```yaml
# service-api/src/main/resources/application.yml
spring:
  application:
    name: service-api
  datasource:
    url: jdbc:postgresql://localhost:5432/devdb
    username: devuser
    password: devpass
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.format_sql: true
server:
  port: 8080

---
spring:
  config:
    activate:
      on-profile: test
  datasource:
    # With Testcontainers we won't hardcode these; they get injected dynamically.
    # If you want an H2 fallback for simple tests, uncomment:
    # url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    # username: sa
    # password:
  jpa:
    hibernate:
      ddl-auto: create-drop
server:
  port: 0

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASS}
  jpa:
    hibernate:
      ddl-auto: none
server:
  port: ${PORT:8080}
management.endpoints.web.exposure.include: health,info,metrics,prometheus
```

**How to pick a profile:**

* Locally:

    * `SPRING_PROFILES_ACTIVE=dev ./gradlew :service-api:bootRun`
    * or `./gradlew :service-api:bootRun --args='--spring.profiles.active=dev'`
* Tests: use `@ActiveProfiles("test")` (shown below).
* CI/Prod: set `SPRING_PROFILES_ACTIVE=prod` and pass secrets via env vars.

> Keep secrets **out of git**; use env vars in prod, and a `.env` (ignored) for local.

---

## B. (Optional) `.env` for local dev convenience

Create:

```
# .env (DO NOT COMMIT)
DB_URL=jdbc:postgresql://localhost:5432/devdb
DB_USER=devuser
DB_PASS=devpass
```

Then wire **bootRun** and **test** tasks to read it (no extra library required):

```kotlin
// service-api/build.gradle.kts
import java.util.Properties

fun loadDotEnv(): Map<String, String> =
    rootProject.file(".env").takeIf { it.exists() }?.inputStream()?.use { input ->
        Properties().apply { load(input) }
            .entries.associate { it.key.toString() to it.value.toString() }
    } ?: emptyMap()

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val env = loadDotEnv()
    if (env.isNotEmpty()) {
        environment(env) // exposes DB_URL/DB_USER/DB_PASS etc to the app
    }
}
tasks.withType<Test> {
    val env = loadDotEnv()
    if (env.isNotEmpty()) environment(env)
}
```

Also add a committed template:

```
# .env.example (commit this)
DB_URL=
DB_USER=
DB_PASS=
```

---

# 2) Test strategy (unit, slice, integration)

## Layering

* **Unit tests**: fast, pure JUnit/Mockito; no Spring context.
* **Slice tests**: `@WebMvcTest`, `@DataJpaTest` for focused Spring slices.
* **Integration tests**: full Spring context + **Testcontainers** (real PostgreSQL), separated into `integrationTest` source set.

### Add testing deps in `service-api/build.gradle.kts`

```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") // JUnit 5, AssertJ, Mockito
    testImplementation("org.mockito:mockito-core")
    // Testcontainers (PostgreSQL + JUnit 5)
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
tasks.withType<Test> {
    useJUnitPlatform()
    // Faster local runs
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}
```

---

## A. Unit test example

```java
// service-api/src/test/java/com/example/api/util/PriceCalcTest.java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PriceCalcTest {
    @Test void addsTax() {
        var total = PriceCalc.addTax(100, 0.18);
        assertThat(total).isEqualTo(118);
    }
}
```

No Spring here—fast and deterministic.

---

## B. Slice tests

**Controller slice** with mocked service:

```java
// service-api/src/test/java/com/example/api/web/ProductControllerTest.java
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private ProductService service;

    @Test void getById() throws Exception {
        given(service.findById(1L)).willReturn(new Product(1L, "Laptop"));
        mvc.perform(get("/api/products/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value("Laptop"));
    }
}
```

**JPA slice** (with H2) if you want ultra-fast repo tests:

```kotlin
// build.gradle.kts (service-api)
dependencies {
    testImplementation("com.h2database:h2") // only for @DataJpaTest slice
}
```

```java
// service-api/src/test/java/com/example/api/repo/ProductRepoTest.java
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepoTest {
    @Autowired private ProductRepository repo;

    @Test void savesAndFinds() {
        var saved = repo.save(new Product(null, "Mouse"));
        assertThat(repo.findById(saved.getId())).isPresent();
    }
}
```

---

## C. Integration tests with Testcontainers (recommended)

Run your app against **real PostgreSQL** in Docker, automatically started/stopped for tests.

```java
// service-api/src/test/java/com/example/api/IntegrationTest.java
package com.example.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @Test void contextLoads() { /* smoke check */ }
}
```

This uses the **test** profile. We don’t hardcode DB creds; `DynamicPropertySource` wires the container values into Spring at runtime.

---

## D. Separate integration source set (optional, neat)

If you want `integrationTest` to run after `test` and be visible in reports:

```kotlin
// service-api/build.gradle.kts
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
        resources.srcDir("src/integrationTest/resources")
        java.srcDir("src/integrationTest/java")
    }
}
configurations {
    create("integrationTestImplementation") { extendsFrom(configurations.testImplementation.get()) }
    create("integrationTestRuntimeOnly")   { extendsFrom(configurations.testRuntimeOnly.get()) }
}
dependencies {
    add("integrationTestImplementation", "org.springframework.boot:spring-boot-starter-test")
    add("integrationTestImplementation", "org.testcontainers:junit-jupiter")
    add("integrationTestImplementation", "org.testcontainers:postgresql")
}
val integrationTest by tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}
tasks.named("check") { dependsOn(integrationTest) }
```

Folder layout:

```
service-api/
  src/
    test/java/...                 (unit + slice)
    integrationTest/java/...      (integration)
    integrationTest/resources/...
```

---

# 3) Wiring CI & coverage

## Jacoco coverage (root or per-module)

```kotlin
// root build.gradle.kts (already applied jacoco in subprojects)
subprojects {
    plugins.withId("jacoco") {
        tasks.register<JacocoReport>("jacocoReportAll") {
            dependsOn(tasks.withType<Test>())
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
            classDirectories.setFrom(fileTree("$buildDir/classes/java/main"))
            sourceDirectories.setFrom(files("src/main/java"))
            executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec"))
        }
    }
}
```

Run: `./gradlew test jacocoReportAll`

---

# 4) Practical commands you’ll use

```bash
# Dev run (reading .env if present)
SPRING_PROFILES_ACTIVE=dev ./gradlew :service-api:bootRun

# Unit + slice tests
./gradlew test

# Integration tests (if you created the source set)
./gradlew integrationTest

# Everything
./gradlew clean build

# Prod run (env-driven)
SPRING_PROFILES_ACTIVE=prod DB_URL=... DB_USER=... DB_PASS=... java -jar service-api/build/libs/service-api-*.jar
```

---

## Key takeaways

* Use **profiles** (`dev`, `test`, `prod`) in `application.yml`; prefer env vars for secrets.
* For local convenience, load a **.env** into `bootRun`/`test` tasks.
* Structure tests by **unit → slice → integration**; prefer **Testcontainers** for realistic DB tests.
* (Optional) separate **`integrationTest`** source set and make `check` depend on it.
* Keep CI and prod **profile-driven**, secrets via **env vars**.

---

# 📦 Minimal project layout

```
my-app/
├─ .env
├─ .env.example
├─ build.gradle.kts
└─ src/
   └─ test/
      └─ java/
         └─ com/example/EnvTest.java
```

---

# 🧪 Goal

* Parse `.env` (KEY=VALUE lines) in Gradle.
* Inject those as **environment variables** for the `test` JVM.
* In JUnit, read them via `System.getenv("KEY")` and print/assert.

---

# 1) `.env` files

**`.env` (not committed):**

```
DB_URL=jdbc:postgresql://localhost:5432/devdb
DB_USER=devuser
DB_PASS=devpass
FEATURE_X=true
```

**`.env.example` (commit this as a template):**

```
DB_URL=
DB_USER=
DB_PASS=
FEATURE_X=
```

> Tip: add `.env` to `.gitignore`.

---

# 2) `build.gradle.kts`

This script:

* Applies `java`, `junit` setup.
* Defines a small **`.env` parser** (handles comments, quotes, and `export KEY=`).
* Exposes those entries to the **`test`** task as environment variables.
* Shows `println` output from tests.

```kotlin
plugins {
    id("java")
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    // Show println output from tests
    testLogging { showStandardStreams = true }

    // Load .env into the test process environment
    val envMap = loadDotEnv(rootProject.file(".env"))
    if (envMap.isNotEmpty()) {
        environment(envMap)
        println("Loaded ${envMap.size} .env entries into test environment")
    } else {
        println(".env not found or empty; continuing without it")
    }
}

/**
 * Tiny .env parser:
 *  - ignores blank lines and lines starting with '#'
 *  - supports `export KEY=VALUE`
 *  - trims quotes around values ("value" or 'value')
 */
fun loadDotEnv(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()

    return file.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
            val eq = line.indexOf('=')
            if (eq == -1) return@map null

            var key = line.substring(0, eq).trim()
            var value = line.substring(eq + 1).trim()

            if (key.startsWith("export ")) key = key.removePrefix("export ").trim()
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))
            ) {
                value = value.substring(1, value.length - 1)
            }
            key to value
        }
        .filterNotNull()
        .toMap()
}
```

> Why inject via Gradle?
> Your tests can stay plain JUnit (no extra libs); they read env with `System.getenv`.

---

# 3) The test that prints (and asserts)

**`src/test/java/com/example/EnvTest.java`**

```java
package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {

    @Test
    void printsEnvFromDotEnv() {
        String url  = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        String feat = System.getenv("FEATURE_X");

        System.out.println("DB_URL=" + url);
        System.out.println("DB_USER=" + user);
        System.out.println("DB_PASS=" + (pass != null ? "********" : null));
        System.out.println("FEATURE_X=" + feat);

        // Basic sanity checks (adjust as you like)
        assertNotNull(url,  "DB_URL must be set");
        assertNotNull(user, "DB_USER must be set");
        assertNotNull(pass, "DB_PASS must be set");
    }
}
```

---

# 4) Run it

```bash
./gradlew test
```

You’ll see the `System.out.println` lines because we set `showStandardStreams = true`.

---

## ✅ Notes & options

* **No extra dependencies** needed. Everything is done in Gradle + JUnit.
* The parser handles most common `.env` formats. If you need more features (multiline, variable expansion), you can use a tiny runtime lib:

    * `testImplementation("io.github.cdimascio:dotenv-java:3.0.0")`
    * Then, in tests:

      ```java
      import io.github.cdimascio.dotenv.Dotenv;
      var dotenv = Dotenv.configure().ignoreIfMissing().load();
      String url = dotenv.get("DB_URL");
      ```
    * (In that case you wouldn’t need the Gradle `environment(...)` wiring.)
* Want to **print .env from a Gradle task** too? Add:

  ```kotlin
  tasks.register("printDotEnv") {
      doLast {
          val map = loadDotEnv(rootProject.file(".env"))
          map.forEach { (k, v) -> println("$k=${if (k.contains("PASS")) "********" else v}") }
      }
  }
  # Run: ./gradlew printDotEnv
  ```