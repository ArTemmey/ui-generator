package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class FragmentComponentClassBuilder(
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

    override fun buildViewModelProperty() =
        with(PropertySpec.builder("viewModel", viewModelClass.asTypeName())) {
            delegate(
                "lazy { %M<$viewModelClass>() } ",
                MemberName("ru.impression.c_logic_base", "obtainViewModel")
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
        addFunction(buildOnCreateFunction())
        addFunction(buildOnCreateViewFunction())
    }

    private fun buildOnCreateFunction() = with(FunSpec.builder("onCreate")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("savedInstanceState", ClassName("android.os", "Bundle").copy(true))
        addCode(
            """dataRelationManager.establishRelations()
scheme.initializer?.invoke(this, viewModel)"""
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
            """binding = %T.inflate(inflater, container, false)
binding.lifecycleOwner = this
binding.viewModel = viewModel
return binding.root""",
            bindingClass
        )
        build()
    }
}