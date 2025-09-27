plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.6"
}

dependencies {
    // Example shared utilities / DTO helpers
    api("org.apache.commons:commons-lang3:3.18.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // (Optional) validation annotations used across modules
    api("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    api("jakarta.validation:jakarta.validation-api:3.1.0")
}