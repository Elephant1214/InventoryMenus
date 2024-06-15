import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`
    kotlin("jvm") version("1.9.24")
    kotlin("plugin.serialization") version("1.9.24")
    id("io.papermc.paperweight.userdev") version("1.7.1")
}

group = "me.elephant1214.nogrief"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    paperweight.devBundle("org.purpurmc.purpur", "1.20.6-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
            freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}
