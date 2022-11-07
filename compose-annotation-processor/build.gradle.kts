import com.compose.type_safe_args.buildSrc.properties.Dependency

plugins {
    kotlin("jvm")
}

version = "1.0.6"

dependencies {
    api(Dependency.Kotlin.stdlib)
    api(Dependency.Ksp.symbolProcessingApi)
}

ext {
    set("PUBLISH_VERSION", "$version")
    set("PUBLISH_ARTIFACT_ID", "compose-annotation-processor")
}

apply {
    from("${rootProject.projectDir}/scripts/publish-module.gradle")
}
