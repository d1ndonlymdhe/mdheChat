package com.example.mdhechat.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

enum class RequstState {
    NONE, LOADING, SUCCESS, FAILURE
}

class Request<ResponseType>(
    private val onSuccess: suspend (res: ResponseType) -> Unit,
    private val onFailure: suspend (err: Exception) -> Unit = {},
    private val requester: suspend () -> ResponseType,
) {
    var state by mutableStateOf(RequstState.NONE)
    suspend fun execute() {
        if (state != RequstState.LOADING) {
            state = RequstState.LOADING
            try {
                val response = requester()
                onSuccess(response)
                state = RequstState.SUCCESS
            } catch (err: Exception) {
                state = RequstState.FAILURE
                onFailure(err)
            }
        }
    }

    fun resetState() {
        if (state != RequstState.LOADING) {
            state = RequstState.NONE
        }
    }

}

