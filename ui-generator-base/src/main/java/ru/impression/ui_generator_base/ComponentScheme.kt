package ru.impression.ui_generator_base

abstract class ComponentScheme<C, VM : ComponentViewModel>(
    val render: (C.(viewModel: VM) -> Int?)? = null
)