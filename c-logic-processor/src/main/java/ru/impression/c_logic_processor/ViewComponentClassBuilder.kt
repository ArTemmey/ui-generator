package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.c_logic_annotations.Bindable
import ru.impression.c_logic_annotations.SharedViewModel
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.collections.ArrayList

class ViewComponentClassBuilder(
    scheme: TypeElement,
    resultClassName: String,
    resultClassPackage: String,
    superclass: TypeName,
    bindingClass: TypeMirror,
    viewModelClass: TypeMirror
) : ComponentClassBuilder(
    scheme,
    resultClassName,
    resultClassPackage,
    superclass,
    bindingClass,
    viewModelClass
) {

    private val viewModelIsShared =
        viewModelClass.getAnnotation(SharedViewModel::class.java) != null

    private val bindableProperties = ArrayList<BindableProperty>().apply {
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        viewModelEnclosedElements.forEach { viewModelElement ->
            viewModelElement.getAnnotation(Bindable::class.java)?.let { annotation ->
                val propertyName = viewModelElement.toString().substringBefore('$')
                val capitalizedPropertyName = propertyName.substring(0, 1)
                    .toUpperCase(Locale.getDefault()) + propertyName.substring(1)
                val propertyGetter =
                    viewModelEnclosedElements.first { it.toString() == "get$capitalizedPropertyName()" }
                val propertyType =
                    ((propertyGetter as ExecutableElement).returnType as DeclaredType).typeArguments[0]
                add(
                    BindableProperty(
                        propertyName, capitalizedPropertyName, propertyType, annotation.twoWay
                    )
                )
            }
        }
    }

    override fun buildViewModelProperty() =
        with(
            PropertySpec.builder(
                "viewModel",
                viewModelClass.asTypeName().let { if (viewModelIsShared) it.copy(true) else it })
        ) {
            if (viewModelIsShared) mutable(true)
            addModifiers(KModifier.OVERRIDE)
            initializer(
                "%M($viewModelClass::class)",
                MemberName("ru.impression.c_logic_base", "createViewModel")
            )
            build()
        }

    override fun buildContainerProperty() =
        with(PropertySpec.builder("container", ClassName("android.view", "View"))) {
            addModifiers(KModifier.OVERRIDE)
            initializer("this")
            build()
        }

    override fun buildLifecycleOwnerProperty() = with(
        PropertySpec.builder(
            "lifecycleOwner",
            ClassName("androidx.lifecycle", "LifecycleOwner")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("this")
        build()
    }

    override fun TypeSpec.Builder.buildRestMembers() {
        addSuperinterface(ClassName("androidx.lifecycle", "LifecycleOwner"))
        primaryConstructor(buildConstructor())
        addSuperclassConstructorParameter("context")
        addSuperclassConstructorParameter("attrs")
        addSuperclassConstructorParameter("defStyleAttr")
        addProperty(buildLifecycleRegistryProperty())
        addProperty(buildIsDetachedFromWindowProperty())
        addFunction(buildGetLifecycleFunction())
        if (bindableProperties.isNotEmpty()) addFunction(buildStartObservationsFunction())
        addFunction(buildOnAttachedToWindowFunction())
        addFunction(buildRestoreViewModelFunction())
        addFunction(buildOnDetachedFromWindowFunction())
        if (viewModelIsShared) addFunction(buildReleaseViewModelFunction())
        addInitializerBlock(buildInitializerBlock())
        //addType(buildCompanionObject())
    }

    private fun buildConstructor(): FunSpec = with(FunSpec.constructorBuilder()) {
        addParameter("context", ClassName("android.content", "Context"))
        addParameter(
            ParameterSpec.builder(
                "attrs",
                ClassName("android.util", "AttributeSet").copy(true)
            ).defaultValue("null").build()
        )
        addParameter(
            ParameterSpec.builder("defStyleAttr", Int::class).defaultValue("0").build()
        )
        addAnnotation(JvmOverloads::class)
        build()
    }

    private fun buildLifecycleRegistryProperty() =
        with(
            PropertySpec.builder(
                "lifecycleRegistry",
                ClassName("androidx.lifecycle", "LifecycleRegistry")
            )
        ) {
            addModifiers(KModifier.PRIVATE)
            initializer("%T(this)", ClassName("androidx.lifecycle", "LifecycleRegistry"))
            build()
        }

    private fun buildIsDetachedFromWindowProperty() =
        with(PropertySpec.builder("isDetachedFromWindow", Boolean::class.java)) {
            addModifiers(KModifier.PRIVATE)
            initializer("false")
            build()
        }

    private fun buildInitializerBlock() = with(CodeBlock.builder()) {
        addStatement(
            """
lifecycleRegistry.handleLifecycleEvent(%T.ON_START)
render()
startObservations()
""",
            ClassName("androidx.lifecycle.Lifecycle.Event")
        )
        build()
    }

    private fun buildGetLifecycleFunction() = with(FunSpec.builder("getLifecycle")) {
        addCode(
            """viewModel = %M($viewModelClass::class)
renderer.binding.%M(viewModel)
renderer.binding.executePendingBindings()
""",
            MemberName("ru.impression.c_logic_base", "createViewModel"),
            MemberName("ru.impression.c_logic_base", "setViewModel")
        )
        build()
    }

    private fun buildStartObservationsFunction() =
        with(FunSpec.builder("startObservations")) {
            addModifiers(KModifier.OVERRIDE)
            addCode(
                """super.startObservations()
viewModel${if (viewModelIsShared) "?" else ""}.onStatePropertyChangedListener = { property, value ->
when (property.name) {
"""
            )
            bindableProperties.forEach {
                addCode(
                    """     ${it.name} -> ${it.name}AttrChanged.onChanged()
"""
                )
            }
            addCode(
                """
}
"""
            )
            build()
        }

    private fun buildOnAttachedToWindowFunction() = with(FunSpec.builder("onAttachedToWindow")) {
        addModifiers(KModifier.OVERRIDE)
        addCode(
            """super.onAttachedToWindow()
lifecycleRegistry.handleLifecycleEvent(%T.ON_RESUME)
"""
        )
        if (viewModelIsShared) addCode(
            """restoreViewModel()
"""
        )
        addCode(
            """if (isDetachedFromWindow) {
    isDetachedFromWindow = false
    startObservations()
}
"""
        )
        build()
    }

    private fun buildRestoreViewModelFunction() = with(FunSpec.builder("restoreViewModel")) {
        addCode(
            """viewModel = %M($viewModelClass::class)
renderer.binding.%M(viewModel)
renderer.binding.executePendingBindings()
""",
            MemberName("ru.impression.c_logic_base", "createViewModel"),
            MemberName("ru.impression.c_logic_base", "setViewModel")
        )
        build()
    }

    private fun buildOnDetachedFromWindowFunction() =
        with(FunSpec.builder("onDetachedFromWindow")) {
            addModifiers(KModifier.OVERRIDE)
            addCode(
                """super.onDetachedFromWindow()
lifecycleRegistry.handleLifecycleEvent(%T.ON_DESTROY)
"""
            )
            if (viewModelIsShared) addCode(
                """releaseViewModel()
"""
            )
            build()
        }

    private fun buildReleaseViewModelFunction() =
        with(FunSpec.builder("releaseViewModel")) {
            addCode(
                """
viewModel = null
renderer.binding?.%M(null)
""",
                MemberName("ru.impression.c_logic_base", "setViewModel")
            )
            addModifiers(KModifier.PRIVATE)
            build()
        }

    private fun buildCompanionObject(): TypeSpec = with(TypeSpec.companionObjectBuilder()) {
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        viewModelEnclosedElements.forEach { viewModelElement ->
            viewModelElement.getAnnotation(Bindable::class.java)?.let {
                addFunction(
                    buildBindingFunction(viewModelElement, viewModelEnclosedElements, it.twoWay)
                )
            }
        }
        build()
    }

    private fun buildBindingFunction(
        viewModelElement: Element,
        viewModelEnclosedElements: List<Element>,
        twoWay: Boolean
    ): FunSpec {
        val propertyName = viewModelElement.toString().substringBefore('$')
        val capitalizedPropertyName = propertyName.substring(0, 1)
            .toUpperCase(Locale.getDefault()) + propertyName.substring(1)
        val propertyGetter =
            viewModelEnclosedElements.first { it.toString() == "get$capitalizedPropertyName()" }
        val propertyType =
            ((propertyGetter as ExecutableElement).returnType as DeclaredType).typeArguments[0]
        return with(FunSpec.builder("set$capitalizedPropertyName")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "BindingAdapter")
                ).addMember("%S", propertyName).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            addParameter(
                "value",
                if (twoWay)
                    ClassName(
                        "ru.impression.c_logic_base.ComponentViewModel",
                        "Data"
                    ).parameterizedBy(propertyType.asTypeName().javaToKotlinType())
                else
                    propertyType.asTypeName()
            )
            if (twoWay)
                addCode(
                    """view.twoWayBindingObservables.remove(%S)
view.viewModel.$propertyName.set(value.get())
view.twoWayBindingObservables[%S] = value""",
                    propertyName,
                    propertyName
                )
            else
                addCode("view.viewModel.$propertyName.set(value.get())")
            build()
        }
    }

    class BindableProperty(
        val name: String,
        val capitalizedName: String,
        val type: TypeMirror,
        val twoWay: Boolean
    )
}