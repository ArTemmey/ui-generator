package ru.impression.ui_generator_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.ui_generator_annotations.Prop
import java.util.*
import javax.lang.model.element.ExecutableElement
import kotlin.collections.ArrayList
import kotlin.reflect.full.starProjectedType

abstract class ComponentClassBuilder(
    private val scheme: KSClassDeclaration,
    protected val resultClassName: String,
    protected val resultClassPackage: String,
    private val superclass: TypeName,
    protected val viewModelClass: KSClassDeclaration
) {

    @OptIn(KspExperimental::class)
    protected val propProperties = ArrayList<PropProperty>().apply {
        var downwardViewModelClass: KSClassDeclaration? = viewModelClass

        while (downwardViewModelClass?.fullName != "ru.impression.ui_generator_base.ComponentViewModel"
            && downwardViewModelClass?.fullName != "ru.impression.ui_generator_base.CoroutineViewModel"
        ) {
            val viewModelEnclosedElements = downwardViewModelClass?.getAllProperties()

            viewModelEnclosedElements?.forEach { viewModelElement ->
                viewModelElement.getAnnotationsByType(Prop::class).firstOrNull()?.let { annotation ->

                    val propertyGetter = viewModelElement.getter
                    val propertyName = propertyGetter.toString().substringBefore(".")
                    val capitalizedPropertyName = propertyName
                        .substring(0, 1)
                        .uppercase(Locale.getDefault()) + propertyName.substring(1)

                    add(
                        PropProperty(
                            propertyName,
                            capitalizedPropertyName,
                            propertyGetter!!.returnType!!.resolve(),
                            annotation.twoWay,
                            "${propertyName}AttrChanged"
                        )
                    )
                }
            }

            downwardViewModelClass = downwardViewModelClass
                ?.getAllSuperTypes()
                ?.firstOrNull()
                ?.declaration as? KSClassDeclaration
        }
    }

    fun build() = with(TypeSpec.classBuilder(resultClassName)) {
        superclass(superclass)
        addSuperinterface(

            ClassName("ru.impression.ui_generator_base", "Component")
                .parameterizedBy(superclass, viewModelClass.asClassName())
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
        initializer("ru.impression.ui_generator_base.DataBindingManager(this)")
        build()
    }

    private fun buildHooksProperty() = with(
        PropertySpec.builder(
            "hooks",
            ClassName("ru.impression.ui_generator_base", "Hooks")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("ru.impression.ui_generator_base.Hooks()")
        build()
    }

    abstract fun TypeSpec.Builder.addRestMembers()

    protected class PropProperty(
        val name: String,
        val capitalizedName: String,
        val type: KSType,
        val twoWay: Boolean,
        val attrChangedPropertyName: String
    ) {
        val kotlinType = type.asTypeName().javaToKotlinType().copy(true)
    }
}