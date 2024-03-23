package com.example.mdhechat.helpers

import kotlinx.serialization.Serializable
@Serializable
data class Response<T>(val success: Boolean, val data: T) {}