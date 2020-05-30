package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

abstract class ComponentClassBuilder(
    protected val scheme: TypeElement,
    protected val resultClassName: String,
    protected val resultClassPackage: String,
    protected val superclass: TypeName,
    protected val bindingClass: TypeMirror,
    protected val viewModelClass: TypeMirror
) {

    fun build() = with(TypeSpec.classBuilder(resultClassName)) {
        superclass(superclass)
        addProperty(buildSchemeProperty())
        addProperty(buildBindingProperty())
        addProperty(buildViewModelProperty())
        addProperty(buildObservingHelperProperty())
        addProperty(buildDataRelationManagerProperty())
        buildRestMembers()
        build()
    }

    protected fun buildSchemeProperty() =
        with(PropertySpec.builder("scheme", scheme.asClassName())) {
            initializer("%T()", scheme.asClassName())
            build()
        }

    protected abstract fun buildBindingProperty(): PropertySpec

    protected abstract fun buildViewModelProperty(): PropertySpec

    protected abstract fun buildObservingHelperProperty(): PropertySpec

    protected fun buildDataRelationManagerProperty() = with(
        PropertySpec.builder(
            "dataRelationManager",
            ClassName("ru.impression.c_logic_base", "DataRelationManager")
        )
    ) {
        initializer(
            "DataRelationManager(%M!!, viewModel, observingHelper)",
            MemberName("ru.impression.c_logic_base", "activity")
        )
        build()
    }

    abstract fun TypeSpec.Builder.buildRestMembers()
}