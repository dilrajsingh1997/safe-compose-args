import com.compose.type_safe_args.buildSrc.properties.Dependency
import com.compose.type_safe_args.buildSrc.properties.Dependency.compileSdk
import com.compose.type_safe_args.buildSrc.properties.appLocalProjectDependencies

plugins {
    id("com.android.library")
    kotlin("android")
}

android {

}


dependencies {

    Dependency.Kotlin.run {
        implementation(stdlib)
    }

    Dependency.Test.run {
        testImplementation(junit4)
    }
}