import com.example.buildSrc.properties.Dependency
import com.example.buildSrc.properties.Dependency.compileSdk
import com.example.buildSrc.properties.appLocalProjectDependencies

plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = Dependency.compileSdk
    buildToolsVersion = Dependency.buildToolVersion

    defaultConfig {
        minSdk = Dependency.minSdk
        targetSdk = Dependency.targetSdk
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "SAMPLE_VALUE", "\"Hello World~!\"")
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            buildConfigField("boolean", "DEBUG_VALUE", "true")
        }
        getByName("release") {
            isDebuggable = false
            buildConfigField("boolean", "DEBUG_VALUE", "false")
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerVersion = "1.5.30"
        kotlinCompilerExtensionVersion = "1.0.3"
    }
    (this as ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>(
        "kotlinOptions"
    ) {
        jvmTarget = "1.8"
        useIR = true
    }
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
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

    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.AndroidX.coreKtx)
    implementation(Dependency.AndroidX.appComat)
    implementation(Dependency.AndroidX.material)
    implementation(Dependency.AndroidX.contstraintlayout)
    testImplementation(Dependency.Test.junit4)

    implementation(project(":annotation"))
    ksp(project(com.example.buildSrc.properties.KspProject.composeBuilder))

    implementation(Dependency.Compose.runtime)
    implementation(Dependency.Compose.material)
    implementation(Dependency.Compose.ui)
    implementation(Dependency.Compose.toolingPreview)
    debugImplementation(Dependency.Compose.tooling)
    implementation(Dependency.Compose.constraintLayout)
    implementation(Dependency.Compose.paging)
    implementation(Dependency.Compose.navigation)
    implementation(Dependency.Compose.uiUtil)

    implementation(Dependency.Accompanist.inset)
    implementation(Dependency.Accompanist.system)
    implementation(Dependency.Accompanist.insetUi)
    implementation(Dependency.Accompanist.navMaterial)
    implementation(Dependency.Accompanist.placeholder)
    implementation(Dependency.Accompanist.permissions)
    implementation(Dependency.Accompanist.pager)
    implementation(Dependency.Accompanist.placeholder)
    implementation(Dependency.Gson.gson)

    appLocalProjectDependencies()
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/"),
    )
}

ksp {
    // Passing an argument to the symbol processor.
    // Change value to "true" in order to apply the argument.
    arg("ignoreGenericArgs", "false")
}
