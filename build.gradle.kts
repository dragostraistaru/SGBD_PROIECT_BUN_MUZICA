plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("org.example.sgbd_proiect_bun_muzica.Launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")

    // PostgreSQL driver
    implementation("org.postgresql:postgresql:42.7.8")

    // Hibernate ORM + JPA
    implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // HikariCP - Connection Pooling
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.12")

    implementation("org.hibernate.orm:hibernate-hikaricp:6.4.4.Final")
}

tasks.withType<Test> {
    useJUnitPlatform()
}