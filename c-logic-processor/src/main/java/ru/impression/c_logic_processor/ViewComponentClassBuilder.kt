package ru.impression.c_logic_processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import ru.impression.c_logic_annotations.Bindable
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

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

    override fun buildObservingHelperProperty() = with(
        PropertySpec.builder(
            "observingHelper",
            ClassName("ru.impression.c_logic_base", "ViewObservingHelper")
        )
    ) {
        initializer("ViewObservingHelper(this)")
        build()
    }

    override fun TypeSpec.Builder.buildRestMembers() {
        primaryConstructor(buildConstructor())
        addSuperclassConstructorParameter("context")
        addSuperclassConstructorParameter("attrs")
        addSuperclassConstructorParameter("defStyleAttr")
        addProperty(buildBindingProperty())
        addProperty(buildTwoWayBindingObservablesProperty())
        addFunction(buildOnAttachedToWindowFunction())
        addFunction(buildOnDetachedFromWindowFunction())
        addInitializerBlock(
            CodeBlock.of(
                """
binding.lifecycleOwner = %M
binding.viewModel = viewModel
scheme.initializer?.invoke(this, viewModel)
""",
                MemberName("ru.impression.c_logic_base", "activity")
            )
        )
        addType(buildCompanionObject())
    }

    private fun buildConstructor(): FunSpec = with(FunSpec.constructorBuilder()) {
        addParameter("context", ClassName("android.content", "Context"))
        addParameter(
            ParameterSpec.builder(
                "attrs",
                ClassName("android.util", "AttributeSet").copy(true)
            ).defaultValue("%L", null).build()
        )
        addParameter(
            ParameterSpec.builder("defStyleAttr", Int::class).defaultValue("%L", 0).build()
        )
        addAnnotation(JvmOverloads::class)
        build()
    }


    private fun buildBindingProperty() =
        with(PropertySpec.builder("binding", bindingClass.asTypeName())) {
            initializer(
                "%T.inflate(%T.from(context), this, true)",
                bindingClass,
                ClassName("android.view", "LayoutInflater")
            )
            build()
        }

    private fun buildTwoWayBindingObservablesProperty() = with(
        PropertySpec.builder(
            "twoWayBindingObservables",
            ClassName("kotlin.collections", "HashMap").parameterizedBy(
                ClassName("kotlin", "String"),
                ClassName("ru.impression.c_logic_base.ComponentViewModel", "Data")
                    .parameterizedBy(STAR)
            )
        )
    ) {
        initializer("HashMap<String, Data<*>>()")
        build()
    }

    private fun buildOnAttachedToWindowFunction() = with(FunSpec.builder("onAttachedToWindow")) {
        addModifiers(KModifier.OVERRIDE)
        addCode(
            """super.onAttachedToWindow()
dataRelationManager.establishRelations()
"""
        )
        val viewModelEnclosedElements =
            (viewModelClass as DeclaredType).asElement().enclosedElements
        for (viewModelElement in viewModelEnclosedElements) {
            if (viewModelElement.getAnnotation(Bindable::class.java)?.twoWay != true) continue
            val propertyName = viewModelElement.toString().substringBefore('$')
            addCode(
                """observingHelper.observe(viewModel.$propertyName) { twoWayBindingObservables[%S]?.value = it }
""",
                propertyName
            )
        }
        build()
    }

    private fun buildOnDetachedFromWindowFunction() =
        with(FunSpec.builder("onDetachedFromWindow")) {
            addModifiers(KModifier.OVERRIDE)
            addCode(
                """super.onDetachedFromWindow()
observingHelper.stopAllObservations()
"""
            )
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
}