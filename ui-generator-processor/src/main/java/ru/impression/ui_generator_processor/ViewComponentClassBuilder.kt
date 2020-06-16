package ru.impression.ui_generator_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class ViewComponentClassBuilder(
    scheme: TypeElement,
    resultClassName: String,
    resultClassPackage: String,
    superclass: TypeName,
    viewModelClass: TypeMirror
) : ComponentClassBuilder(
    scheme,
    resultClassName,
    resultClassPackage,
    superclass,
    viewModelClass
) {

    override fun buildViewModelProperty() =
        with(
            PropertySpec.builder("viewModel", viewModelClass.asTypeName())
        ) {
            addModifiers(KModifier.OVERRIDE)
            initializer("createViewModel($viewModelClass::class)")
            build()
        }

    override fun buildContainerProperty() =
        with(PropertySpec.builder("container", ClassName("android.view", "View"))) {
            addModifiers(KModifier.OVERRIDE)
            initializer("this")
            build()
        }

    override fun buildBoundLifecycleOwnerProperty() = with(
        PropertySpec.builder(
            "boundLifecycleOwner",
            ClassName("androidx.lifecycle", "LifecycleOwner")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        initializer("this")
        build()
    }

    override fun TypeSpec.Builder.addRestMembers() {
        addSuperinterface(ClassName("androidx.lifecycle", "LifecycleOwner"))
        primaryConstructor(buildConstructor())
        addSuperclassConstructorParameter("context")
        addSuperclassConstructorParameter("attrs")
        addSuperclassConstructorParameter("defStyleAttr")
        addProperty(buildLifecycleRegistryProperty())
        addProperty(buildIsDetachedFromWindowProperty())
        bindableProperties.forEach {
            if (it.twoWay) addProperty(buildBindablePropertyAttrChangedProperty(it))
        }
        addInitializerBlock(buildInitializerBlock())
        addFunction(buildGetLifecycleFunction())
        if (bindableProperties.firstOrNull { it.twoWay } != null)
            addFunction(buildStartObservationsFunction())
        addFunction(buildOnAttachedToWindowFunction())
        addFunction(buildOnDetachedFromWindowFunction())
        addType(buildCompanionObject())
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
            mutable(true)
            addModifiers(KModifier.PRIVATE)
            initializer("false")
            build()
        }

    private fun buildBindablePropertyAttrChangedProperty(bindableProperty: BindableProperty) =
        with(
            PropertySpec.builder(
                bindableProperty.attrChangedPropertyName,
                ClassName("androidx.databinding", "InverseBindingListener").copy(true)
            )
        ) {
            mutable(true)
            addModifiers(KModifier.PRIVATE)
            initializer("null")
            build()
        }

    private fun buildInitializerBlock() = with(CodeBlock.builder()) {
        addStatement(
            """
                lifecycleRegistry.handleLifecycleEvent(%T.Event.ON_START)
                render()
                startObservations()""".trimIndent(),
            ClassName("androidx.lifecycle", "Lifecycle")
        )
        build()
    }

    private fun buildGetLifecycleFunction() = with(FunSpec.builder("getLifecycle")) {
        addModifiers(KModifier.OVERRIDE)
        addCode("return lifecycleRegistry")
        build()
    }

    private fun buildStartObservationsFunction() =
        with(FunSpec.builder("startObservations")) {
            addModifiers(KModifier.OVERRIDE)
            addCode(
                """
                    super.startObservations()
                    """.trimIndent()
            )
            addCode(
                """
                    
                    viewModel.addOnPropertyChangedListener(this) { property, _ ->
                      when (property.name) {
            """.trimIndent()
            )
            bindableProperties.forEach {
                addCode(
                    """
                        
                              %S -> ${it.name}AttrChanged?.onChange()
                    """.trimIndent(),
                    it.name
                )
            }
            addCode(
                """
                    
                      }
                    }
                    """.trimIndent()
            )
            build()
        }

    private fun buildOnAttachedToWindowFunction() = with(FunSpec.builder("onAttachedToWindow")) {
        addModifiers(KModifier.OVERRIDE)
        addCode(
            """
                super.onAttachedToWindow()
                lifecycleRegistry.handleLifecycleEvent(%T.Event.ON_RESUME)
                if (isDetachedFromWindow) {
                  isDetachedFromWindow = false
                  startObservations()
                }
                """.trimIndent(),
            ClassName("androidx.lifecycle", "Lifecycle")
        )
        build()
    }

    private fun buildOnDetachedFromWindowFunction() =
        with(FunSpec.builder("onDetachedFromWindow")) {
            addModifiers(KModifier.OVERRIDE)
            addCode(
                """
                    super.onDetachedFromWindow()
                    lifecycleRegistry.handleLifecycleEvent(%T.Event.ON_DESTROY)
                    viewModel.onCleared()
                    isDetachedFromWindow = true
                    """.trimIndent(),
                ClassName("androidx.lifecycle", "Lifecycle")
            )
            build()
        }

    private fun buildCompanionObject(): TypeSpec = with(TypeSpec.companionObjectBuilder()) {
        bindableProperties.forEach {
            addFunction(buildSetBindablePropertyFunction(it))
            if (it.twoWay) {
                addFunction(buildSetBindablePropertyAttrChangedFunction(it))
                addFunction(buildGetBindablePropertyFunction(it))
            }
        }
        build()
    }

    private fun buildSetBindablePropertyFunction(bindableProperty: BindableProperty) =
        with(FunSpec.builder("set${bindableProperty.capitalizedName}")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "BindingAdapter")
                ).addMember("%S", bindableProperty.name).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            addParameter("value", bindableProperty.type.asTypeName().javaToKotlinType().copy(true))
            addCode(
                """
                    if (value == view.viewModel.${bindableProperty.name}) return
                    val property = view.viewModel::${bindableProperty.name} as %T
                    if (property.returnType.isMarkedNullable)
                      property.%M(view.viewModel, value)
                    else
                      property.%M(view.viewModel, value ?: return)
                """.trimIndent(),
                ClassName("kotlin.reflect", "KMutableProperty")
                    .parameterizedBy(STAR),
                MemberName("ru.impression.ui_generator_base", "set"),
                MemberName("ru.impression.ui_generator_base", "set")
            )
            build()
        }

    private fun buildSetBindablePropertyAttrChangedFunction(bindableProperty: BindableProperty) =
        with(FunSpec.builder("set${bindableProperty.capitalizedName}AttrChanged")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "BindingAdapter")
                ).addMember("%S", bindableProperty.attrChangedPropertyName).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            addParameter(
                "value",
                ClassName("androidx.databinding", "InverseBindingListener").copy(true)
            )
            addCode("view.${bindableProperty.attrChangedPropertyName} = value")
            build()
        }

    private fun buildGetBindablePropertyFunction(bindableProperty: BindableProperty) =
        with(FunSpec.builder("get${bindableProperty.capitalizedName}")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "InverseBindingAdapter")
                ).addMember("attribute = %S", bindableProperty.name).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            returns(bindableProperty.type.asTypeName().javaToKotlinType().copy(true))
            addCode("return view.viewModel.${bindableProperty.name}")
            build()
        }
}