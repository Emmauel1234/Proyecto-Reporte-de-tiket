package com.example.apptiket

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.apptiket.data.SessionManager
import com.example.apptiket.ui.LoginScreen
import com.example.apptiket.ui.LoginViewModel
import com.example.apptiket.ui.MainScreen
import com.example.apptiket.ui.TicketsViewModel
import com.example.apptiket.ui.theme.TiketTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private val ticketsViewModel: TicketsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Encabezado y login son oscuros: íconos claros en la barra de estado
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        val session = SessionManager(applicationContext)

        setContent {
            TiketTheme {
                val state by viewModel.state.collectAsState()

                if (state.isLoggedIn) {
                    MainScreen(
                        userName = state.userName.orEmpty(),
                        employeeNumber = session.employeeNumber,
                        viewModel = ticketsViewModel,
                        onLogout = {
                            ticketsViewModel.reiniciar()
                            viewModel.logout()
                        }
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