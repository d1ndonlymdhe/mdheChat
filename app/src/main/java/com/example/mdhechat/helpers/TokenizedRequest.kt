package com.example.mdhechat.helpers

data class TokenizedRequest<T>(val token: String, val request: T)