import com.example.buildSrc.properties.Dependency

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":annotation"))
    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.Ksp.symbolProcessingApi)
}
