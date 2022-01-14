package ru.impression.ui_generator_base

interface ObservableEntity : StateOwner {

    fun <T> state(initialValue: T, onChanged: ((T) -> Unit)? = null) =
        StateDelegate(this, initialValue, onChanged)

    fun addStateOwner(stateOwner: StateOwner)

    fun removeStateOwner(stateOwner: StateOwner)
}

class ObservableEntityImpl : ObservableEntity {
    private val stateOwners = ArrayList<StateOwner>()

    override fun addStateOwner(stateOwner: StateOwner) {
        stateOwners.add(stateOwner)
    }

    override fun removeStateOwner(stateOwner: StateOwner) {
        stateOwners.remove(stateOwner)
    }

    override fun onStateChanged(renderImmediately: Boolean) {
        stateOwners.forEach { it.onStateChanged(renderImmediately) }
    }
}