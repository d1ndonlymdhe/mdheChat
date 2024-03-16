@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdhechat

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.ui.theme.MdheChatTheme
import com.example.mdhechat.uiHelpers.Direction
import com.example.mdhechat.uiHelpers.Spaced
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdheChatTheme {
                MainView()
            }
        }
    }
}


// Define the Profile composable.
@Composable
fun Profile(onNavigateToFriendsList: () -> Unit) {
    Text("Profile")
    Button(onClick = { onNavigateToFriendsList() }) {
        Text("Go to Friends List")
    }
}

// Define the FriendsList composable.
@Composable
fun FriendsList(onNavigateToProfile: () -> Unit) {
    Text("Friends List")
    Button(onClick = { onNavigateToProfile() }) {
        Text("Go to Profile")
    }
}

// Define the MyApp composable, including the `NavController` and `NavHost`.
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "profile") {
        composable("Hello") { Profile(onNavigateToFriendsList = { navController.navigate("friendslist") }) }
        composable("friendslist") { FriendsList(onNavigateToProfile = { navController.navigate("profile") }) }
    }
}


enum class Tabs(val icon: ImageVector, val title: String) {
    Home(Icons.Filled.Home, "Home"), Add(Icons.Filled.Add, "Add Friends"), Share(
        Icons.Filled.Share,
        "Share"
    )
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainView() {
    CurrentChats()
}

@Composable
fun TopBar(comp: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .padding(8.dp, 4.dp)
    ) {
        comp()
    }
}


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
fun AllThumbnailRenderer(thumbnails: List<ChatThumbnail>) {
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


@Composable
fun BottomBar(activeScreen: Tabs, tabChanger: (Tabs) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp, 4.dp), horizontalArrangement = Arrangement.Center
    ) {


        val defaultModifier = Modifier

        val unSelectedModifier = defaultModifier.then(
            Modifier
                .weight(1.0f / Tabs.entries.size)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer, ButtonDefaults.outlinedShape
                )
        )
        val selectedModifier = defaultModifier.then(
            Modifier
                .weight(1.0f)
                .background(
                    MaterialTheme.colorScheme.primaryContainer, ButtonDefaults.outlinedShape
                )
        )

        Tabs.entries.forEach {
            OutlinedButton(
                onClick = {
                    tabChanger(it)
                },
                modifier = if (activeScreen == it) {
                    selectedModifier
                } else {
                    unSelectedModifier
                },
            ) {
                Icon(imageVector = it.icon, contentDescription = it.name)
            }
            Spacer(modifier = Modifier.width(5.dp))
        }


    }
}


@Composable
fun CurrentChats() {
    var chatPreviews by remember {
        mutableStateOf(
            listOf(
                ChatThumbnail("d1ndonlymdhe", "Hello"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
            )
        )
    }
    var activeScreen by remember {
        mutableStateOf(Tabs.Home)
    }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var searchUsername by remember {
        mutableStateOf("")
    }

    var searchResultUsers by remember {
        mutableStateOf(listOf<User>())
    }

    val searchRequest by remember {
        mutableStateOf(Request(onFailure = {
            it.message?.let { it1 -> Log.e("SER", it1) }
        }, requester = suspend {
            val res = client.get {
                url {
                    protocol = URLProtocol.HTTP
                    host = "192.168.1.86"
                    port = 8080
                    path("/search")
                    parameter("username", searchUsername)
                }
            }
            if (res.status == HttpStatusCode.OK) {
                val resString = res.body<String>();
                Json.decodeFromString<SearchResult>(resString)
            } else {
                SearchResult(false, listOf())
            }
        }, onSuccess = {
            searchResultUsers = it.results
        }))
    }



    NavHost(navController = navController, startDestination = Tabs.Home.toString()) {
        Tabs.entries.forEach { tab ->
            composable(tab.toString()) {
                activeScreen = tab
            }
        }
    }

    Scaffold(topBar = {
        when (activeScreen) {
            Tabs.Add -> {
                TopBar {
                    OutlinedTextField(
                        value = searchUsername,
                        onValueChange = { searchUsername = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            scope.launch {
                                searchRequest.execute()
                            }
                        })
                    )
                }
            }

            else -> {
                TopBar {
                    Text(
                        "Chat",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }, bottomBar = {
        BottomBar(activeScreen = activeScreen, tabChanger = {
            if (activeScreen != it) {
                navController.navigate(it.toString()) {
                    popUpTo(Tabs.Home.toString())
                }

            }
        })

    }) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            when (activeScreen) {
                Tabs.Home -> {
                    AllThumbnailRenderer(thumbnails = chatPreviews)
                }
                Tabs.Add -> {
                    when (searchRequest.state) {
                        RequstState.LOADING -> {
                            Text("Loading")
                        }
                        RequstState.NONE -> {
                            Text("Search For Users")
                        }
                        RequstState.FAILURE -> {
                            Text("Error Occurred")
                        }
                        RequstState.SUCCESS -> {
                            SearchResultRenderer(results = searchResultUsers) {}
                        }
                    }
                }

                Tabs.Share -> {
                    Text(text = "Share", color = Color.Green)
                }

            }

        }
    }
}


@Composable
fun SearchResultRenderer(results: List<User>, setScreen: (Tabs) -> Unit) {
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


@Serializable
data class User(val id: String, val username: String)

@Serializable
data class AuthUser(val id: String, val username: String, val password: String)

@Serializable
data class SearchResult(val success: Boolean, val results: List<User>)
