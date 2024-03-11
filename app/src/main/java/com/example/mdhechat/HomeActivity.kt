@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdhechat

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.mdhechat.ui.theme.MdheChatTheme
import java.util.Deque


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
    Home(Icons.Filled.Home, "Home"), Add(Icons.Filled.Add, "Add Friends"),
    Share(
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
fun TopBar(title: String) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .padding(8.dp, 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
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
    var interactionSource = remember {
        MutableInteractionSource()
    }
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
//                    navController.navigate(it.toString());
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


@SuppressLint("RestrictedApi")
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
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
                ChatThumbnail("java", "java is fun"),
            )
        )
    }
    val customBackStack = ArrayDeque<Tabs>(listOf(Tabs.Home))
    var activeScreen by remember {
        mutableStateOf(Tabs.Home)
    }
    val navController = rememberNavController()

//    var prevStackSize by remember {
//        mutableIntStateOf(1)
//    }
    var initBackStackSize = 2
    var prevStackSize by remember {
        initBackStackSize = navController.currentBackStack.value.size
        mutableIntStateOf(initBackStackSize)
    }
    NavHost(navController = navController, startDestination = Tabs.Home.toString()) {
        Tabs.entries.forEach { tab ->
            composable(tab.toString()) {
                LaunchedEffect(key1 = null, block = {
                    val backStack = navController.currentBackStack.value
//                    if (!customBackStack.isEmpty() && customBackStack.first() == Tabs.Home) {
//                        backStack.dropLastWhile {
//                            backStack.size != 2
//                        }
//                    }
//                    if (!customBackStack.isEmpty()) {
//                        activeScreen = if (customBackStack.size > prevStackSize) {
//                            tab
//                        } else {
//                            Tabs.Home
//                        }
//                    }
//                    if (customBackStack.size > 2) {
//                        customBackStack.removeAt(1)
//                    }
//                    prevStackSize = customBackStack.size
//                    if (tab == Tabs.Home) {
//                        backStack.dropLastWhile {
//                            backStack.size != initBackStackSize
//                        }
//                    }

//                    if (tab == Tabs.Home) {
//                        while(backStack.size != 2){
//                            backStack.last
//                        }
//                    }

                    activeScreen = if (backStack.size > prevStackSize) {
                        tab
                    } else {
                        while (backStack.size != 2) {
//                            backStack.dropLast(1)
//                            navController.clearBackStack("")
                            navController.popBackStack(Tabs.Home.toString(),true)
                        }

                        Tabs.Home
                    }
                    if (backStack.size > 4) {
                        backStack.drop(3)
                    }
                    prevStackSize = backStack.size

                })
            }
        }
    }

    Scaffold(topBar = {
        TopBar(title = "Chats")
    }, bottomBar = {
        BottomBar(activeScreen = activeScreen, tabChanger = {
//            activeScreen = it
            customBackStack.addFirst(it)
            navController.navigate(it.toString())
        })

    }) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            when (activeScreen) {
                Tabs.Home -> {
                    AllThumbnailRenderer(thumbnails = chatPreviews)
                }

                Tabs.Add -> {
                    Text(color = Color.Green, text = "Add Friends")
                }

                Tabs.Share -> {
                    Text(text = "Share", color = Color.Green)
                }
            }

        }
    }

}