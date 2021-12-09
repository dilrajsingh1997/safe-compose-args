import com.compose.type_safe_args.buildSrc.properties.Dependency

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Dependency.Kotlin.stdlib)
    implementation(Dependency.Ksp.symbolProcessingApi)
}
