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
    (this as ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>("kotlinOptions") {
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

    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.material:material:1.0.5")
    implementation("androidx.compose.animation:animation:1.0.5")
    implementation("androidx.compose.ui:ui-tooling:1.0.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.5")

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
