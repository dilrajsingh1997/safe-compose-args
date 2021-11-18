import com.example.buildSrc.properties.Dependency
import com.example.buildSrc.properties.Dependency.compileSdk
import com.example.buildSrc.properties.appLocalProjectDependencies

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