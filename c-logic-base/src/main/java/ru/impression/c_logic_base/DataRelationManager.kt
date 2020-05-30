package ru.impression.c_logic_base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KProperty1

class DataRelationManager(
    private val activity: FragmentActivity,
    private val viewModel: ComponentViewModel,
    private val observingHelper: ObservingHelper
) {

    fun establishRelations() {
        viewModel.dataRelations.forEach { dataRelation ->
            when (dataRelation.type) {
                DataRelation.Type.MUTABILITY -> observingHelper.observe(
                    (dataRelation.sourceProperty as KProperty1<Any, LiveData<*>>).get(
                        ViewModelProvider(activity)[dataRelation.sourceViewModelClass.java]
                    )
                ) { (dataRelation.target as ComponentViewModel.Data<Any>).set(it) }

                DataRelation.Type.AFFECTION -> observingHelper.observe(dataRelation.target) {
                    (dataRelation.sourceProperty as KProperty1<Any, MutableLiveData<*>>).get(
                        ViewModelProvider(activity)[dataRelation.sourceViewModelClass.java]
                    ).value = it
                }
            }
        }
    }
}