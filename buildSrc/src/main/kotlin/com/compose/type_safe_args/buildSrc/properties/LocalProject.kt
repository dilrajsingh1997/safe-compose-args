package com.compose.type_safe_args.buildSrc.properties

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers

object KspProject {
    const val composeBuilder = ":compose-annotation-processor"
}

fun DependencyHandlerScope.appLocalProjectDependencies() {
}

fun buildLocalProjects(kClass: KClass<out Any>): HashMap<String, String> {
    val locals = hashMapOf<String, String>()

    for (member in kClass.declaredMembers) {
        locals[member.name] = member.call() as String
    }

    return locals
}

