package ru.impression.c_logic_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import ru.impression.c_logic_annotations.MakeComponent
import ru.impression.c_logic_annotations.SharedViewModel
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

@AutoService(Processor::class)
class CLogicProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes() = mutableSetOf(MakeComponent::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment): Boolean {
        p1.getElementsAnnotatedWith(MakeComponent::class.java).forEach { element ->
            element as TypeElement
            val typeArguments = (element.superclass as DeclaredType).typeArguments
            val superclass = typeArguments[0]
            val viewModelClass = typeArguments[1]
            val resultClassName = "${element.simpleName}Component"
            val resultClassPackage = processingEnv.elementUtils.getPackageOf(element).toString()
            var resultClass: TypeSpec? = null
            var downwardClass = superclass
            classIteration@ while (downwardClass.toString() != "none") {
                when (downwardClass.toString()) {
                    "android.view.View" -> {
                        val viewModelClassString = viewModelClass.toString()
                        resultClass = ViewComponentClassBuilder(
                            element,
                            resultClassName,
                            resultClassPackage,
                            superclass.asTypeName(),
                            viewModelClass,
                            p1.getElementsAnnotatedWith(SharedViewModel::class.java)
                                .firstOrNull { it.toString() == viewModelClassString } != null
                        ).build()
                        break@classIteration
                    }
                    "androidx.fragment.app.Fragment" -> {
                        resultClass = FragmentComponentClassBuilder(
                            element,
                            resultClassName,
                            resultClassPackage,
                            superclass.asTypeName(),
                            viewModelClass
                        ).build()
                        break@classIteration
                    }
                    else -> downwardClass =
                        ((downwardClass as DeclaredType).asElement() as TypeElement).superclass
                }
            }

            resultClass ?: run {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Illegal type of superclass for $element. Superclass must be either " +
                            "out android.view.View or out " +
                            "androidx.fragment.app.Fragment"
                )
                return false
            }

            val file =
                FileSpec.builder(resultClassPackage, resultClassName).addType(resultClass).build()
            file.writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!))
        }
        return false
    }
}
