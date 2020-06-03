package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

abstract class ComponentClassBuilder(
    protected val scheme: TypeElement,
    protected val resultClassName: String,
    protected val resultClassPackage: String,
    protected val superclass: TypeName,
    protected val viewModelClass: TypeMirror
) {

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
        buildRestMembers()
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

    abstract fun TypeSpec.Builder.buildRestMembers()
}