package com.example.mdhechat.helpers

import kotlinx.serialization.Serializable

@Serializable
data class TokenedRequest<T>(val token: String, val data: T)