package com.example.mdhechat.mainApp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mdhechat.client
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.helpers.Response
import com.example.mdhechat.helpers.TokenizedRequest
import com.example.mdhechat.server
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.example.mdhechat.mainApp.User
import com.example.mdhechat.mainApp.localUserData


@Serializable
enum class FriendshipStatus {
    SENT,
    RECEIVED,
    FRIENDS,
    NONE
}


@Composable
fun Profile(user: User) {
    val (friendshipStatus, setFriendshipStatus) = remember {
        mutableStateOf(FriendshipStatus.NONE)
    }


    val token = localUserData.current.token

    val getUserInfoRequest = Request({
        setFriendshipStatus(it.data)
    }, { err ->
        err.message?.let {
            println(it)
        }
    }) {
        val res = client.post("$server/relation") {
            contentType(ContentType.Application.Json)
            setBody(TokenizedRequest(token, user.id))
        }
        res.body<Response<FriendshipStatus>>()
    }
    LaunchedEffect(key1 = null) {
        getUserInfoRequest.execute()
    }
    Column {
        Text(text = user.username)
        Spacer(modifier = Modifier.height(5.dp))
        when (getUserInfoRequest.state) {
            RequstState.SUCCESS -> {
                FriendshipStatusButton(
                    token = token,
                    friendId = user.id,
                    friendshipStatus,
                    setFriendshipStatus,
                )
            }

            else -> {
                getUserInfoRequest.state.toString()
            }
        }


    }
}

@Composable
fun FriendshipStatusButton(
    token: String,
    friendId: String,
    friendshipStatus: FriendshipStatus,
    setFriendshipStatus: (FriendshipStatus) -> Unit
) {
    val sendRequest = requestFactory(
        token,
        friendId,
        friendshipStatus,
        setFriendshipStatus,
        FriendshipStatus.SENT,
        "$server/friendship/send"
    )
    val acceptRequest = requestFactory(
        token,
        friendId,
        friendshipStatus,
        setFriendshipStatus,
        FriendshipStatus.FRIENDS,
        "$server/friendship/accept"
    )
    val rejectRequest = requestFactory(
        token,
        friendId,
        friendshipStatus,
        setFriendshipStatus,
        FriendshipStatus.NONE,
        "$server/friendship/reject"
    )
    val unfriendRequest = requestFactory(
        token,
        friendId,
        friendshipStatus,
        setFriendshipStatus,
        FriendshipStatus.NONE,
        "$server/friendship/remove"
    )
    val cancelRequest = requestFactory(
        token,
        friendId,
        friendshipStatus,
        setFriendshipStatus,
        FriendshipStatus.NONE,
        "$server/friendship/cancel"
    )
    Row {
        when (friendshipStatus) {
            FriendshipStatus.NONE -> {
                FriendShipActionButton(label = "Send Request", request = sendRequest)
            }

            FriendshipStatus.FRIENDS -> {
                FriendShipActionButton(label = "Unfriend", request = unfriendRequest)
            }

            FriendshipStatus.RECEIVED -> {
                FriendShipActionButton(label = "Accept", request = acceptRequest)
                Spacer(modifier = Modifier.width(5.dp))
                FriendShipActionButton(label = "Reject", request = rejectRequest)
            }

            FriendshipStatus.SENT -> {
                FriendShipActionButton(label = "Cancel", request = cancelRequest)
            }
        }
    }
}

fun requestFactory(
    token: String,
    friendId: String,
    friendshipStatus: FriendshipStatus,
    setFriendshipStatus: (FriendshipStatus) -> Unit,
    expectedStatus: FriendshipStatus,
    reqUrl: String
): Request<Response<String>> {
    val request = Request({
        if (it.success) {
            setFriendshipStatus(expectedStatus)
        } else {
            setFriendshipStatus(friendshipStatus)
        }
    }, {
        setFriendshipStatus(friendshipStatus)
    }) {
        val res = client.post(reqUrl) {
            setAttributes {
                contentType(ContentType.Application.Json)
            }
            setBody(TokenizedRequest(token, friendId))
        }
        res.body<Response<String>>()
    }
    return request
}

@Composable
fun <T> FriendShipActionButton(
    label: String,
    request: Request<T>,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            scope.launch {
                request.execute()
            }
        },
        modifier = modifier
    ) {
        Text(text = label, modifier = textModifier)
    }
}