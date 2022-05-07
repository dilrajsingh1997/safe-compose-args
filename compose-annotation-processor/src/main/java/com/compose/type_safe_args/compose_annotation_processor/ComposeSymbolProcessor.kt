package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class ComposeSymbolProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val composeDestinations = resolver
            .getSymbolsWithAnnotation("com.compose.type_safe_args.annotation.ComposeDestination")
            .filterIsInstance<KSClassDeclaration>()

        val defaultArguments = resolver
            .getSymbolsWithAnnotation("com.compose.type_safe_args.annotation.HasDefaultValue")
            .filterIsInstance<KSPropertyDeclaration>()

        val argumentProviders = resolver
            .getSymbolsWithAnnotation("com.compose.type_safe_args.annotation.ArgumentProvider")
            .filterIsInstance<KSClassDeclaration>()

        if (defaultArguments.iterator().hasNext()) {
            // round 1- processing the default arguments

            composeDestinations.forEach { composeDestination ->
                val packageName = composeDestination.packageName.asString()

                val propertyMap =
                    getPropertyMap(composeDestination.getAllProperties(), logger, resolver) ?: run {
                        logger.error("invalid argument found")
                        return@forEach
                    }

                var argumentProviderFile: OutputStream? = null

                if (propertyMap.any { it.value.hasDefaultValue }) {
                    argumentProviderFile = codeGenerator.createNewFile(
                        dependencies = Dependencies(
                            false,
                            *resolver.getAllFiles().toList().toTypedArray()
                        ),
                        packageName = packageName,
                        fileName = "I${composeDestination.simpleName.asString()}Provider"
                    )
                }

                if (argumentProviderFile != null) {
                    argumentProviderFile addLine "package $packageName"

                    addImports(argumentProviderFile, propertyMap.values)

                    argumentProviderFile addLine ""

                    composeDestination.accept(
                        DefaultArgumentVisitor(
                            argumentProviderFile,
                            resolver,
                            logger,
                            options,
                            propertyMap
                        ),
                        Unit
                    )
                }

                argumentProviderFile?.dispose()
            }

            return mutableListOf<KSAnnotated>().apply {
                addAll(argumentProviders)
                addAll(composeDestinations)
            }
        } else {
            // round 2- processing the compose annotations

            val argumentProviderMap = mutableMapOf<KSClassDeclaration, KSClassDeclaration>()
            argumentProviders.forEach { argumentProvider ->
                composeDestinations.forEach { composeDestination ->

                    val doesDestinationHaveArgumentProvider = argumentProvider.superTypes.map { it }
                        .any {
                            it.toString() == "I${composeDestination.simpleName.asString()}Provider" &&
                            it.resolve()
                                .declaration
                                .packageName.asString() == composeDestination.packageName.asString()
                        }

                    if (doesDestinationHaveArgumentProvider) {
                        argumentProviderMap[composeDestination] = argumentProvider
                    }
                }
            }

            composeDestinations.forEach { composeDestination ->
                val packageName = composeDestination.packageName.asString()

                val file = codeGenerator.createNewFile(
                    dependencies = Dependencies(
                        false,
                        *resolver.getAllFiles().toList().toTypedArray()
                    ),
                    packageName = packageName,
                    fileName = composeDestination.simpleName.asString()
                )

                val propertyMap = getPropertyMap(composeDestination.getAllProperties(), logger, resolver) ?: run {
                    logger.error("invalid argument found")
                    return@forEach
                }

                val singletonClass: KSClassDeclaration? = composeDestination.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .firstOrNull {
                        it.classKind == ClassKind.OBJECT
                    }

                file addLine "package $packageName"

                file addImport "import androidx.navigation.*"
                file addImport "import android.net.Uri"
                file addImport "import android.os.Bundle"
                file addImport "import com.google.gson.reflect.TypeToken"
                file addImport "import com.compose.type_safe_args.annotation.*"

                addImports(file, propertyMap.values)

                argumentProviderMap[composeDestination]?.qualifiedName?.asString()?.let {
                    file addImport "import $it"
                }

                singletonClass?.qualifiedName?.asString()?.let {
                    file addImport "import $it"
                }

                file addLine ""

                composeDestination.accept(NavTypeVisitor(file, resolver, logger, options, propertyMap), Unit)
                composeDestination.accept(
                    ComposeDestinationVisitor(
                        file,
                        resolver,
                        logger,
                        options,
                        argumentProviderMap,
                        propertyMap,
                        singletonClass
                    ), Unit
                )
                file.dispose()
            }

            return emptyList()
        }
    }
}
