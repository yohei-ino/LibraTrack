import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("nu.studer.jooq") version "8.2"
    id("org.flywaydb.flyway") version "9.22.3"
}

group = "com.libratrack"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    
    jooqGenerator("org.postgresql:postgresql")
}

jooq {
    version.set("3.19.3")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)
    
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/libratrack"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        properties.add(Property().apply {
                            key = "rootPath"
                            value = "src/main/resources/db/migration"
                        })
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isPojos = true
                        isPojosEqualsAndHashCode = true
                        isPojosToString = true
                    }
                    target.apply {
                        packageName = "com.libratrack.jooq"
                        directory = "src/main/kotlin/com/libratrack/jooq"
                    }
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("com.libratrack.LibratrackApplicationKt")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/libratrack"
    user = "postgres"
    password = "postgres"
} 