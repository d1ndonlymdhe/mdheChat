package mainApp.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mdhechat.uiHelpers.Direction
import com.example.mdhechat.uiHelpers.Spaced
import kotlinx.serialization.Serializable
import mainApp.User

@Serializable

data class SearchResult(val username: String)


@Composable
fun SearchResultRenderer(results: List<SearchResult>) {
    Spaced(
        direction = Direction.COL, gap = 5.dp, items = results, modifier = Modifier.padding(
            PaddingValues(4.dp, 2.dp)
        )
    ) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        Surface(
            onClick = {}, interactionSource = interactionSource
        ) {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.inversePrimary, RoundedCornerShape(5.dp)
                    )
                    .fillMaxWidth()
                    .padding(8.dp, 4.dp)
            ) {
                Text(
                    it.username,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(0.8f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Friend")
                }
            }
        }
    }
}
