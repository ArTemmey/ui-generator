package ru.impression.ui_generator_processor

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import ru.impression.ui_generator_annotations.Prop
import java.util.*
import javax.lang.model.element.ExecutableElement
import kotlin.collections.ArrayList
import kotlin.reflect.full.starProjectedType

@OptIn(KotlinPoetKspPreview::class)
abstract class ComponentClassBuilder(
    protected val scheme: KSClassDeclaration,
    protected val resultClassName: String,
    protected val resultClassPackage: String,
    protected val superclass: TypeName,
    protected val viewModelClass: KSClassDeclaration
) {

    @OptIn(KspExperimental::class)
    protected val propProperties = ArrayList<PropProperty>().apply {
        var downwardViewModelClass: KSClassDeclaration? = viewModelClass

        while (downwardViewModelClass?.qualifiedName?.asString() != "ru.impression.ui_generator_base.ComponentViewModel"
            && downwardViewModelClass?.qualifiedName?.asString() != "ru.impression.ui_generator_base.CoroutineViewModel"
        ) {
            val viewModelEnclosedElements = downwardViewModelClass
                ?.getAllProperties()
                ?.filter { it.findOverridee() == null }

            viewModelEnclosedElements?.forEach { viewModelElement ->
                viewModelElement.getAnnotationsByType(Prop::class).firstOrNull()
                    ?.let { annotation ->

                        val propertyGetter = viewModelElement.getter
                        val propertyName = propertyGetter.toString().substringBefore(".")
                        val capitalizedPropertyName = propertyName
                            .substring(0, 1)
                            .uppercase(Locale.getDefault()) + propertyName.substring(1)

                        add(
                            PropProperty(
                                propertyName,
                                capitalizedPropertyName,
                                propertyGetter!!.returnType!!,
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
                .parameterizedBy(superclass, viewModelClass.toClassName())
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
        with(PropertySpec.builder("scheme", scheme.toClassName())) {
            addModifiers(KModifier.OVERRIDE)
            initializer("%T()", scheme.toClassName())
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

    @OptIn(KotlinPoetKspPreview::class)
    protected class PropProperty(
        val name: String,
        val capitalizedName: String,
        val type: KSTypeReference,
        val twoWay: Boolean,
        val attrChangedPropertyName: String
    ) {
        val kotlinType = type.toTypeName().copy(true)
    }
}