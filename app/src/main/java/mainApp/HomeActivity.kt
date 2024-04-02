@file:OptIn(ExperimentalMaterial3Api::class)

package mainApp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mdhechat.client
import com.example.mdhechat.dataStore
import com.example.mdhechat.helpers.PassValue
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.helpers.Response
import com.example.mdhechat.helpers.getTokenFromStore
import com.example.mdhechat.helpers.getUsernameFromStore
import com.example.mdhechat.server
import com.example.mdhechat.ui.theme.MdheChatTheme
import com.example.mdhechat.uiHelpers.TopBar
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mainApp.screens.ChatThumbnail
import mainApp.screens.HomeTab
import mainApp.screens.Profile
import mainApp.screens.SearchResult
import mainApp.screens.SearchResultRenderer


data class UserData(val username: String, val token: String)

val localUserData = compositionLocalOf { UserData("", "") }

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


enum class Tabs(val icon: ImageVector, val title: String) {
    Home(
        Icons.Filled.Home,
        "Home"
    ),
    Search(
        Icons.Filled.Add,
        "Add Friends"
    ),
    Notifications(
        Icons.Filled.Notifications,
        "Notifications"
    ),
    Profile(
        Icons.Filled.Face,
        "Profile"
    )
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainView() {
    val context = LocalContext.current
    var username = ""
    var token = ""
    val userData = UserData("", "")
    LaunchedEffect(Unit) {
        username = getUsernameFromStore(context.dataStore) ?: ""
        token = getTokenFromStore(context.dataStore) ?: ""
    }

    var profileUser by remember {
        mutableStateOf(User("", ""))
    }

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

    var searchResult by remember {
        mutableStateOf(listOf<SearchResult>())
    }

    val searchRequest by remember {
        mutableStateOf(Request(onFailure = {
            it.message?.let { it1 -> Log.e("SER", it1) }
        }, requester = {
            val res = client.get("$server/search") {
                url {
                    parameter("query", searchUsername)
                }
            }
            if (res.status == HttpStatusCode.OK) {
                res.body<Response<List<SearchResult>>>()
            } else {
                Response(false, listOf())
            }
        }, onSuccess = { res ->
            searchResult = res.data.filter { it.username != username }
        }))
    }
    NavHost(navController = navController, startDestination = Tabs.Home.toString()) {
        Tabs.entries.forEach { tab ->
            composable(tab.toString()) {
                activeScreen = tab
            }
        }
    }
    CompositionLocalProvider(localUserData provides UserData(username, token)) {
        Scaffold(topBar = {
            when (activeScreen) {
                Tabs.Search -> {
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
                            text = activeScreen.title,
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
                        HomeTab(thumbnails = chatPreviews)
                    }

                    Tabs.Search -> {
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
                                SearchResultRenderer(
                                    results = searchResult,
                                    passUser = PassValue(profileUser),
                                    passActiveScreen = PassValue(activeScreen)
                                )
                            }
                        }
                    }

                    Tabs.Notifications -> {
                        Text(text = "Notifications", color = Color.Green)
                    }

                    Tabs.Profile -> {
                        Profile(user = profileUser)
                    }
                }

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


@Serializable
data class User(val id: String, val username: String)

@Serializable
data class AuthUser(val id: String, val username: String, val password: String)


