package ru.impression.ui_generator_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

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
        propProperties.forEach {
            if (it.twoWay) addProperty(buildAttrChangedProperty(it))
        }
        addInitializerBlock(buildInitializerBlock())
        addFunction(buildGetLifecycleFunction())
        if (propProperties.firstOrNull { it.twoWay } != null)
            addFunction(buildOnTwoWayPropChangedFunction())
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

    private fun buildAttrChangedProperty(propProperty: PropProperty) =
        with(
            PropertySpec.builder(
                propProperty.attrChangedPropertyName,
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
                %M(attrs)
                render(false)
                startObservations()
                """.trimIndent(),
            MemberName("ru.impression.ui_generator_base", "resolveAttrs")
        )
        build()
    }

    private fun buildGetLifecycleFunction() = with(FunSpec.builder("getLifecycle")) {
        addModifiers(KModifier.OVERRIDE)
        addCode("return lifecycleRegistry")
        build()
    }

    private fun buildOnTwoWayPropChangedFunction() =
        with(FunSpec.builder("onTwoWayPropChanged")) {
            addModifiers(KModifier.OVERRIDE)
            addParameter("propertyName", ClassName("kotlin", "String"))
            addCode(
                """
                     when (propertyName) {
                     """.trimIndent()
            )
            propProperties.forEach {
                if (it.twoWay)
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
                    """.trimIndent()
            )
            build()
        }

    private fun buildOnAttachedToWindowFunction() = with(FunSpec.builder("onAttachedToWindow")) {
        addModifiers(KModifier.OVERRIDE)
        addCode(
            """
                super.onAttachedToWindow()
                lifecycleRegistry.handleLifecycleEvent(%T.Event.ON_CREATE)
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
        propProperties.forEach {
            addFunction(buildPropSetter(it))
            if (it.twoWay) {
                addFunction(buildAttrChangedSetter(it))
                addFunction(buildPropGetter(it))
            }
        }
        build()
    }

    private fun buildPropSetter(propProperty: PropProperty) =
        with(FunSpec.builder("set${propProperty.capitalizedName}")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "BindingAdapter")
                ).addMember("%S", propProperty.name).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            addParameter("value", propProperty.type.asTypeName().javaToKotlinType().copy(true))
            addCode(
                """
                    if (value === view.viewModel.${propProperty.name}) return
                    view.viewModel::${propProperty.name}.%M(value)
                """.trimIndent(),
                MemberName("ru.impression.ui_generator_base", "safeSetProp")
            )
            build()
        }

    private fun buildAttrChangedSetter(propProperty: PropProperty) =
        with(FunSpec.builder("set${propProperty.capitalizedName}AttrChanged")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "BindingAdapter")
                ).addMember("%S", propProperty.attrChangedPropertyName).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            addParameter(
                "value",
                ClassName("androidx.databinding", "InverseBindingListener").copy(true)
            )
            addCode("view.${propProperty.attrChangedPropertyName} = value")
            build()
        }

    private fun buildPropGetter(propProperty: PropProperty) =
        with(FunSpec.builder("get${propProperty.capitalizedName}")) {
            addAnnotation(JvmStatic::class.java)
            addAnnotation(
                AnnotationSpec.builder(
                    ClassName("androidx.databinding", "InverseBindingAdapter")
                ).addMember("attribute = %S", propProperty.name).build()
            )
            addParameter("view", ClassName(resultClassPackage, resultClassName))
            returns(propProperty.type.asTypeName().javaToKotlinType().copy(true))
            addCode("return view.viewModel.${propProperty.name}")
            build()
        }
}