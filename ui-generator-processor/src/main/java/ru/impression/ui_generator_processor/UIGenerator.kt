package ru.impression.ui_generator_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import java.lang.StringBuilder

@OptIn(KotlinPoetKspPreview::class)
class UIGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver.getSymbolsWithAnnotation("ru.impression.ui_generator_annotations.MakeComponent")
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(BuilderVisitor(), Unit) }

        return ret
    }

    inner class BuilderVisitor : KSVisitorVoid() {


        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)

            val resultClassName = "${classDeclaration.simpleName.asString()}Component"
            val resultClassPackageName = classDeclaration.containingFile!!.packageName.asString()
            val file = codeGenerator.createNewFile(
                Dependencies(true, classDeclaration.containingFile!!),
                resultClassPackageName,
                resultClassName
            )
            val typeArguments = classDeclaration.superTypes.first().element
                ?.typeArguments
                ?: return logger.error("Cannot find type arguments")
            val superClass = typeArguments.first()
            val viewModelClass = typeArguments[1].type!!.resolve().declaration as KSClassDeclaration
            var resultClass: TypeSpec? = null
            var downwardClass = superClass.type?.resolve()

            classIteration@ while (downwardClass != null) {
                when (downwardClass.toClassName().canonicalName) {
                    "android.view.View" -> {
                        resultClass = ViewComponentClassBuilder(
                            classDeclaration,
                            resultClassName,
                            resultClassPackageName,
                            with(superClass.type!!.resolve()) {
                                toTypeName(declaration.typeParameters.toTypeParameterResolver())
                            },
                            viewModelClass
                        ).build()
                        break@classIteration
                    }
                    "androidx.fragment.app.Fragment" -> {
                        resultClass = FragmentComponentClassBuilder(
                            classDeclaration,
                            resultClassName,
                            resultClassPackageName,
                            with(superClass.type!!.resolve()) {
                                toTypeName(declaration.typeParameters.toTypeParameterResolver())
                            },
                            viewModelClass
                        ).build()
                        break@classIteration
                    }

                    else -> downwardClass =
                        (downwardClass.declaration as KSClassDeclaration).superTypes.firstOrNull()
                            ?.resolve()
                }
            }

            resultClass ?: return logger.error(
                "Illegal type of superclass for ${classDeclaration.toClassName().canonicalName}. Superclass must be either " +
                        "out android.view.View or out " +
                        "androidx.fragment.app.Fragment"
            )

            val resultContent = FileSpec.builder(resultClassPackageName, resultClassName)
                .addType(resultClass)
                .build()

            val resultText = StringBuilder().apply { resultContent.writeTo(this) }.toString()

            file.write(resultText.encodeToByteArray())
            file.close()
        }
    }
}