import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    application
}

group = "org.phase03"
version = "0.0.1"

application {
    mainClass.set("org.phase03.api.Phase03ApiApplication")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } // keep consistent with root
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":common"))
    implementation(project(":service-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("org.postgresql:postgresql:42.7.4")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.13.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Force Spring tests to the 'test' profile (clean isolation)
    systemProperty("spring.profiles.active", "test")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// --- Local dev convenience: load .env ONLY for bootRun (not for tests) ---
fun loadDotEnv(file: File = rootProject.file(".env")): Map<String, String> {
    if (!file.exists()) return emptyMap()
    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val i = line.indexOf('=')
            if (i == -1) null else {
                var k = line.substring(0, i).trim()
                var v = line.substring(i + 1).trim()
                if (k.startsWith("export ")) k = k.removePrefix("export ").trim()
                if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                    v = v.substring(1, v.length - 1)
                }
                k to v
            }
        }.toMap()
}

tasks.named<BootRun>("bootRun") {
    // Activate 'dev' unless overridden
    systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active") ?: "dev")

    val envMap = loadDotEnv()
    if (envMap.isNotEmpty()) {
        environment(envMap)
        println("bootRun: loaded ${envMap.size} vars from .env")
    } else {
        println("bootRun: .env not found or empty")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")

    // Allow override: -Penvfile=.env
    val envFileName = (findProperty("envfile") as String?) ?: ".env.test"
    val envFile = rootProject.file(envFileName)
    val envMap = loadDotEnv(envFile)

    if (envMap.isNotEmpty()) {
        environment(envMap)
        println("test: loaded ${envMap.size} vars from ${envFile.name}")
    } else {
        println("test: env file '$envFileName' not found or empty; continuing without it")
    }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
// Boot produces bootJar; keep default (plain jar disabled). Enable if needed:
// tasks.named<Jar>("jar") { enabled = true }
