package ru.impression.ui_generator_processor

import com.google.devtools.ksp.symbol.KSClassDeclaration

fun KSClassDeclaration.toTypeName() = asType(emptyList())