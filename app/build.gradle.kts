plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.kspPlugin)
}

android {
    namespace = "com.sensors.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sensors.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val mapkitApiKey = properties["MAPKIT_API_KEY"]
        buildConfigField("String", "MAPKIT_API_KEY", "\"${mapkitApiKey}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    api(project(":domain"))
    api(project(":data"))
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.maps.mobile)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //DI
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
}