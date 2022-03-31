package ru.impression.ui_generator_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import ru.impression.ui_generator_annotations.SharedViewModel

@OptIn(KotlinPoetKspPreview::class)
class FragmentComponentClassBuilder(
    logger: KSPLogger,
    scheme: KSClassDeclaration,
    resultClassName: String,
    resultClassPackage: String,
    superclass: TypeName,
    viewModelClass: KSClassDeclaration,
    packageName: String
) : ComponentClassBuilder(
    logger,
    scheme,
    resultClassName,
    resultClassPackage,
    superclass,
    viewModelClass,
    packageName
) {

    @OptIn(KspExperimental::class)
    override fun buildViewModelProperty() =
        with(PropertySpec.builder("viewModel", viewModelClass.toClassName())) {
            addModifiers(KModifier.OVERRIDE)
            delegate(if (propProperties.isEmpty()) CodeBlock.of("lazy { createViewModel($viewModelClass::class, ${viewModelClass.isAnnotationPresent(SharedViewModel::class)}) } ") else
                with(CodeBlock.builder()) {
                    add(
                        """
                        lazy { 
                          val viewModel = createViewModel($viewModelClass::class, ${viewModelClass.isAnnotationPresent(SharedViewModel::class)})
                        
                        """.trimIndent()
                    )
                    add("""
                        if (!viewModel.propsAreSet) {
                        
                    """.trimIndent())
                    propProperties.forEach { prop ->
                        if (prop.kotlinType.isNullable) {
                            add("""
                                viewModel.${prop.name} = ${prop.name}
                                
                            """.trimIndent())

                        } else {
                            add("""
                                   ${prop.name}?.let { viewModel.${prop.name} = it }
                                
                            """.trimIndent())
                        }
                    }
                    add("""
                        viewModel.propsAreSet = true
                        }
                        
                    """.trimIndent())
                    add(
                        """
                            viewModel
                        }
                        """.trimIndent()
                    )
                    build()
                })
            build()
        }

    override fun buildContainerProperty() =
        with(PropertySpec.builder("container", ClassName("android.view", "View").copy(true))) {
            mutable(true)
            addModifiers(KModifier.OVERRIDE)
            initializer("null")
            build()
        }

    override fun buildBoundLifecycleOwnerProperty() = with(
        PropertySpec.builder(
            "boundLifecycleOwner",
            ClassName("androidx.lifecycle", "LifecycleOwner")
        )
    ) {
        addModifiers(KModifier.OVERRIDE)
        getter(FunSpec.getterBuilder().addCode("return viewLifecycleOwner").build())
        build()
    }

    override fun TypeSpec.Builder.addRestMembers() {
        propProperties.forEach { addProperty(buildPropWrapperProperty(it)) }
        addInitializerBlock(buildInitializerBlock())
        addFunction(buildOnCreateFunction())
        addFunction(buildOnCreateViewFunction())
        addFunction(buildOnActivityCreatedFunction())
        addFunction(buildOnSaveInstanceStateFunction())
        addFunction(buildOnDestroyViewFunction())
    }

    private fun buildPropWrapperProperty(propProperty: PropProperty) = with(
        PropertySpec.builder(
            propProperty.name,
            propProperty.type.toTypeName().copy(nullable = true)
        )
    ) {
        mutable(true)
        initializer("null")
        getter(
            FunSpec.getterBuilder()
                .addCode(
                    """
                        return field ?: %M("${propProperty.name}")
                    
                    """.trimIndent(),
                    MemberName("ru.impression.ui_generator_base", "getArgument")
                )
                .build()
        )
        setter(
            FunSpec.setterBuilder().addParameter("value", propProperty.type.toTypeName().copy(nullable = true)).addCode(
                """
                    field = value
                    try {
                      %M("${propProperty.name}", value)
                    } catch (e: %T) {
                    }
                """.trimIndent(),
                MemberName("ru.impression.ui_generator_base", "putArgument"),
                IllegalArgumentException::class.java
            ).build()
        )
        build()
    }

    private fun buildInitializerBlock() = CodeBlock.of(
        """
            hooks.callInitBlocks()
        """.trimIndent()
    )

    private fun buildOnCreateFunction() = with(FunSpec.builder("onCreate")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("savedInstanceState", ClassName("android.os", "Bundle").copy(true))
        addCode(
            """
                super.onCreate(savedInstanceState)
                viewModel.onRestoreInstanceState(savedInstanceState?.getParcelable("viewModelState"))""".trimIndent()
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
                this.container = container
                return render(attachToContainer = false, executeBindingsImmediately = false)?.root
                """.trimIndent()
        )
        build()
    }

    private fun buildOnActivityCreatedFunction() = with(FunSpec.builder("onActivityCreated")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("savedInstanceState", ClassName("android.os", "Bundle").copy(true))
        addCode(
            """
                super.onActivityCreated(savedInstanceState)
                viewModel.setComponent(this)
                """.trimIndent()
        )
        build()
    }

    private fun buildOnSaveInstanceStateFunction() = with(FunSpec.builder("onSaveInstanceState")) {
        addModifiers(KModifier.OVERRIDE)
        addParameter("outState", ClassName("android.os", "Bundle"))
        addCode(
            """
                super.onSaveInstanceState(outState)
                outState.putParcelable("viewModelState", viewModel.onSaveInstanceState())
                """.trimIndent()
        )
        build()
    }

    private fun buildOnDestroyViewFunction() = with(FunSpec.builder("onDestroyView")) {
        addModifiers(KModifier.OVERRIDE)
        addCode(
            """
                container = null
                dataBindingManager.releaseBinding()
                super.onDestroyView()
                """.trimIndent()
        )
        build()
    }
}