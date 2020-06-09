package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class FragmentComponentClassBuilder(
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
        with(PropertySpec.builder("viewModel", viewModelClass.asTypeName())) {
            addModifiers(KModifier.OVERRIDE)
            delegate(
                "lazy { %M($viewModelClass::class) } ",
                MemberName("ru.impression.c_logic_base", "createViewModel")
            )
            build()
        }

    override fun buildContainerProperty() =
        with(PropertySpec.builder("container", ClassName("android.view", "View"))) {
            addModifiers(KModifier.OVERRIDE)
            delegate("lazy { %T(context!!) }", ClassName("android.widget", "FrameLayout"))
            build()
        }

    override fun buildLifecycleOwnerProperty() = with(
        PropertySpec.builder(
            "lifecycleOwner",
            ClassName("androidx.lifecycle", "LifecycleOwner")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        delegate("lazy { viewLifecycleOwner }")
        build()
    }

    override fun TypeSpec.Builder.addRestMembers() {
        addFunction(buildOnCreateViewFunction())
        addFunction(buildOnActivityCreatedFunction())
        bindableProperties.forEach { addProperty(buildBindableProperty(it)) }
    }

    private fun buildBindableProperty(bindableProperty: BindableProperty) = with(
        PropertySpec.builder(
            bindableProperty.name,
            bindableProperty.type.asTypeName().javaToKotlinType().copy(true)
        )
    ) {
        mutable(true)
        delegate(
            "%M(%S)",
            MemberName("ru.impression.c_logic_base", "argument"),
            bindableProperty.name
        )
        build()
    }

    private fun buildOnCreateViewFunction() = with(FunSpec.builder("onCreateView")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("inflater", ClassName("android.view", "LayoutInflater"))
        addParameter("container", ClassName("android.view", "ViewGroup").copy(true))
        addParameter("savedInstanceState", ClassName("android.os", "Bundle").copy(true))
        returns(ClassName("android.view", "View").copy(true))
        addCode(
            """
                render()
                return this.container
                """.trimIndent()
        )
        build()
    }

    private fun buildOnActivityCreatedFunction() = with(FunSpec.builder("onActivityCreated")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("savedInstanceState", ClassName("android.os", "Bundle").copy(true))
        bindableProperties.forEach {
            addCode(
                """
                    super.onActivityCreated(savedInstanceState)
                    if (${it.name} != viewModel.${it.name}) {
                      val viewModel${it.capitalizedName} = viewModel::${it.name} as %T
                      if (viewModel${it.capitalizedName}.returnType.isMarkedNullable)
                        viewModel${it.capitalizedName}.%M(viewModel, ${it.name})
                      else
                        ${it.name}?.let { viewModel${it.capitalizedName}.%M(viewModel, it) }
                    }
                
                """.trimIndent(),
                ClassName("kotlin.reflect", "KMutableProperty")
                    .parameterizedBy(STAR),
                MemberName("ru.impression.c_logic_base", "set"),
                MemberName("ru.impression.c_logic_base", "set")
            )
        }
        addCode("startObservations()")
        build()
    }
}