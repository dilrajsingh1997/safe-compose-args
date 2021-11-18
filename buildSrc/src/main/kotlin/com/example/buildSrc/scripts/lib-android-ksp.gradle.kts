plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}


android {
    buildTypes {
        getByName("debug") {
            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/debug/kotlin"))
                }
            }
        }
        getByName("release") {
            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/release/kotlin"))
                }
            }

        }
    }
}

ksp {
    arg("projectName", project.name)
}