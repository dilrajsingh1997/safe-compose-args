plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

// separated area by buildSrc
// these should be same as Dependency
val kotlinVersion = "1.5.30"
val kspVersion = "1.5.30-1.0.0"

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = kotlinVersion
}


repositories {
    google()
    jcenter()
}

dependencies {

    // hard-coded area
    implementation("com.android.tools.build:gradle:7.2.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
}