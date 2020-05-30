package ru.impression.c_logic_base

import androidx.databinding.ViewDataBinding

abstract class ComponentScheme<C, B : ViewDataBinding, VM : ComponentViewModel>(val initializer: (C.(viewModel: VM) -> Unit)? = null)
