# 📌 Phase 1 – Gradle Foundations

## 1. What is Gradle?

* **Gradle** is a **build automation tool** that can handle compiling code, running tests, packaging JAR/WARs, publishing to repos, and integrating with CI/CD pipelines.
* It’s **flexible** (scriptable in Groovy or Kotlin), **fast** (incremental builds, caching), and **widely used** (esp. with Java, Kotlin, Android, Spring Boot).

---

## 2. Installing & Setup

* Install via:

    * **SDKMAN (Linux/Mac):** `sdk install gradle 8.10`
    * **Windows:** Download ZIP and add `bin` to PATH
* Always use **Gradle Wrapper** (`gradlew` and `gradlew.bat`) for consistency across teams:

  ```bash
  ./gradlew --version
  ```

  This ensures everyone uses the same Gradle version defined in `gradle-wrapper.properties`.

---

## 3. Project Structure

When you run `gradle init` and choose a simple Java app:

```
my-app/
 ├── gradlew
 ├── gradlew.bat
 ├── settings.gradle(.kts)
 ├── build.gradle(.kts)
 ├── src/
 │   ├── main/java/com/example/App.java
 │   └── test/java/com/example/AppTest.java
 └── gradle/
     └── wrapper/gradle-wrapper.properties
```

---

## 4. Minimal Build File

Here’s a **Hello World Gradle project** in both DSLs:

### 🔹 `build.gradle` (Groovy DSL)

```groovy
plugins {
    id 'java'
}

group = 'com.example'
version = '1.0.0'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.apache.commons:commons-lang3:3.14.0'
    testImplementation 'junit:junit:4.13.2'
}

test {
    useJUnitPlatform()
}
```

### 🔹 `build.gradle.kts` (Kotlin DSL)

```kotlin
plugins {
    id("java")
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.14.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnitPlatform()
}
```

✅ Both do the same thing:

* Apply the **Java plugin**
* Define **group** & **version**
* Use **Maven Central repo**
* Add **dependencies** (commons-lang3 + JUnit)
* Configure **JUnit tests**

---

## 5. Running Builds

```bash
./gradlew build       # Compiles, tests, and packages
./gradlew clean       # Cleans build/ folder
./gradlew test        # Runs tests
./gradlew tasks       # Lists available tasks
```

💡 **Pro Tip:** Always use `./gradlew` (wrapper) instead of system-installed `gradle`.

---

## 6. Key Takeaways (Phase 1)

* Gradle has **three phases**: Initialization → Configuration → Execution.
* You define **tasks** and Gradle executes them.
* You can write scripts in **Groovy DSL** or **Kotlin DSL** (same functionality, different syntax).
* **Wrapper is king** – ensures reproducible builds.

# 🔹 Why does Gradle have **two DSLs** (Groovy & Kotlin)?

Gradle originally started with **Groovy DSL (`build.gradle`)**:

* Groovy is a dynamic language → flexible, less boilerplate.
* Syntax is short and expressive.
* But: lacks type safety, IDE autocomplete is limited.

Later, Gradle introduced **Kotlin DSL (`build.gradle.kts`)**:

* Kotlin is **statically typed** → full **IDE support**, **refactoring**, **autocompletion**.
* Helps reduce runtime errors (invalid method/property detected at compile time).
* Encourages **modern, safer builds**.

👉 **Which to choose?**

* **Existing projects** → stick to Groovy (easier migration, most docs/examples are in Groovy).
* **New projects** → prefer Kotlin DSL (better tooling, future-proof, especially for large-scale builds).

💡 Professional Tip: Learn **both** (so you can read/maintain legacy Groovy projects and write new ones in Kotlin).

---

# 🔹 What are **Tasks** in Gradle?

Think of **tasks** as the *smallest unit of work* in a Gradle build.

Examples of tasks:

* `compileJava` → compiles Java code
* `test` → runs unit tests
* `jar` → packages JAR file
* `clean` → deletes the `build/` folder

Each **task** can:

* Do something (e.g., compile, copy, zip, deploy).
* Depend on other tasks (`dependsOn`).
* Be customized.

👉 Example: Custom Task

### Groovy DSL

```groovy
task hello {
    doLast {
        println 'Hello, Gradle!'
    }
}
```

### Kotlin DSL

```kotlin
tasks.register("hello") {
    doLast {
        println("Hello, Gradle!")
    }
}
```

Run with:

```bash
./gradlew hello
```

---

# 🔹 Why & How do we **Script in Gradle**?

Gradle is not just XML config (like Maven) → it’s a **scripting environment**.
This makes it **powerful** because you can:

1. **Automate repetitive tasks**
   Example: Copy frontend build files into Spring Boot `resources/static/`:

   ```groovy
   task copyFrontend(type: Copy) {
       from '../frontend/dist/'
       into 'src/main/resources/static/'
   }
   ```

2. **Customize build logic**

    * Change build behavior per environment (Dev/QA/Prod).
    * Add conditional logic in scripts.

3. **Extend with Plugins**

    * Gradle scripts can **apply plugins** (like Spring Boot, Docker, Kubernetes).
    * Example: Packaging Spring Boot app into Docker with a plugin.

👉 **Usage in real projects**:

* Instead of manual steps (`javac`, `jar`, copy files, run tests), you define scripts → Gradle automates end-to-end.
* In CI/CD, you just call:

  ```bash
  ./gradlew clean build publish
  ```

---

# 🔹 How to Use Gradle Scripts in Practice?

1. **Default tasks** → Already available from plugins (`build`, `test`, etc.).
2. **Custom tasks** → You write them to automate your team’s workflows.
3. **Reusable logic** → Extract to `gradle/*.gradle` files or even custom plugins for bigger teams.

---

# ✅ Key Takeaways

* **Two DSLs** exist because Groovy = legacy & flexible, Kotlin = modern & type-safe.
* **Tasks** are the building blocks → each does some unit of work.
* **Scripts** turn Gradle into a powerful automation engine, not just a config file.
* You **use scripts** when you want Gradle to do more than compile → like running Docker, moving files, publishing builds, or integrating frontend + backend.

---

# 📌 Phase 2 – Dependencies & Plugins

## 1. 🔹 Repositories

Where Gradle **fetches libraries** from.

### Groovy DSL

```groovy
repositories {
    mavenCentral()
    google() // used in Android projects
    maven { url "https://jitpack.io" } // custom repo
}
```

### Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}
```

💡 **Pro Tip:**
Always keep dependencies in **Maven Central** if possible (more reliable than JCenter, which is deprecated).

---

## 2. 🔹 Dependencies

Dependencies are grouped into **configurations** (scopes).

### Common Configurations

* `implementation` → normal dependency (not exposed to consumers).
* `api` → dependency exposed to consumers (used in libraries).
* `compileOnly` → available only at compile time (e.g., Lombok).
* `runtimeOnly` → available only at runtime (e.g., JDBC drivers).
* `testImplementation` → dependencies for testing (JUnit, Mockito).

### Example

#### Groovy DSL

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web:3.2.0'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'

    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'

    runtimeOnly 'org.postgresql:postgresql:42.7.3'

    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testImplementation 'org.mockito:mockito-core:5.10.0'
}
```

#### Kotlin DSL

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    runtimeOnly("org.postgresql:postgresql:42.7.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.10.0")
}
```

---

## 3. 🔹 Plugins

Plugins extend Gradle with extra tasks & conventions.

### Built-in Plugins

* `java` → compiles Java
* `application` → creates runnable apps
* `war` → builds WAR files
* `maven-publish` → publish artifacts

### Example

#### Groovy DSL

```groovy
plugins {
    id 'java'
    id 'application'
}

application {
    mainClass = 'com.example.MainApp'
}
```

#### Kotlin DSL

```kotlin
plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("com.example.MainApp")
}
```

---

## 4. 🔹 Real-World Example (Spring Boot + PostgreSQL)

Here’s how you’d set up a **Spring Boot app** with Gradle:

### Groovy DSL

```groovy
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Kotlin DSL

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

Run:

```bash
./gradlew bootRun
```

---

## 5. ✅ Key Takeaways (Phase 2)

* **Repositories** = where dependencies come from.
* **Dependencies** = different scopes for compile/test/runtime.
* **Plugins** = extend Gradle (Spring Boot, Docker, Kubernetes, etc.).
* You can write **Groovy or Kotlin DSL** – functionality is the same.