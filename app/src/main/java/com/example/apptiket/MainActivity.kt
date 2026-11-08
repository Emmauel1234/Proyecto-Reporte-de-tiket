package com.example.apptiket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.apptiket.ui.LoginScreen
import com.example.apptiket.ui.LoginViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsState()

                if (state.isLoggedIn) {
                    // Temporal: aquí irá la navegación a las otras 2 pantallas
                    HomePlaceholder(
                        userName = state.userName.orEmpty(),
                        onLogout = viewModel::logout
                    )
                } else {
                    LoginScreen(
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                        onLoginClick = viewModel::login
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePlaceholder(userName: String, onLogout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sesión iniciada: $userName")
            Button(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}