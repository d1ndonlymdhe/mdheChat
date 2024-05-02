package com.example.mdhechat.mainApp.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mdhechat.mainApp.FriendshipType
import com.example.mdhechat.mainApp.NotificationMap
import com.example.mdhechat.uiHelpers.Direction
import com.example.mdhechat.uiHelpers.Spaced


enum class NotificationField {
    Friendship,
    System
}

@Composable
fun NotificationScreen(
    notificationMap: NotificationMap?,
    setNotificationMap: (NotificationMap) -> Unit
) {
    if (notificationMap == null) {
        Text(text = "No notifications")
        return
    }
//    val friendshipNotifications by remember {
//        derivedStateOf {
//            notificationMap.friendshipNotificationList
//        }
//    }

    Spaced(
        direction = Direction.COL,
        gap = 10.dp,
        items = NotificationField.entries,
        modifier = Modifier
    ) { notificationField ->
        Text(notificationField.toString())
        when (notificationField) {
            NotificationField.Friendship -> {
                Spaced(
                    direction = Direction.COL,
                    gap = 5.dp,
                    items = notificationMap.friendshipNotificationList,
                    modifier = Modifier.padding(5.dp, 0.dp)
                ) { friendshipNotification ->
                    Text(
                        "${friendshipNotification.relatedUserName} ${
                            when (friendshipNotification.friendshipType) {
                                FriendshipType.OTHER -> "Unknown Event"
                                FriendshipType.SEND -> "sent you "
                                FriendshipType.ACCEPT -> "accepted your"
                            }
                        } friend request"
                    )
                }
            }

            NotificationField.System -> {}
        }
    }

}