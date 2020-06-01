package ru.impression.c_logic_base

import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

abstract class ComponentScheme<C, VM : ComponentViewModel>(
    val render: (C.(viewModel: VM) -> KClass<out ViewDataBinding>?)? = null
)