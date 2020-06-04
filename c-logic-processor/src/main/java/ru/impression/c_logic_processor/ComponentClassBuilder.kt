package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.c_logic_annotations.Bindable
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import kotlin.collections.ArrayList

abstract class ComponentClassBuilder(
    protected val scheme: TypeElement,
    protected val resultClassName: String,
    protected val resultClassPackage: String,
    protected val superclass: TypeName,
    protected val viewModelClass: TypeMirror
) {

    protected val bindableProperties = ArrayList<BindableProperty>().apply {
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        viewModelEnclosedElements.forEach { viewModelElement ->
            viewModelElement.getAnnotation(Bindable::class.java)?.let { annotation ->
                val propertyName = viewModelElement.toString().substringBefore('$')
                val capitalizedPropertyName = propertyName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + propertyName.substring(1)
                val propertyGetter =
                    viewModelEnclosedElements.first { it.toString() == "get$capitalizedPropertyName()" }
                val propertyType = (propertyGetter as ExecutableElement).returnType
                add(
                    BindableProperty(
                        propertyName,
                        capitalizedPropertyName,
                        propertyType,
                        annotation.twoWay,
                        "${propertyName}AttrChanged"
                    )
                )
            }
        }
    }

    fun build() = with(TypeSpec.classBuilder(resultClassName)) {
        superclass(superclass)
        addSuperinterface(
            ClassName("ru.impression.c_logic_base", "Component")
                .parameterizedBy(superclass, viewModelClass.asTypeName())
        )
        addProperty(buildSchemeProperty())
        addProperty(buildViewModelProperty())
        addProperty(buildContainerProperty())
        addProperty(buildLifecycleOwnerProperty())
        addProperty(buildRendererProperty())
        addRestMembers()
        build()
    }

    private fun buildSchemeProperty() =
        with(PropertySpec.builder("scheme", scheme.asClassName())) {
            addModifiers(KModifier.OVERRIDE)
            initializer("%T()", scheme.asClassName())
            build()
        }

    protected abstract fun buildViewModelProperty(): PropertySpec

    protected abstract fun buildContainerProperty(): PropertySpec

    protected abstract fun buildLifecycleOwnerProperty(): PropertySpec

    private fun buildRendererProperty() = with(
        PropertySpec.builder(
            "renderer",
            ClassName("ru.impression.c_logic_base", "Renderer")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("Renderer(this)")
        build()
    }

    abstract fun TypeSpec.Builder.addRestMembers()

    protected class BindableProperty(
        val name: String,
        val capitalizedName: String,
        val type: TypeMirror,
        val twoWay: Boolean,
        val attrChangedPropertyName: String
    )
}