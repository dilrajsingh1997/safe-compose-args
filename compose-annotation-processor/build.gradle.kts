import com.compose.type_safe_args.buildSrc.properties.Dependency

plugins {
    kotlin("jvm")
}

version = "1.0.1"

dependencies {
    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.Ksp.symbolProcessingApi)
}
