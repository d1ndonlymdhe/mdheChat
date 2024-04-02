package mainApp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class ChatThumbnail(val username: String, val lastMessage: String)

@Composable
fun ChatThumbnailRenderer(thumbnail: ChatThumbnail) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Surface(
        onClick = {}, interactionSource = interactionSource
    ) {

        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.inversePrimary, RoundedCornerShape(5.dp)
                )
                .fillMaxWidth()
                .padding(8.dp, 4.dp)
        ) {
            Text(thumbnail.username, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(2.dp))
            Text(thumbnail.lastMessage, style = MaterialTheme.typography.labelMedium)
        }
    }
}


@Composable
fun HomeTab(thumbnails: List<ChatThumbnail>) {
    val scrollState = rememberScrollState()
    Surface {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(2.dp, 8.dp)
                .fillMaxWidth()
        ) {
            thumbnails.forEach {
                ChatThumbnailRenderer(thumbnail = it)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


