package ru.impression.c_logic_base

import kotlin.reflect.KProperty1

@PublishedApi
internal class PropertyShare(
    val sourceProperty: KProperty1<ComponentViewModel, *>,
    val targetProperty: ComponentViewModel.DataImpl<*>
)