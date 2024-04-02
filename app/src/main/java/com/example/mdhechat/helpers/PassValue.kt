package com.example.mdhechat.helpers

data class PassValue<T>(var value: T) {
    val setVal = { newValue: T ->
        value = newValue
    }

    operator fun component2() = setVal
}


class PassValue2<T>(var value: T, val setValue: (T) -> Unit) {
    operator fun component1() = value
    operator fun component2() = { newValue: T ->
        {
            value = newValue
            setValue(newValue)
        }
    }
}