package com.example.compose_annotation_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class ComposeSymbolProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private var tabs = 0

    infix fun OutputStream.addLine(line: String) {
        this.write("\n".toByteArray())
        repeat((1..tabs).count()) {
            this.write("\t".toByteArray())
        }
        this.write(line.toByteArray())
    }

    infix fun OutputStream.addPhrase(line: String) {
        this.write(line.toByteArray())
    }

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

        symbols.forEach { it.accept(Visitor(file, resolver), Unit) }

        file.close()

        return symbols.filterNot { it.validate() }.toList()
    }

    inner class Visitor(private val file: OutputStream, private val resolver: Resolver) :
        KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val annotation: KSAnnotation = classDeclaration.annotations.first {
                it.shortName.asString() == "ComposeDestination"
            }

            val nameArgument: KSValueArgument = annotation.arguments
                .first { arg -> arg.name?.asString() == "route" }

            val route = nameArgument.value as String

            val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()
            var propertyCount = 0

            properties.forEach { propertyCount ++ }

            val className = route.replaceFirstChar { it.uppercaseChar() }
            val dataClassName = "${className}Args"

            file addLine "class ${className}Destination {"
            tabs++

            if (propertyCount > 0) {
                file addLine "data class $dataClassName ("
                tabs++

                properties.forEach { property ->
                    val argumentName = property.simpleName.asString()
                    val resolvedType: KSType = property.type.resolve()
                    file addLine "val $argumentName: "
                    file addPhrase (resolvedType.declaration.qualifiedName?.asString() ?: run {
                        logger.error("Invalid property type", property)
                        return
                    })
                    file addPhrase if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""
                    file addPhrase ", "
                }

                tabs--
                file addLine ")"
            }

            file addLine "companion object {"
            tabs ++

            if (propertyCount > 0) {
                file addLine "fun parseArguments(backStackEntry: NavBackStackEntry): $dataClassName {"
                tabs++

                file addLine "return "
                file addPhrase "$dataClassName("
                tabs++

                properties.forEach { property ->
                    val argumentName = property.simpleName.asString()
                    val resolvedType: KSType = property.type.resolve()

                    fun getParsedElement(): String {
                        return try {
                            when (resolver.getClassDeclarationByName(resolvedType.declaration.qualifiedName!!)
                                ?.toString()) {
                                "Boolean" -> "backStackEntry.arguments?.getBoolean(\"$argumentName\") ?: false"
                                "String" -> "backStackEntry.arguments?.getString(\"$argumentName\") ?: \"\""
                                "Float" -> "backStackEntry.arguments?.getFloat(\"$argumentName\") ?: 0F"
                                "Int" -> "backStackEntry.arguments?.getInt(\"$argumentName\") ?: 0"
                                "Long" -> "backStackEntry.arguments?.getLong(\"$argumentName\") ?: 0L"
                                else -> ""
                            }
                        } catch (e: Exception) {
                            ""
                        }
                    }

                    file addLine "$argumentName = ${getParsedElement()}"
                    file addPhrase ", "
                }

                tabs--
                file addLine ")"

                tabs--
                file addLine "}"
            }

            var argumentString = ""
            var count = 0
            file addLine "val argumentList"
            file addPhrase ": MutableList<NamedNavArgument> "
            tabs ++
            file addLine "get() = mutableListOf("
            count = 0
            properties.forEach { property ->
                count ++
                val argumentName = property.simpleName.asString()
                val resolvedType: KSType = property.type.resolve()

//                logger.error(resolver.getClassDeclarationByName(resolvedType.declaration.qualifiedName!!), null)

                fun getElementNavType(): String {
                    return try {
                        when (resolver.getClassDeclarationByName(resolvedType.declaration.qualifiedName!!)
                            ?.toString()) {
                            "Boolean" -> "NavType.BoolType"
                            "String" -> "NavType.StringType"
                            "Float" -> "NavType.FloatType"
                            "Int" -> "NavType.IntType"
                            "Long" -> "NavType.LongType"
                            else -> ""
                        }
                    } catch (e: Exception) {
                        ""
                    }
                }

                tabs ++
                file addLine "navArgument(\"$argumentName\") {"
                tabs ++
                file addLine "type = ${getElementNavType()}"
                tabs --
                file addLine "},"
                tabs --

                argumentString += "$argumentName={$argumentName}"
                if (count != propertyCount) {
                    argumentString += ","
                }
            }
            file addLine ")"
            tabs --
            file addLine "fun getDestination("
            properties.forEach { property ->
                val argumentName = property.simpleName.asString()
                val resolvedType: KSType = property.type.resolve()
                file addPhrase "$argumentName: "
                file addPhrase (resolvedType.declaration.qualifiedName?.asString() ?: run {
                    logger.error("Invalid property type", property)
                    return
                })
                file addPhrase if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""
                file addPhrase ", "
            }
            file addPhrase "): String {"
            tabs ++

            file addLine "return \"$route${if (propertyCount > 0) "?" else ""}\" + "
            tabs ++
            tabs ++
            count = 0
            properties.forEach { property ->
                count ++
                val argumentName = property.simpleName.asString()

                file addLine "\"$argumentName="
                file addPhrase "$$argumentName"
                if (count == propertyCount) {
                    file addPhrase "\""
                } else {
                    file addPhrase ",\""
                }
                file addPhrase " + "
            }
            file addLine "\"\""
            tabs --
            tabs --

            tabs --
            file addLine "}"
            file addLine "val route = \"$route"
            if (argumentString.isNotEmpty()) {
                file addPhrase "?"
                file addPhrase argumentString
            }
            file addPhrase "\""
            tabs --
            file addLine "}"
            tabs--
            file addLine "}"
        }

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val argumentName = property.simpleName.asString()
            file addLine "\t$argumentName: "

            val resolvedType: KSType = property.type.resolve()
            file addLine (resolvedType.declaration.qualifiedName?.asString() ?: run {
                logger.error("Invalid property type", property)
                return
            })
            file addLine if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

            val genericArguments: List<KSTypeArgument> =
                property.type.element?.typeArguments ?: emptyList()
            visitTypeArguments(genericArguments)

            file addLine ","
        }

        private fun visitTypeArguments(typeArguments: List<KSTypeArgument>) {
            if (typeArguments.isNotEmpty()) {
                file addLine "<"
                typeArguments.forEachIndexed { i, arg ->
                    visitTypeArgument(arg, data = Unit)
                    if (i < typeArguments.lastIndex) file addLine ", "
                }
                file addLine ">"
            }
        }

        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
            if (options["ignoreGenericArgs"] == "true") {
                file addLine "*"
                return
            }

            when (val variance: Variance = typeArgument.variance) {
                Variance.STAR -> {
                    file addLine "*"
                    return
                }
                Variance.COVARIANT, Variance.CONTRAVARIANT -> {
                    file addLine variance.label
                    file addLine " "
                }
                Variance.INVARIANT -> {
                    // do nothing
                }
            }
            val resolvedType: KSType? = typeArgument.type?.resolve()
            file addLine (resolvedType?.declaration?.qualifiedName?.asString() ?: run {
                logger.error("Invalid type argument", typeArgument)
                return
            })
            file addLine if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

            val genericArguments: List<KSTypeArgument> =
                typeArgument.type?.element?.typeArguments ?: emptyList()
            visitTypeArguments(genericArguments)
        }
    }
}
