plugins {
    id("custom-lib")
    id("com.google.devtools.ksp")
    kotlin("kapt")
}

android {
    compileSdk = com.example.buildSrc.properties.Dependency.compileSdk
    buildToolsVersion = com.example.buildSrc.properties.Dependency.buildToolVersion
}

dependencies {
    ksp(project(com.example.buildSrc.properties.KspProject.composeBuilder))
}
