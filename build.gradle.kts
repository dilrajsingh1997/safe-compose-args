buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        // these should be full path in root build.gradle.kts
        classpath(com.compose.type_safe_args.buildSrc.properties.Dependency.ClassPath.androidGradle)
        classpath(com.compose.type_safe_args.buildSrc.properties.Dependency.ClassPath.kotlin)
        classpath(com.compose.type_safe_args.buildSrc.properties.Dependency.ClassPath.ksp)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

apply {
    from("${rootDir}/scripts/publish-root.gradle")
}
