/**
 * Precompiled [lib-android-ksp.gradle.kts][Lib_android_ksp_gradle] script plugin.
 *
 * @see Lib_android_ksp_gradle
 */
class LibAndroidKspPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Lib_android_ksp_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
