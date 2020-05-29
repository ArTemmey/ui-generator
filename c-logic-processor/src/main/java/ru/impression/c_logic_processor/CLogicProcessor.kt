package ru.impression.c_logic_processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.MakeComponent
import java.io.File
import java.lang.Exception
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.VariableElement

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
            val superclassTypeMirror = typeArguments[0]
            val superclassName = superclassTypeMirror.toString()
            val superclassIsViewGroup = Class.forName("android.view.ViewGroup")
                .isAssignableFrom(Class.forName(superclassName))
            val superclassIsFragment = Class.forName("androidx.fragment.app.Fragment")
                .isAssignableFrom(Class.forName(superclassName))
            if (!superclassIsViewGroup && !superclassIsFragment)
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Illegal type of superclass for $element. Superclass must extend either " +
                            "android.view.ViewGroup or androidx.fragment.app.Fragment"
                )
            val resultClassName = "${element.simpleName}Component"
            val resultClassPackage = processingEnv.elementUtils.getPackageOf(element).toString()
            val resultClassBuilder =
                TypeSpec.classBuilder(resultClassName).superclass(typeArguments[0].asTypeName())
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("context", ClassName("android.content", "Context"))
                            .addParameter(
                                ParameterSpec.builder(
                                    "attrs",
                                    ClassName("android.util", "AttributeSet").copy(true)
                                ).defaultValue("%L", null).build()
                            )
                            .addParameter(
                                ParameterSpec.builder("defStyleAttr", Int::class)
                                    .defaultValue("%L", 0).build()
                            )
                            .addAnnotation(JvmOverloads::class)
                            .build()
                    )
                    .addSuperclassConstructorParameter("context")
                    .addSuperclassConstructorParameter("attrs")
                    .addSuperclassConstructorParameter("defStyleAttr")
                    .addInitializerBlock(
                        CodeBlock.of(
                            "%T(%L, %L, %L).%N(%L)",
                            ClassName("ru.impression.c_logic_base", "ComponentImplementation"),
                            "this",
                            "${typeArguments[1]}::class.java",
                            "${typeArguments[2]}::class.java",
                            "getRootView",
                            "this"
                        )
                    )

            val companionObjectBuilder = TypeSpec.companionObjectBuilder()

            (typeArguments[2] as DeclaredType).asElement().enclosedElements.forEach { viewModelElement ->
                if (viewModelElement.getAnnotation(Bindable::class.java) != null) {
                    // processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,  viewModelElement.toString())
                    val propertyName = viewModelElement.toString().substringBefore('$')
                    viewModelElement is VariableElement
                    companionObjectBuilder.addFunction(
                        FunSpec.builder("set_$propertyName").addAnnotation(JvmStatic::class.java)
                            .addAnnotation(
                                AnnotationSpec.builder(
                                    ClassName("androidx.databinding", "BindingAdapter")
                                ).addMember("%S", propertyName).build()
                            ).addParameter("view", ClassName(resultClassPackage, resultClassName))
                            .addParameter(
                                "value",
                                ClassName(
                                    "ru.impression.c_logic_base.ComponentViewModel",
                                    "Observable"
                                ).parameterizedBy(ClassName("kotlin", "Any"))
                            )
                            .build()
                    )
                }
            }

            resultClassBuilder.addType(companionObjectBuilder.build())

            val file =
                FileSpec.builder(resultClassPackage, resultClassName)
                    .addType(resultClassBuilder.build()).build()
            file.writeTo(File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!))

            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, typeArguments[2].toString())
        }
        return false
    }
}
