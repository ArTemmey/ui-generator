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

    override fun buildObservingHelperProperty() = with(
        PropertySpec.builder(
            "observingHelper",
            ClassName("ru.impression.c_logic_base", "FragmentObservingHelper")
        )
    ) {
        initializer("FragmentObservingHelper(this)")
        build()
    }

    override fun TypeSpec.Builder.buildRestMembers() {
        buildOnCreateViewFunction()
    }

    private fun buildOnCreateViewFunction()  = with(FunSpec.builder("onCreateView")){
        addParameter("")
    }
}