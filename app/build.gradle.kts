import com.compose.type_safe_args.buildSrc.properties.Dependency
import com.compose.type_safe_args.buildSrc.properties.Dependency.compileSdk
import com.compose.type_safe_args.buildSrc.properties.appLocalProjectDependencies

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
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {

    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.AndroidX.coreKtx)
    implementation(Dependency.AndroidX.appComat)
    implementation(Dependency.AndroidX.material)
    implementation(Dependency.AndroidX.contstraintlayout)
    testImplementation(Dependency.Test.junit4)

    implementation(project(":annotation"))
    ksp(project(com.compose.type_safe_args.buildSrc.properties.KspProject.composeBuilder))

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

androidComponents.onVariants { variant ->
    kotlin.sourceSets.findByName(variant.name)?.kotlin?.srcDirs(
        file("$buildDir/generated/ksp/${variant.name}/kotlin")
    )
}

ksp {
    // Passing an argument to the symbol processor.
    // Change value to "true" in order to apply the argument.
    arg("ignoreGenericArgs", "false")
}
