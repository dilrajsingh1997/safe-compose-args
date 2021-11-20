package com.example.buildSrc.properties

object Dependency {
    const val kotlinVersion = "1.5.30"
    const val minSdk = 21
    const val compileSdk = 31
    const val targetSdk = 31
    const val buildToolVersion = "30.0.3"
    const val kspVersion = "1.5.30-1.0.0"

    private const val moshiVersion = "1.11.0"

    object ClassPath {
        const val androidGradle = "com.android.tools.build:gradle:7.0.2"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        const val ksp = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.3.2"
        const val appComat = "androidx.appcompat:appcompat:1.2.0"

        const val material = "com.google.android.material:material:1.3.0"
        const val contstraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    }

    object Moshi {
        const val lib = "com.squareup.moshi:moshi:$moshiVersion"
        const val compiler = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
    }

    object Ksp {
        const val symbolProcessingApi = "com.google.devtools.ksp:symbol-processing-api:$kspVersion"
    }

    object Compose {
        const val snapshot = ""
        const val version = "1.0.3"
        const val constraintLayoutVersion = "1.0.0-beta02"
        const val pagingVersion = "1.0.0-alpha13"
        const val navigationVersion = "2.4.0-alpha10"
        const val uiUtilVersion = "1.1.0-alpha05"

        @get:JvmStatic
        val snapshotUrl: String
            get() = "https://androidx.dev/snapshots/builds/$snapshot/artifacts/repository/"

        const val foundation = "androidx.compose.foundation:foundation:$version"
        const val layout = "androidx.compose.foundation:foundation-layout:$version"
        const val material = "androidx.compose.material:material:$version"
        const val materialIconsCore = "androidx.compose.material:material-icons-core:$version"
        const val materialIconsExtended =
            "androidx.compose.material:material-icons-extended:$version"
        const val runtime = "androidx.compose.runtime:runtime:$version"
        const val runtimeLivedata = "androidx.compose.runtime:runtime-livedata:$version"
        const val ui = "androidx.compose.ui:ui:$version"
        const val tooling = "androidx.compose.ui:ui-tooling:$version"
        const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
        const val test = "androidx.compose.test:test-core:$version"
        const val uiTest = "androidx.compose.ui:ui-test:$version"
        const val junit = "androidx.compose.ui:ui-test-junit4:$version"

        const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:$constraintLayoutVersion"
        const val paging = "androidx.paging:paging-compose:$pagingVersion"
        const val navigation = "androidx.navigation:navigation-compose:$navigationVersion"
        const val uiUtil = "androidx.compose.ui:ui-util:$uiUtilVersion"
        const val activityCompose = "androidx.activity:activity-compose:$version"
        const val animation = "androidx.compose.animation:animation:$version"
        const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
    }

    object Accompanist {
        const val version = "0.19.0"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
        const val inset = "com.google.accompanist:accompanist-insets:$version"
        const val insetUi = "com.google.accompanist:accompanist-insets-ui:$version"
        const val system = "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val navMaterial = "com.google.accompanist:accompanist-navigation-material:$version"
        const val navAnim = "com.google.accompanist:accompanist-navigation-animation:$version"
        const val placeholder = "com.google.accompanist:accompanist-placeholder-material:$version"
        const val flowLayout = "com.google.accompanist:accompanist-flowlayout:$version"
        const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
        const val coil = "com.google.accompanist:accompanist-coil:$version"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
    }

    object Test {
        const val junit4 = "junit:junit:4.13.1"
    }
}