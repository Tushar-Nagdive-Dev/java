// Root build.gradle.kts — conventions only (no Boot/app code here)
plugins {
    id("java")
}

group = "org.phase03"
version = "0.0.1"

// Prefer LTS for Spring Boot 3.5.x
val javaVersion = 21

allprojects {
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        // helpful console logs
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        // Do NOT load .env here; keep tests clean.
        // If you must, do it per-module and be intentional.
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.3"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}

// Example root-level utility tasks (optional)
val prepare by tasks.register("prepare") { doLast { println("Preparing…") } }
val deploy by tasks.register("deploy") {
    dependsOn(prepare)
    doLast { println("Deploying!") }
}
val cleanup by tasks.register("cleanup") { doLast { println("Cleaning temp files…") } }
deploy.finalizedBy(cleanup)

// If you want deploy to run after **service-api** tests, wire it explicitly:
// deploy.mustRunAfter(":service-api:test")
