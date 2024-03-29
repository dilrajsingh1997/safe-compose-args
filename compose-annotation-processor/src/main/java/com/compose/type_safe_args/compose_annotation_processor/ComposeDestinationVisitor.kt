package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import java.io.OutputStream

class ComposeDestinationVisitor(
    private val file: OutputStream,
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
    private val argumentProviderMap: MutableMap<KSClassDeclaration, KSClassDeclaration>,
    private val propertyMap: Map<KSPropertyDeclaration, PropertyInfo>,
    private val singletonClass: KSClassDeclaration?,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val route = classDeclaration.simpleName.asString()
        val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()

        fun getSingletonExtension(): String {
            return if (singletonClass != null) {
                "${singletonClass.simpleName.asString()}."
            } else {
                ""
            }
        }

        val dataClassName = "${route}Args"

        if (singletonClass == null) {
            file addLine "class ${route}Destination {"
            file.increaseIndent()
        }

        if (propertyMap.isNotEmpty()) {
            file addLine "data class $dataClassName ("
            file.increaseIndent()

            properties.forEach { property ->
                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                file addLine "val ${propertyInfo.propertyName}: "
                addVariableType(file, propertyInfo)
                file addPhrase ", "
            }

            file.decreaseIndent()
            file addLine ")"
        }

        if (singletonClass == null) {
            file addLine "companion object {"
            file.increaseIndent()
        }

        if (propertyMap.isNotEmpty()) {
            file addLine "fun ${getSingletonExtension()}parseArguments(backStackEntry: NavBackStackEntry): $dataClassName {"
            file.increaseIndent()

            file addLine "return "
            file addPhrase "$dataClassName("
            file.increaseIndent()

            properties.forEach { property ->
                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                fun getParsedElement() {
                    when (propertyInfo.composeArgumentType) {
                        ComposeArgumentType.BOOLEAN -> {
                            file addPhrase "backStackEntry.arguments?.getBoolean(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: false"
                            }
                        }
                        ComposeArgumentType.STRING -> {
                            file addPhrase "backStackEntry.arguments?.getString(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: \"\""
                            }
                        }
                        ComposeArgumentType.FLOAT -> {
                            file addPhrase "backStackEntry.arguments?.getFloat(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: 0F"
                            }
                        }
                        ComposeArgumentType.INT -> {
                            file addPhrase "backStackEntry.arguments?.getInt(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: 0"
                            }
                        }
                        ComposeArgumentType.LONG -> {
                            file addPhrase "backStackEntry.arguments?.getLong(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: 0L"
                            }
                        }
                        ComposeArgumentType.INT_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getIntArray(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: intArrayOf()"
                            }
                        }
                        ComposeArgumentType.BOOLEAN_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getBooleanArray(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: booleanArrayOf()"
                            }
                        }
                        ComposeArgumentType.LONG_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getLongArray(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: longArrayOf()"
                            }
                        }
                        ComposeArgumentType.FLOAT_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getFloatArray(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: floatArrayOf()"
                            }
                        }
                        ComposeArgumentType.PARCELABLE -> {
                            file addPhrase "backStackEntry.arguments?.getParcelable<"
                            addVariableType(file, propertyInfo)
                            file addPhrase ">(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: throw NullPointerException(\"parcel value not found\")"
                            }
                        }
                        ComposeArgumentType.PARCELABLE_ARRAY -> {
                            file addPhrase "backStackEntry.arguments?.getParcelableArrayList"
                            visitChildTypeArguments(propertyInfo.typeArguments)
                            file addPhrase "(\"$argumentName\")"
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: throw NullPointerException(\"parcel value not found\")"
                            }
                        }
                        ComposeArgumentType.SERIALIZABLE -> {
                            file addPhrase "backStackEntry.arguments?.getSerializable"
                            file addPhrase "(\"$argumentName\") as"
                            if (propertyInfo.isNullable) {
                                file addPhrase "?"
                            }
                            file addPhrase " "
                            addVariableType(file, propertyInfo)
                            if (!propertyInfo.isNullable) {
                                file addPhrase " ?: throw NullPointerException(\"parcel value not found\")"
                            }
                        }
                    }
                }

                file addLine "$argumentName = "
                getParsedElement()
                file addPhrase ", "
            }

            file.decreaseIndent()
            file addLine ")"

            file.decreaseIndent()
            file addLine "}"
        }

        val providerClassName =
            if (propertyMap.any { it.value.hasDefaultValue } &&
                argumentProviderMap.containsKey(classDeclaration)
            ) {
                argumentProviderMap[classDeclaration]?.simpleName?.asString()
            } else {
                null
            }

        var argumentString = ""
        var count = 0
        file addLine "val ${getSingletonExtension()}argumentList"
        file addPhrase ": MutableList<NamedNavArgument> "
        file.increaseIndent()
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
                        "${route}_${propertyInfo.propertyName.replaceFirstChar { it.uppercase() }}NavType"
                    }
                }
            }

            if (propertyInfo.isOptional && !(propertyInfo.isNullable || propertyInfo.hasDefaultValue)) {
                logger.error(
                    message = "All optional arguments must be null or have a default value, https://developer.android.com/jetpack/compose/navigation#optional-args",
                    property
                )
                return
            }

            file.increaseIndent()
            file addLine "navArgument(\"$argumentName\") {"
            file.increaseIndent()
            file addLine "type = ${getElementNavType()}"
            file addLine "nullable = ${propertyInfo.isNullable}"
            if (propertyInfo.hasDefaultValue) {
                file addLine "defaultValue = "
                file addPhrase "${
                    providerClassName ?: logger.error(
                        "no provider found for $argumentName",
                        property
                    )
                }.${argumentName}"
            }
            file.decreaseIndent()
            file addLine "},"
            file.decreaseIndent()

            argumentString += "/{$argumentName}"
            if (count != propertyMap.size) {
                argumentString += ""
            }
        }
        file addLine ")"
        file.decreaseIndent()

        file addLine "fun ${getSingletonExtension()}getDestination("
        properties.forEach { property ->

            val propertyInfo = propertyMap[property] ?: run {
                logger.error("Invalid type argument", property)
                return
            }
            val argumentName = propertyInfo.propertyName

            file addPhrase "$argumentName: "
            addVariableType(file, propertyInfo)
            if (propertyInfo.hasDefaultValue) {
                logger.info(
                    "$providerClassName is providing ${propertyInfo.propertyName}",
                    property
                )
                file addPhrase " = ${
                    providerClassName ?: logger.error(
                        "no provider found for $argumentName",
                        property
                    )
                }.${argumentName}"
            } else if (propertyInfo.isOptional && propertyInfo.isNullable) {
                file addPhrase " = null"
            }
            file addPhrase ", "
        }
        file addPhrase "): String {"
        file.increaseIndent()

        file addLine "return \"$route\" + "
        file.increaseIndent()
        file.increaseIndent()
        count = 0
        properties
            .filter {
                propertyMap[it]?.isOptional?.not() ?: true
            }
            .forEach { property ->
                count++

                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                file addLine "\"/"

                file addPhrase when (propertyInfo.composeArgumentType) {
                    ComposeArgumentType.INT,
                    ComposeArgumentType.BOOLEAN,
                    ComposeArgumentType.LONG,
                    ComposeArgumentType.FLOAT,
                    ComposeArgumentType.STRING,
                    -> "$$argumentName"
                    else -> "\${Uri.encode(gson.toJson($argumentName))}"
                }
                file addPhrase "\""
                file addPhrase " + "
            }

        var firstOptionalProperty = true

        properties
            .filter {
                propertyMap[it]?.isOptional ?: true
            }
            .forEach { property ->
                count++

                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                if (firstOptionalProperty) {
                    firstOptionalProperty = false
                    file addLine "\"?$argumentName="
                } else {
                    file addLine "\"$argumentName="
                }

                file addPhrase when (propertyInfo.composeArgumentType) {
                    ComposeArgumentType.INT,
                    ComposeArgumentType.BOOLEAN,
                    ComposeArgumentType.LONG,
                    ComposeArgumentType.FLOAT,
                    ComposeArgumentType.STRING,
                    -> "$$argumentName"
                    else -> "\${Uri.encode(gson.toJson($argumentName))}"
                }
                file addPhrase ",\""
                file addPhrase " + "
            }
        file addLine "\"\""
        file.decreaseIndent()
        file.decreaseIndent()

        file.decreaseIndent()
        file addLine "}"
        file addLine "val ${getSingletonExtension()}route"
        file.increaseIndent()

        file addLine "get() = "
        file addPhrase "\"$route"


        properties
            .filter {
                propertyMap[it]?.isOptional?.not() ?: true
            }
            .forEach { property ->
                count++

                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                file addPhrase "/"
                file addPhrase "{$argumentName}"
            }

        firstOptionalProperty = true

        properties
            .filter {
                propertyMap[it]?.isOptional ?: true
            }
            .forEach { property ->
                count++

                val propertyInfo = propertyMap[property] ?: run {
                    logger.error("Invalid type argument", property)
                    return
                }
                val argumentName = propertyInfo.propertyName

                if (firstOptionalProperty) {
                    firstOptionalProperty = false
                    file addPhrase "?$argumentName={$argumentName}"
                } else {
                    file addPhrase "$argumentName={$argumentName}"
                }
                file addPhrase ","
            }

        file addPhrase "\""

        file.decreaseIndent()

        if (singletonClass == null) {
            file.decreaseIndent()
            file addLine "}"
        }

        if (singletonClass == null) {
            file.decreaseIndent()
            file addLine "}"
        }
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
        file addPhrase propertyInfo.resolvedClassSimpleName
        visitChildTypeArguments(propertyInfo.typeArguments)
        file addPhrase if (propertyInfo.isNullable) "?" else ""
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
        file addPhrase (resolvedType?.declaration?.simpleName?.asString() ?: run {
            logger.error("Invalid type argument", typeArgument)
            return
        })
        file addPhrase if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

        val genericArguments: List<KSTypeArgument> =
            typeArgument.type?.element?.typeArguments ?: emptyList()
        visitChildTypeArguments(genericArguments)
    }
}
