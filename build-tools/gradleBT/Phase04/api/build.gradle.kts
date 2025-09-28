plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    application
}

application { mainClass.set("org.phase04.api.Phase04ApiApplication") }

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // runtime driver for real DBs if you use one outside tests
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // tests (HSQLDB in-memory)
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
    val testImplementation by getting
    val testRuntimeOnly by getting
    named("integrationTestImplementation") { extendsFrom(testImplementation) }
    named("integrationTestRuntimeOnly")   { extendsFrom(testRuntimeOnly) }
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
