plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = com.compose.type_safe_args.buildSrc.properties.Dependency.compileSdk
    buildToolsVersion = com.compose.type_safe_args.buildSrc.properties.Dependency.buildToolVersion

    defaultConfig {
        minSdk = com.compose.type_safe_args.buildSrc.properties.Dependency.minSdk
        targetSdk = com.compose.type_safe_args.buildSrc.properties.Dependency.targetSdk
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("debug")
        getByName("release")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    (this as ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>(
        "kotlinOptions"
    ) {
        jvmTarget = "1.8"
        useIR = true
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(com.compose.type_safe_args.buildSrc.properties.Dependency.Kotlin.stdlib)
    implementation(com.compose.type_safe_args.buildSrc.properties.Dependency.AndroidX.coreKtx)

    implementation(com.compose.type_safe_args.buildSrc.properties.Dependency.Compose.navigation)
    implementation(com.compose.type_safe_args.buildSrc.properties.Dependency.Gson.gson)
}

