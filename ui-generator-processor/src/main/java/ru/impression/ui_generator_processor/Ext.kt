package ru.impression.ui_generator_processor

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap

fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
    (rawType.javaToKotlinType() as ClassName).parameterizedBy(
        *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
    )
} else {
    val className = JavaToKotlinClassMap.INSTANCE
        .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
    if (className == null) this
    else ClassName.bestGuess(className)
}

fun KSClassDeclaration.asClassName() = ClassName(packageName.asString(), simpleName.asString())

fun KSType.asTypeName() = when(val declaration = declaration) {
    is KSClassDeclaration -> declaration.asClassName()
    is KSTypeAlias -> declaration.findActualType().asClassName()
    is KSTypeParameter -> declaration.closestClassDeclaration()?.asClassName()
    is KSPropertyDeclaration -> declaration.closestClassDeclaration()?.asClassName()

    else -> error("Unsupported type")
} ?: error("Unsupported type")

val KSDeclaration.fullName get() = "${packageName.asString()}.${simpleName.asString()}"