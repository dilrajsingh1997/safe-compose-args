package com.example.compose_annotation_processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import java.io.OutputStream

class ComposeDestinationVisitor(private val file: OutputStream, private val resolver: Resolver, private val logger: KSPLogger, private val options: Map<String, String>, ) :
    KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {

        val annotation: KSAnnotation = classDeclaration.annotations.first {
            it.shortName.asString() == "ComposeDestination"
        }

        val nameArgument: KSValueArgument = annotation.arguments
            .first { arg -> arg.name?.asString() == "route" }

        val route = nameArgument.value as String
        val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()

        val propertyMap = getPropertyMap(properties, logger, resolver) ?: run {
            logger.error("invalid argument found")
            return
        }

        val className = route.replaceFirstChar { it.uppercaseChar() }
        val dataClassName = "${className}Args"

        file addLine "class ${className}Destination {"
        tabs++

        if (propertyMap.isNotEmpty()) {
            file addLine "data class $dataClassName ("
            tabs++

            properties.forEach { property ->
                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                file addLine "val ${propertyInfo.propertyName}: "
                addVariableType(file, propertyInfo)
                file addPhrase ", "
            }

            tabs--
            file addLine ")"
        }

        file addLine "companion object {"
        tabs++

        if (propertyMap.isNotEmpty()) {
            file addLine "fun parseArguments(backStackEntry: NavBackStackEntry): $dataClassName {"
            tabs++

            file addLine "return "
            file addPhrase "$dataClassName("
            tabs++

            properties.forEach { property ->
                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                fun getParsedElement() {
                    when (propertyInfo.composeArgumentType) {
                        ComposeArgumentType.BOOLEAN -> file addPhrase "backStackEntry.arguments?.getBoolean(\"$argumentName\") ?: false"
                        ComposeArgumentType.STRING -> file addPhrase "backStackEntry.arguments?.getString(\"$argumentName\") ?: \"\""
                        ComposeArgumentType.FLOAT -> file addPhrase "backStackEntry.arguments?.getFloat(\"$argumentName\") ?: 0F"
                        ComposeArgumentType.INT -> file addPhrase "backStackEntry.arguments?.getInt(\"$argumentName\") ?: 0"
                        ComposeArgumentType.LONG -> file addPhrase "backStackEntry.arguments?.getLong(\"$argumentName\") ?: 0L"
                        ComposeArgumentType.INT_ARRAY -> file addPhrase "backStackEntry.arguments?.getIntArray(\"$argumentName\") ?: intArrayOf()"
                        ComposeArgumentType.BOOLEAN_ARRAY -> file addPhrase "backStackEntry.arguments?.getBooleanArray(\"$argumentName\") ?: booleanArrayOf()"
                        ComposeArgumentType.LONG_ARRAY -> file addPhrase "backStackEntry.arguments?.getLongArray(\"$argumentName\") ?: longArrayOf()"
                        ComposeArgumentType.FLOAT_ARRAY -> file addPhrase "backStackEntry.arguments?.getFloatArray(\"$argumentName\") ?: floatArrayOf()"
                        ComposeArgumentType.STRING_ARRAY -> {
                            // todo
                        }
                        ComposeArgumentType.PARCELABLE -> {
                            file addPhrase "backStackEntry.arguments?.getParcelable<"
                            addVariableType(file, propertyInfo)
                            file addPhrase ">(\"$argumentName\") ?: throw NullPointerException(\"parcel value not found\")"
                        }
                        ComposeArgumentType.PARCELABLE_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getParcelableArrayList"
                            visitChildTypeArguments(propertyInfo.typeArguments)
                            file addPhrase "(\"$argumentName\")"
                            file addPhrase " ?: throw NullPointerException(\"parcel value not found\")"
                        }
                        ComposeArgumentType.SERIALIZABLE -> {
                            file addPhrase "backStackEntry.arguments?.getSerializable"
                            file addPhrase "(\"$argumentName\") as? "
                            addVariableType(file, propertyInfo)
                            file addPhrase " ?: throw NullPointerException(\"parcel value not found\")"
                        }
                    }
                }

                file addLine "$argumentName = "
                getParsedElement()
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
        tabs++
        file addLine "get() = mutableListOf("
        count = 0
        properties.forEach { property ->
            count++

            val propertyInfo = propertyMap[property] ?: run {
                logger.error("Invalid type argument", property)
                return
            }
            val argumentName = propertyInfo.propertyName

            fun getElementNavType(): String {
                return when (propertyInfo.composeArgumentType) {
                    ComposeArgumentType.BOOLEAN -> "NavType.BoolType"
                    ComposeArgumentType.STRING -> "NavType.StringType"
                    ComposeArgumentType.FLOAT -> "NavType.FloatType"
                    ComposeArgumentType.INT -> "NavType.IntType"
                    ComposeArgumentType.LONG -> "NavType.LongType"
                    ComposeArgumentType.INT_ARRAY -> "IntArrayType"
                    ComposeArgumentType.BOOLEAN_ARRAY -> "BoolArrayType"
                    ComposeArgumentType.FLOAT_ARRAY -> "FloatArrayType"
                    ComposeArgumentType.LONG_ARRAY -> "LongArrayType"
                    else -> {
                        "${className}_${propertyInfo.propertyName.replaceFirstChar { it.uppercase() }}NavType"
                    }
                }
            }

            tabs++
            file addLine "navArgument(\"$argumentName\") {"
            tabs++
            file addLine "type = ${getElementNavType()}"
            tabs--
            file addLine "},"
            tabs--

            argumentString += "$argumentName={$argumentName}"
            if (count != propertyMap.size) {
                argumentString += ","
            }
        }
        file addLine ")"
        tabs--
        file addLine "fun getDestination("
        properties.forEach { property ->

            val propertyInfo = propertyMap[property] ?: run {
                logger.error("Invalid type argument", property)
                return
            }
            val argumentName = propertyInfo.propertyName

            file addPhrase "$argumentName: "
            addVariableType(file, propertyInfo)
            file addPhrase ", "
        }
        file addPhrase "): String {"
        tabs++

        file addLine "return \"$route${if (propertyMap.isNotEmpty()) "?" else ""}\" + "
        tabs++
        tabs++
        count = 0
        properties.forEach { property ->
            count++

            val propertyInfo = propertyMap[property] ?: run {
                logger.error("Invalid type argument", property)
                return
            }
            val argumentName = propertyInfo.propertyName

            file addLine "\"$argumentName="

            file addPhrase when (propertyInfo.composeArgumentType) {
                ComposeArgumentType.INT,
                ComposeArgumentType.BOOLEAN,
                ComposeArgumentType.LONG,
                ComposeArgumentType.FLOAT,
                ComposeArgumentType.STRING -> "$$argumentName"
                else -> "\${Uri.encode(gson.toJson($argumentName))}"
            }
            if (count == propertyMap.size) {
                file addPhrase "\""
            } else {
                file addPhrase ",\""
            }
            file addPhrase " + "
        }
        file addLine "\"\""
        tabs--
        tabs--

        tabs--
        file addLine "}"
        file addLine "val route = \"$route"
        if (argumentString.isNotEmpty()) {
            file addPhrase "?"
            file addPhrase argumentString
        }
        file addPhrase "\""
        tabs--
        file addLine "}"
        tabs--
        file addLine "}"
    }

    private fun visitChildTypeArguments(typeArguments: List<KSTypeArgument>) {
        if (typeArguments.isNotEmpty()) {
            file addPhrase "<"
            typeArguments.forEachIndexed { i, arg ->
                visitTypeArgument(arg, data = Unit)
                if (i < typeArguments.lastIndex) file addLine ", "
            }
            file addPhrase ">"
        }
    }

    private fun addVariableType(file: OutputStream, propertyInfo: PropertyInfo) {
        file addPhrase propertyInfo.resolvedClassQualifiedName
        file addPhrase if (propertyInfo.isNullable) "?" else ""
        visitChildTypeArguments(propertyInfo.typeArguments)
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
        if (options["ignoreGenericArgs"] == "true") {
            file addPhrase "*"
            return
        }

        when (val variance: Variance = typeArgument.variance) {
            Variance.STAR -> {
                file addPhrase "*"
                return
            }
            Variance.COVARIANT, Variance.CONTRAVARIANT -> {
                file addPhrase variance.label
                file addPhrase " "
            }
            Variance.INVARIANT -> {
                // do nothing
            }
        }
        val resolvedType: KSType? = typeArgument.type?.resolve()
        file addPhrase (resolvedType?.declaration?.qualifiedName?.asString() ?: run {
            logger.error("Invalid type argument", typeArgument)
            return
        })
        file addPhrase if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

        val genericArguments: List<KSTypeArgument> =
            typeArgument.type?.element?.typeArguments ?: emptyList()
        visitChildTypeArguments(genericArguments)
    }
}
