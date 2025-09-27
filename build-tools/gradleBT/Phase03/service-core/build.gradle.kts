plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.6"
}

dependencies {
    implementation(project(":common"))

    // Business logic with Spring (no Boot plugin here)
//    implementation("org.springframework:spring-context")
//    implementation("org.springframework.data:spring-data-jpa")

    // JPA API + annotations
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // (Optional) mapping / utils
//    implementation("org.modelmapper:model mapper:3.2.0")

    // Database driver only needed at runtime (tests/integration)
    runtimeOnly("org.postgresql:postgresql:42.7.4")
}
