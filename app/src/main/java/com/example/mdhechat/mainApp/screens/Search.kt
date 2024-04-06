package com.example.mdhechat.mainApp.screens

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
import androidx.navigation.NavController
import com.example.mdhechat.mainApp.Screen
import com.example.mdhechat.uiHelpers.Direction
import com.example.mdhechat.uiHelpers.Spaced
import kotlinx.serialization.Serializable
import com.example.mdhechat.mainApp.Tabs
import com.example.mdhechat.mainApp.User

@Serializable

data class SearchResult(val username: String, val id: String)


@Composable
fun SearchResultRenderer(
    mainNavController: NavController,
    results: List<SearchResult>,
    profileUser: User,
    setProfileUser: (User) -> Unit,
    activeScreen: Screen,
    setActiveScreen: (Screen) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Spaced(
        direction = Direction.COL, gap = 5.dp, items = results, modifier = Modifier.padding(
            PaddingValues(4.dp, 2.dp)
        )
    ) {
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
                IconButton(onClick = {
                    setActiveScreen(Screen.Profile)
                    mainNavController.navigate(Screen.Profile.toString()) {
                        popUpTo(Screen.Home.toString()) {
                            saveState = true
                        }
                    }
                    setProfileUser(User(it.id, it.username))
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Friend")
                }
            }
        }
    }
}
