package ru.impression.ui_generator_base

import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

abstract class ComponentScheme<C, VM : ComponentViewModel>(
    val getBindingClass: (C.(viewModel: VM) -> KClass<out ViewDataBinding>?)? = null
)