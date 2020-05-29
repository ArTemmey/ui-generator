package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.c_logic_annotations.Bindable
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

class ViewComponentClassBuilder(
    private val processingEnv: ProcessingEnvironment,
    private val scheme: TypeElement,
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
        addProperty(buildSchemeProperty())
        addProperty(buildBindingProperty())
        addProperty(buildViewModelProperty())
        addProperty(buildBindingManagerProperty())
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

    private fun buildSchemeProperty() =
        with(PropertySpec.builder("scheme", scheme.asClassName())) {
            initializer("%T()", scheme.asClassName())
            build()
        }

    private fun buildBindingProperty() =
        with(PropertySpec.builder("binding", bindingClass.asTypeName())) {
            initializer(
                "%T.%N(%T.%N(%N), %L, %L)", bindingClass,
                "inflate",
                ClassName("android.view", "LayoutInflater"),
                "from",
                "context",
                "this",
                "true"
            )
            build()
        }

    private fun buildViewModelProperty() =
        with(PropertySpec.builder("viewModel", viewModelClass.asTypeName())) {
            initializer(
                "%T.%N(%L, %L)",
                ClassName("ru.impression.c_logic_base", "ComponentViewModel"),
                "create",
                "$viewModelClass::class",
                "this"
            )
            build()
        }


    private fun buildBindingManagerProperty() =
        with(
            PropertySpec.builder(
                "bindingManager",
                ClassName("ru.impression.c_logic_base", "BindingManager")
            )
        ) {
            initializer(
                "%T(%L, %N, %N)",
                ClassName("ru.impression.c_logic_base", "BindingManager"),
                "this",
                "binding",
                "viewModel"
            )
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
                                    "Data"
                                ).parameterizedBy(propertyType.asTypeName())
                            else
                                propertyType.asTypeName()
                        )
                        .addCode(
                            """
        |view.bindingManager.removeTwoWayBindableData(%N)
        |view.viewModel.%N.set(value)
        |view.bindingManager.addTwoWayBindableData(%N, value)""", propertyName, propertyName
                        )
                        .build()
                )
            }
        }
        build()
    }
}