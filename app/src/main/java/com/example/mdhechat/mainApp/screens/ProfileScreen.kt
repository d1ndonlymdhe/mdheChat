package com.example.mdhechat.mainApp.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mdhechat.client
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.helpers.Response
import com.example.mdhechat.helpers.TokenedRequest
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
import com.example.mdhechat.ui.theme.MdheChatTheme


@Serializable
enum class FriendshipStatus {
    SENT,
    RECEIVED,
    FRIENDS,
    NONE
}


@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfilePreview() {
    MdheChatTheme {
        ProfileScreen(user = User("", "Hari Bahadur"))
    }
}


@Composable
fun ProfileScreen(user: User) {
    val (friendshipStatus, setFriendshipStatus) = remember {
        mutableStateOf(FriendshipStatus.NONE)
    }
    val token = localUserData.current.token
    val getUserInfoRequest = remember {
        Request({
            setFriendshipStatus(it.data)
        }, { err ->
            err.message?.let {
                Log.v("ERR", it)
//            println(it)
            }
        }) {
            val res = client.post("$server/friendship") {
                contentType(ContentType.Application.Json)
                setBody(TokenedRequest(token, user.id))
            }
            res.body<Response<FriendshipStatus>>()
        }
    }

    LaunchedEffect(key1 = null) {
        Log.v("INF", "Executing")
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

                Text(
                    text = getUserInfoRequest.state.toString()
                )
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
    val sendRequest = remember {
        requestFactory(
            token,
            friendId,
            friendshipStatus,
            setFriendshipStatus,
            FriendshipStatus.SENT,
            "$server/friendship/send"
        )
    }
    val acceptRequest = remember {
        requestFactory(
            token,
            friendId,
            friendshipStatus,
            setFriendshipStatus,
            FriendshipStatus.FRIENDS,
            "$server/friendship/accept"
        )
    }
    val rejectRequest = remember {
        requestFactory(
            token,
            friendId,
            friendshipStatus,
            setFriendshipStatus,
            FriendshipStatus.NONE,
            "$server/friendship/reject"
        )
    }
    val unfriendRequest = remember {
        requestFactory(
            token,
            friendId,
            friendshipStatus,
            setFriendshipStatus,
            FriendshipStatus.NONE,
            "$server/friendship/delete"
        )
    }
    val cancelRequest = remember {
        requestFactory(
            token,
            friendId,
            friendshipStatus,
            setFriendshipStatus,
            FriendshipStatus.NONE,
            "$server/friendship/cancel"
        )
    }
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
            setBody(TokenedRequest(token, friendId))
        }
        Log.v("STA", res.status.toString())
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