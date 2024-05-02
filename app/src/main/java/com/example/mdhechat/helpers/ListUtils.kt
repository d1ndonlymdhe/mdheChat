package com.example.mdhechat.helpers

fun <T> mergeList(list1: List<T>, list2: List<T>): List<T> {
    val newList = mutableListOf<T>();
    list1.forEach {
        newList.add(it)
    }
    list2.forEach {
        newList.add(it)
    }
    return newList.toList()
}
