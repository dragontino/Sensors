import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kspPlugin) apply false
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}

ext {
    properties["MAPKIT_API_KEY"] = getMapkitApiKey()
}


fun getMapkitApiKey(): String {
    val properties = Properties()
    project.file("local.properties").inputStream().let(properties::load)
    return properties.getProperty("MAPKIT_API_KEY", "")
}