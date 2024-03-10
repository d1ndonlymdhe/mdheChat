package com.example.mdhechat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.mdhechat.ui.theme.MdheChatTheme

import io.ktor.client.*
import io.ktor.client.call.body
//import io.ktor.client.engine.cio.*;
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

var client = HttpClient() {
    expectSuccess = false
}


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



suspend fun checkAuth(dataStore: DataStore<Preferences>,runner: ()->Unit){
    val username_key = stringPreferencesKey("username")
    val login_status_key = booleanPreferencesKey("login_status")
    var x = false
    dataStore.data.map { preferences ->
        val username  = preferences[username_key]
        val login_status = preferences[login_status_key]
        (username != null && login_status != null) && login_status
    }.collect{ value ->
        if(value){
            runner();
        }
    }
}

@Composable
fun MainScreen() {

    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore
    val context = LocalContext.current;
    LaunchedEffect(null,block = {
        checkAuth(dataStore){
            context.startActivity(Intent(context,HomeActivity::class.java))
        }
    } )
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


suspend fun signupreq(context: Context, username: String, password: String): Boolean {
    @Serializable
    data class Signupreq(val username: String, val password: String)

    Log.v("TEST", Json.encodeToString(Signupreq(username, password)))
    try {
        val res = client.post("http://192.168.1.86:8080/signup") {
            setAttributes {
                contentType(ContentType.Application.Json)
            }
            setBody(
                Json.encodeToString(Signupreq(username, password))
            )
        }
        if (
            res.status == HttpStatusCode.OK
        ) {
            val text = res.body<String>()
            if (text == "Success") {
                return true
            }
        }
    } catch (e: IOException) {
        e.message?.let { Log.e("ERR", it) }
        Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show()
        return false
    } catch (err: Exception) {
        err.message?.let { Log.e("Err", it) }
        Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show()
        return false
    }
    return false
}

@Preview
@Composable
fun signupPreview() {
    Signup(setActiveScreen = {})
}

@Preview
@Composable
fun loginPreview() {
    Login(setActiveScreen = {})
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
    var signuploading by remember {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    fun handleSignup() {
        signuploading = true;
        scope.launch {
            val success = signupreq(context, username, password)
            signuploading = false
            if (success) {
                setActiveScreen(ActiveScreen.LOGIN)
            }
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
                if (!signuploading) {
                    handleSignup()
                }
            }),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(mask = '*'),
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                if (!signuploading) {
                    handleSignup()
                }
            },
                modifier = Modifier
                    .onFocusChanged { buttonFocused = it.isFocused }
            ) {
                Text(
                    text = if (signuploading) {
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


suspend fun loginReq(context: Context, username: String, password: String):Boolean {
    @Serializable
    data class LoginReq(val username: String, val password: String)
    try {
        val res = client.post("http://192.168.1.86:8080/login") {
            setAttributes {
                contentType(ContentType.Application.Json)
            }
            setBody(
                Json.encodeToString(LoginReq(username, password))
            )
        }
        if(res.status == HttpStatusCode.OK){
            if(res.body<String>() == "Success"){
                return true
            }
        }
    } catch (e: IOException) {
        e.message?.let {
            Log.e("ERR", it)
        }
        Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show()
    } catch (err: Exception) {
        err.message?.let { Log.e("ERR", it) }
        Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show()
    }
    return false
}


//@Preview
@Composable
fun Login(setActiveScreen: (ActiveScreen) -> Unit) {
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
    var loginLoading by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current

    fun handleLogin() {
        loginLoading = true;
        scope.launch {
            val success = loginReq(context, username, password)
            loginLoading = false
            if (success) {
                val dataStore = context.dataStore;
                dataStore.edit { preferences->
                    run {
                        val username_key = stringPreferencesKey("username")
                        val login_status_key = booleanPreferencesKey("login_status")
                        preferences[username_key] = username
                        preferences[login_status_key] = true
                    }
                }
            }
        }
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
                handleLogin()
            }),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(mask = '*'),
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                handleLogin()
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