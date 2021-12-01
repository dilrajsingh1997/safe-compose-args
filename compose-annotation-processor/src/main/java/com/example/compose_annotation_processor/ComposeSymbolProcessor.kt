package com.example.compose_annotation_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class ComposeSymbolProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.example.annotation.ComposeDestination")
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val packageName = "com.example.safecomposeargs"
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = packageName,
            fileName = "GeneratedFunctions"
        )
        file addLine "package $packageName"
        file addLine "import androidx.navigation.*"
        file addLine "import android.net.Uri"
        file addLine "import android.os.Bundle"
        file addLine "import com.google.gson.reflect.TypeToken"
        file addLine ""

        symbols.forEach { it.accept(NavTypeVisitor(file, resolver, logger, options), Unit) }
        symbols.forEach { it.accept(ComposeDestinationVisitor(file, resolver, logger, options), Unit) }

        file.close()
        return symbols.filterNot { it.validate() }.toList()
    }
}
