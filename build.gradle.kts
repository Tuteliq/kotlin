plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.dokka") version "1.9.10"
    `maven-publish`
    signing
}

group = "dev.tuteliq"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor client
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Tuteliq SDK")
                description.set("Official Kotlin SDK for Tuteliq - AI-powered child safety API")
                url.set("https://github.com/TuteliqSDK/kotlin")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("tuteliq")
                        name.set("Tuteliq")
                        email.set("sales@tuteliq.dev")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/TuteliqSDK/kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/TuteliqSDK/kotlin.git")
                    url.set("https://github.com/TuteliqSDK/kotlin")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign> {
    onlyIf { project.hasProperty("signing.gnupg.keyName") }
}
