@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdhechat.mainApp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mdhechat.client
import com.example.mdhechat.dataStore
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.helpers.Response
import com.example.mdhechat.helpers.getTokenFromStore
import com.example.mdhechat.helpers.getUsernameFromStore
import com.example.mdhechat.server
import com.example.mdhechat.ui.theme.MdheChatTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.example.mdhechat.mainApp.screens.ChatThumbnail
import com.example.mdhechat.mainApp.screens.HomeTab
import com.example.mdhechat.mainApp.screens.SearchResult
import com.example.mdhechat.mainApp.screens.SearchResultRenderer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mdhechat.mainApp.screens.ProfileScreen

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


enum class Screen {
    Home, Profile
}


@Composable
fun MainView() {
    val context = LocalContext.current

    var username by remember {
        mutableStateOf("")
    }
    var token by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        username = getUsernameFromStore(context.dataStore) ?: ""
        token = getTokenFromStore(context.dataStore) ?: ""
    }

    val (profileUser,setProfileUser) = remember {
        mutableStateOf(
            User("", "")
        )
    }

    val mainNavController = rememberNavController()

    val (currentScreen, setCurrentScreen) = remember {
        mutableStateOf(Screen.Home)
    }

    CompositionLocalProvider(localUserData provides UserData(username, token)) {

        NavHost(navController = mainNavController, startDestination = Screen.Home.toString()) {
            Screen.entries.forEach { screen ->
                composable(screen.toString()) {
                    when (screen) {
                        Screen.Home -> {
                            HomeScreen(
                                mainNavController = mainNavController,
                                screen = screen,
                                setScreen = setCurrentScreen,
                                profileUser = profileUser,
                                setProfileUser = setProfileUser
                            )
                        }

                        Screen.Profile -> {
                            ProfileScreen(user = profileUser)
                        }
                    }
                }
            }
        }
    }

}


enum class Tabs(val icon: ImageVector, val title: String) {
    Chats(
        Icons.Filled.Home, "Chats"
    ),
    Search(
        Icons.Filled.Add, "Add Friends"
    ),
    Notifications(
        Icons.Filled.Notifications, "Notifications"
    ),
}


@Composable
fun HomeScreen(
    mainNavController: NavController,
    screen: Screen,
    setScreen: (Screen) -> Unit,
    profileUser: User,
    setProfileUser: (User) -> Unit
) {
    val username = localUserData.current.username
    val token = localUserData.current.token


    val chatPreviews by remember {
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
    val (activeTab, setActiveTab) = remember {
        mutableStateOf(Tabs.Chats)
    }

    val homeNavController = rememberNavController()
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
    Scaffold(topBar = {
        val navStackEntry by homeNavController.currentBackStackEntryAsState()
        val currentDestination = navStackEntry?.destination
        TopAppBar(modifier = Modifier.fillMaxHeight(0.08f),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    when (currentDestination?.hierarchy?.first()?.route) {
                        Tabs.Search.toString() -> OutlinedTextField(
                            modifier = Modifier
                                .padding(0.dp, 4.dp)
                                .fillMaxHeight(),
                            value = searchUsername,
                            onValueChange = { searchUsername = it },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                scope.launch {
                                    searchRequest.execute()
                                }
                            })
                        )

                        else -> currentDestination?.hierarchy?.first()?.route?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            })

    }, bottomBar = {
        NavigationBar(modifier = Modifier.fillMaxHeight(0.08f)) {
            val navStackEntry by homeNavController.currentBackStackEntryAsState()
            val currentDestination = navStackEntry?.destination
            Tabs.entries.forEach { screen ->
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == screen.toString() } == true,
                    onClick = {
                        setActiveTab(screen)
                        homeNavController.navigate(screen.toString()) {
                            popUpTo(Tabs.Chats.toString())
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    icon = { Icon(screen.icon, screen.title) },
                )

            }
        }


    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = homeNavController, startDestination = Tabs.Chats.toString()
            ) {
                Tabs.entries.forEach { tab ->
                    composable(tab.toString()) {
                        when (tab) {
                            Tabs.Chats -> {
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
                                            mainNavController = mainNavController,
                                            results = searchResult,
                                            profileUser = profileUser,
                                            setProfileUser = setProfileUser,
                                            activeScreen = screen,
                                            setActiveScreen = setScreen
                                        )
                                    }
                                }
                            }

                            Tabs.Notifications -> {
                                Text(text = "Notifications", color = Color.Green)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Serializable
data class User(val id: String, val username: String)

@Serializable
data class AuthUser(val id: String, val username: String, val password: String)


