package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.c_logic_annotations.Bindable
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

class ViewComponentClassBuilder(
    private val processingEnv: ProcessingEnvironment,
    private val resultClassName: String,
    private val resultClassPackage: String,
    private val superclass: TypeName,
    private val bindingClass: TypeMirror,
    private val viewModelClass: TypeMirror
) {

    fun build(): TypeSpec = with(TypeSpec.classBuilder(resultClassName)) {
        superclass(superclass)
        primaryConstructor(buildConstructor())
        addSuperclassConstructorParameter("context")
        addSuperclassConstructorParameter("attrs")
        addSuperclassConstructorParameter("defStyleAttr")
        addProperty(
            PropertySpec.builder(
                "implementation",
                ClassName("ru.impression.c_logic_base", "ViewComponentImplementation")
            ).initializer(
                CodeBlock.of(
                    "%T(%L, %T.%N(%T.%N(%N), %L, %L), %L)",
                    ClassName("ru.impression.c_logic_base", "ViewComponentImplementation"),
                    "this",
                    bindingClass,
                    "inflate",
                    ClassName("android.view", "LayoutInflater"),
                    "from",
                    "context",
                    "this",
                    "true",
                    "$viewModelClass::class"
                )
            ).build()
        )
        addType(buildCompanionObject())
        build()
    }

    private fun buildConstructor(): FunSpec = with(FunSpec.constructorBuilder()) {
        addParameter("context", ClassName("android.content", "Context"))
        addParameter(
            ParameterSpec.builder(
                "attrs",
                ClassName("android.util", "AttributeSet").copy(true)
            ).defaultValue("%L", null).build()
        )
        addParameter(
            ParameterSpec.builder("defStyleAttr", Int::class).defaultValue("%L", 0).build()
        )
        addAnnotation(JvmOverloads::class)
        build()
    }

    private fun buildCompanionObject(): TypeSpec = with(TypeSpec.companionObjectBuilder()) {
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        viewModelEnclosedElements.forEach { viewModelElement ->
            val bindableAnnotation = viewModelElement.getAnnotation(Bindable::class.java)
            if (bindableAnnotation != null) {
                val propertyName = viewModelElement.toString().substringBefore('$')
                val capitalizedPropertyName = propertyName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + propertyName.substring(1)
                val propertyGetter =
                    viewModelEnclosedElements.first { it.toString() == "get$capitalizedPropertyName()" }
                val propertyType =
                    ((propertyGetter as ExecutableElement).returnType as DeclaredType).typeArguments[0]
                addFunction(
                    FunSpec.builder("set$capitalizedPropertyName")
                        .addAnnotation(JvmStatic::class.java)
                        .addAnnotation(
                            AnnotationSpec.builder(
                                ClassName("androidx.databinding", "BindingAdapter")
                            ).addMember("%S", propertyName).build()
                        ).addParameter("view", ClassName(resultClassPackage, resultClassName))
                        .addParameter(
                            "value",
                            if (bindableAnnotation.twoWay)
                                ClassName(
                                    "ru.impression.c_logic_base.ComponentViewModel",
                                    "Observable"
                                ).parameterizedBy(propertyType.asTypeName())
                            else
                                propertyType.asTypeName()
                        )
                        .addCode(
                            "%N.%N.%N(%S, %N)",
                            "view",
                            "implementation",
                            "onValueBound",
                            propertyName,
                            "value"
                        )
                        .build()
                )
            }
        }
        build()
    }
}