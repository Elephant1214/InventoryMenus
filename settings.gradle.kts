rootProject.name = "InventoryMenus"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version("2.1.10")
        id("io.papermc.paperweight.userdev") version("2.0.0-beta.14")
    }
}