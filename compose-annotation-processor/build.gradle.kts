import com.example.buildSrc.properties.Dependency

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.Ksp.symbolProcessingApi)
}
