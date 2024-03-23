package com.example.mdhechat


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mdhechat.helpers.Request
import com.example.mdhechat.helpers.RequstState
import com.example.mdhechat.helpers.Response
import com.example.mdhechat.helpers.getTokenFromStore
import com.example.mdhechat.ui.theme.MdheChatTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*;
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mainApp.HomeActivity

var client = HttpClient {
    expectSuccess = false
    install(ContentNegotiation) {
        json()
    }
}


const val server = "http://192.168.1.86:8080"

enum class ActiveScreen {
    SIGNUP, LOGIN
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val dataStore = LocalContext.current.dataStore
    val context = LocalContext.current
    LaunchedEffect(null, block = {
        val token = getTokenFromStore(dataStore)
        if (!token.isNullOrEmpty()) {
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    })
    var activeScreen by remember {
        mutableStateOf(ActiveScreen.SIGNUP)
    }
    MdheChatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (activeScreen == ActiveScreen.SIGNUP) {
                    Signup(setActiveScreen = { activeScreen = it })
                } else {
                    Login(setActiveScreen = { activeScreen = it })
                }
            }

        }
    }
}

@Composable
fun Signup(setActiveScreen: (ActiveScreen) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val focusRequester = remember {
        FocusRequester()
    }
    var buttonFocused by remember {
        mutableStateOf(false)
    }

    val signupRequester = Request(
        onSuccess = {
            setActiveScreen(ActiveScreen.LOGIN)
        },
        onFailure = { e ->
            e.message?.let { Log.e("ERR", it) }
            Toast.makeText(context, "Error Occurred", Toast.LENGTH_SHORT).show()
        }
    ) {
        @Serializable
        data class SignupReq(val username: String, val password: String)

        val res = client.post("$server/signup") {
            setAttributes {
                contentType(ContentType.Application.Json)
            }
            setBody(SignupReq(username, password))
        }
        if (res.status != HttpStatusCode.OK) {
            res.body<Response<String>>()
        } else {
            ""
        }

    }


    Column(
        Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SIGN UP FOR CHAT",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.focusRequester(focusRequester),
            singleLine = true,
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.focusRequester(focusRequester),
            keyboardActions = KeyboardActions(onDone = {
                scope.launch {
                    signupRequester.execute()
                }
            }),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(mask = '*'),
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                scope.launch {
                    signupRequester.execute()
                }
            },
                modifier = Modifier
                    .onFocusChanged { buttonFocused = it.isFocused }
            ) {
                Text(
                    text = if (signupRequester.state == RequstState.LOADING) {
                        "Loading"
                    } else {
                        "Sign Up"
                    },
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { setActiveScreen(ActiveScreen.LOGIN) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        ) {
            Text(
                text = "Login Instead",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
            )
        }
    }
}


@Composable
fun Login(setActiveScreen: (ActiveScreen) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val focusRequester = remember {
        FocusRequester()
    }
    var buttonFocused by remember {
        mutableStateOf(false)
    }

    val loginRequester = Request(onSuccess = { res ->
        if (res.success) {
            dataStore.edit {
                val tokenKey = stringPreferencesKey("token")
                val usernameKey = stringPreferencesKey("username")
                it[usernameKey] = username
                it[tokenKey] = res.data
            }
            context.startActivity(Intent(context, HomeActivity::class.java))
        }

    }, onFailure = { err ->
        err.message?.let { Log.e("ERR", it) }
        Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show()
    }) {
        @Serializable
        data class LoginRequest(val username: String, val password: String)
        val res = client.post("$server/login") {
            setAttributes {
                contentType(ContentType.Application.Json)
            }
            setBody(
                LoginRequest(username, password)
            )
        }
        val x = res.body<Response<String>>()
        x
    }


    Column(
        Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LOGIN FOR CHAT",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.focusRequester(focusRequester),
            singleLine = true,
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.focusRequester(focusRequester),
            keyboardActions = KeyboardActions(onDone = {
                scope.launch { loginRequester.execute() }
            }),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(mask = '*'),
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                scope.launch { loginRequester.execute() }
            },
                modifier = Modifier
                    .onFocusChanged { buttonFocused = it.isFocused }
            ) {
                Text(
                    text = "Login",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { setActiveScreen(ActiveScreen.SIGNUP) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        ) {
            Text(
                text = "Signup Instead",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
            )
        }
    }
}