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
        var downwardViewModelClass = viewModelClass
        while (downwardViewModelClass.toString() != "ru.impression.ui_generator_base.ComponentViewModel" && downwardViewModelClass.toString() != "ru.impression.ui_generator_base.CoroutineViewModel") {
            val viewModelEnclosedElements =
                (downwardViewModelClass as DeclaredType).asElement().enclosedElements

            viewModelEnclosedElements.forEach { viewModelElement ->
                viewModelElement.getAnnotation(Prop::class.java)?.let { annotation ->
                    var propertyName = viewModelElement.toString().substringBefore('$')
                    if (propertyName.contains("get")) propertyName =
                        propertyName.replace("get", "").decapitalize(Locale.getDefault())
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

            downwardViewModelClass = (downwardViewModelClass.asElement() as TypeElement).superclass
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
        addProperty(buildDataBindingManagerProperty())
        addProperty(buildHooksProperty())
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

    private fun buildDataBindingManagerProperty() = with(
        PropertySpec.builder(
            "dataBindingManager",
            ClassName("ru.impression.ui_generator_base", "DataBindingManager")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("DataBindingManager(this)")
        build()
    }

    private fun buildHooksProperty() = with(
        PropertySpec.builder(
            "hooks",
            ClassName("ru.impression.ui_generator_base", "Hooks")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("Hooks()")
        build()
    }

    abstract fun TypeSpec.Builder.addRestMembers()

    protected class PropProperty(
        val name: String,
        val capitalizedName: String,
        val type: TypeMirror,
        val twoWay: Boolean,
        val attrChangedPropertyName: String
    ) {
        val kotlinType = type.asTypeName().javaToKotlinType().copy(true)
    }
}