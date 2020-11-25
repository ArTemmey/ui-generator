package ru.impression.ui_generator_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.ui_generator_annotations.Prop
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

    protected val propProperties = ArrayList<PropProperty>().apply {
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        viewModelEnclosedElements.forEach { viewModelElement ->
            viewModelElement.getAnnotation(Prop::class.java)?.let { annotation ->
                val propertyName = viewModelElement.toString().substringBefore('$')
                val capitalizedPropertyName = propertyName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + propertyName.substring(1)
                val propertyGetter = viewModelEnclosedElements.first {
                    it.toString() == "get$capitalizedPropertyName()"
                            || it.toString() == "$propertyName()"
                }
                val propertyType = (propertyGetter as ExecutableElement).returnType
                add(
                    PropProperty(
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
            ClassName("ru.impression.ui_generator_base", "Component")
                .parameterizedBy(superclass, viewModelClass.asTypeName())
        )
        addProperty(buildSchemeProperty())
        addProperty(buildViewModelProperty())
        addProperty(buildContainerProperty())
        addProperty(buildBoundLifecycleOwnerProperty())
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

    protected abstract fun buildBoundLifecycleOwnerProperty(): PropertySpec

    private fun buildRendererProperty() = with(
        PropertySpec.builder(
            "renderer",
            ClassName("ru.impression.ui_generator_base", "Renderer")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("Renderer(this)")
        build()
    }

    abstract fun TypeSpec.Builder.addRestMembers()

    protected class PropProperty(
        val name: String,
        val capitalizedName: String,
        val type: TypeMirror,
        val twoWay: Boolean,
        val attrChangedPropertyName: String
    )
}