plugins {
    kotlin("jvm") version "2.3.10"

    id("com.gradleup.shadow") version "9.4.1"
}

group = "com.xspacy"
version = "1.0.0"

repositories {
    mavenCentral()

    gradlePluginPortal()

    maven("https://libraries.minecraft.net")

    maven("https://mvn.xspacy.com/private") {
        credentials {
            username = System.getenv("MAVEN_USERNAME")
            password = System.getenv("MAVEN_PASSWORD")
        }
    }
}

kotlin {
    jvmToolchain(25)
}

tasks {
    shadowJar {
        archiveFileName.set("${project.name}.jar")

        doLast {
            copy {
                from("build/libs/${project.name}.jar")
                into("/Users/viniciusgutierrez/Documents/projeto-x/plugins/")
            }
        }
    }
}

dependencies {
    compileOnly("org.jetbrains.exposed:exposed-core:1.2.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:1.2.0")
    compileOnly("org.jetbrains.exposed:exposed-java-time:1.2.0")
    compileOnly("org.jetbrains.exposed:exposed-migration-core:1.2.0")
    compileOnly("org.jetbrains.exposed:exposed-migration-jdbc:1.2.0")
    compileOnly("org.jetbrains.exposed:spring7-transaction:1.2.0")
    compileOnly("org.jetbrains.exposed:exposed-json:1.2.0")

    compileOnly("org.postgresql:postgresql:42.7.10")

    compileOnly("com.zaxxer:HikariCP:7.0.2")

    compileOnly("redis.clients:jedis:7.4.1")

    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

    compileOnly("com.google.code.gson:gson:2.13.2")

    compileOnly("org.spigotmc:spigot:26.1.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:26.1.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:minecraft-server:26.1.2-R0.1-SNAPSHOT")

    compileOnly(kotlin("stdlib"))

    compileOnly("com.xspacy:core:1.0-SNAPSHOT")
}
