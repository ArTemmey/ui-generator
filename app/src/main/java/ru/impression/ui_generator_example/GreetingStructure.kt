package ru.impression.ui_generator_example

import kotlinx.serialization.Serializable
import ru.impression.ui_generator_base.ObservableEntity
import ru.impression.ui_generator_base.ObservableEntityImpl

@Serializable
class GreetingStructure(private var greetingAddressee: String) :
    ObservableEntity by ObservableEntityImpl() {

    var greetingAddresseeState by state(greetingAddressee) { greetingAddressee = it }
}