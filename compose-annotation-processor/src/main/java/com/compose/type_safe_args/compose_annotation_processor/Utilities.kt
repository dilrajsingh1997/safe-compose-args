package com.compose.type_safe_args.compose_annotation_processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import java.io.OutputStream

internal fun getPropertyMap(
    properties: Sequence<KSPropertyDeclaration>,
    logger: KSPLogger,
    resolver: Resolver
): Map<KSPropertyDeclaration, PropertyInfo>? {
    val propertyMap = mutableMapOf<KSPropertyDeclaration, PropertyInfo>()
    properties.forEach { property ->
        val resolvedType = property.type.resolve()
        val resolvedClassDeclarationName = resolver.getClassDeclarationByName(
            resolvedType.declaration.qualifiedName ?: run {
                logger.error("Invalid type argument", property)
                return null
            })
            ?.toString() ?: ""
        val resolvedClassQualifiedName = (resolvedType.declaration.qualifiedName?.asString()
            ?: run {
                logger.error("Invalid type argument", property)
                return null
            })
        val typeArguments = property.type.element?.typeArguments ?: emptyList()
        propertyMap[property] = PropertyInfo(
            propertyName = property.simpleName.asString(),
            resolvedType = resolvedType,
            resolvedClassDeclarationName = resolvedClassDeclarationName,
            resolvedClassQualifiedName = resolvedClassQualifiedName,
            resolvedClassSimpleName = resolvedType.declaration.simpleName.asString(),
            typeArguments = typeArguments,
            isNullable = resolvedType.nullability == Nullability.NULLABLE,
            composeArgumentType = when (resolvedClassDeclarationName) {
                "Boolean" -> ComposeArgumentType.BOOLEAN
                "String" -> ComposeArgumentType.STRING
                "Float" -> ComposeArgumentType.FLOAT
                "Int" -> ComposeArgumentType.INT
                "Long" -> ComposeArgumentType.LONG
                "IntArray" -> ComposeArgumentType.INT_ARRAY
                "BooleanArray" -> ComposeArgumentType.BOOLEAN_ARRAY
                "LongArray" -> ComposeArgumentType.LONG_ARRAY
                "FloatArray" -> ComposeArgumentType.FLOAT_ARRAY
                else -> when {
                    resolvedClassQualifiedName == "kotlin.collections.ArrayList" -> {
                        var isParcelable = false
                        var isSerializable = false
                        for (argument in typeArguments) {
                            val resolvedArgument = argument.type?.resolve()
                            if ((resolvedArgument?.declaration as? KSClassDeclaration)?.superTypes?.map { it.toString() }
                                    ?.contains("Parcelable") == true) {
                                isParcelable = true
                            }
                            if ((resolvedArgument?.declaration as? KSClassDeclaration)?.superTypes?.map { it.toString() }
                                    ?.contains("Serializable") == true) {
                                isSerializable = true
                            }
                        }
                        when {
                            isParcelable -> {
                                ComposeArgumentType.PARCELABLE_ARRAY
                            }
                            isSerializable -> {
                                ComposeArgumentType.SERIALIZABLE
                            }
                            else -> {
                                logger.error(
                                    "invalid property type, cannot pass it in bundle",
                                    property
                                )
                                return null
                            }
                        }
                    }
                    (resolvedType.declaration as KSClassDeclaration).superTypes.map { it.toString() }
                        .contains("Parcelable") -> {
                        ComposeArgumentType.PARCELABLE
                    }
                    (resolvedType.declaration as KSClassDeclaration).superTypes.map { it.toString() }
                        .contains("Serializable") -> {
                        ComposeArgumentType.SERIALIZABLE
                    }
                    ((resolvedType.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS) -> {
                        ComposeArgumentType.SERIALIZABLE
                    }
                    else -> {
                        logger.error(
                            "invalid property type, cannot pass it in bundle",
                            property
                        )
                        return null
                    }
                }
            },
            hasDefaultValue = property.annotations.map { it.shortName.asString() }.any { it == "HasDefaultValue" }
        )
    }
    return propertyMap
}

internal fun addImports(file: OutputStream, properties: Collection<PropertyInfo>) {
    properties.forEach { propertyInfo ->
        file addImport "import ${propertyInfo.resolvedClassQualifiedName}"
    }
}

internal val filePropertyMap = mutableMapOf<OutputStream, OutputStreamProperties>()

internal infix fun OutputStream.addLine(line: String) {
    this.initializeFile()
    this.write("\n".toByteArray())

    repeat(filePropertyMap[this]?.tabs ?: 0) {
        this.write("\t".toByteArray())
    }
    this.write(line.toByteArray())
}

internal infix fun OutputStream.addPhrase(line: String) {
    this.initializeFile()
    this.write(line.toByteArray())
}

internal infix fun OutputStream.addImport(import: String) {
    this.initializeFile()

    if (filePropertyMap[this]?.importSet?.contains(import) == false) {
        filePropertyMap[this]?.importSet?.add(import)
        this.write("\n".toByteArray())
        this.write(import.toByteArray())
    }
}

internal fun OutputStream.dispose() {
    this.close()
    filePropertyMap.remove(this)
}

internal fun OutputStream.initializeFile() {
    if (!filePropertyMap.containsKey(this)) {
        filePropertyMap[this] = OutputStreamProperties(tabs = 0, importSet = mutableSetOf())
    }
}

internal fun OutputStream.increaseIndent() {
    filePropertyMap[this]?.apply {
        tabs++
    }
}

internal fun OutputStream.decreaseIndent() {
    filePropertyMap[this]?.apply {
        tabs--
    }
}

internal data class OutputStreamProperties(var tabs: Int, val importSet: MutableSet<String>)
