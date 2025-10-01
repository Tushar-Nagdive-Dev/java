plugins {
    id("java")
}

group = "org.banking"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // --- Testing stack ---
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")     // Core JUnit 5
    testImplementation("org.assertj:assertj-core:3.26.3")     // Fluent assertions

    // (Optional) Mockito if you ever need mocking in service tests
    testImplementation("org.mockito:mockito-core:5.14.1")
}

tasks.test {
    useJUnitPlatform() // Required to run JUnit 5
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}