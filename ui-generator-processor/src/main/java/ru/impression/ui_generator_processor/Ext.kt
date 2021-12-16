@file:OptIn(KspExperimental::class)

package ru.impression.ui_generator_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

inline fun <reified T: Annotation> KSPropertyDeclaration.hasAnnotation() =
    (getAnnotationsByType(T::class).count() > 0)

fun KSPropertyDeclaration.getParentTree(): List<KSPropertyDeclaration> =
    findOverridee()?.let { listOf(it) + it.getParentTree() }.orEmpty()

inline fun <reified T: Annotation> KSPropertyDeclaration.hasAnnotationInTree(): Boolean =
    getParentTree().none { it.hasAnnotation<T>() } && hasAnnotation<T>()