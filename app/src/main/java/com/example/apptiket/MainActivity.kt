package com.example.apptiket

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        val session = SessionManager(applicationContext)

        setContent {
            TiketTheme {
                val context = LocalContext.current
                val state by viewModel.state.collectAsState()
                val ticketError by ticketsViewModel.errorMessage

                // Carga los tickets reales cada vez que se inicia sesión
                LaunchedEffect(state.isLoggedIn) {
                    if (state.isLoggedIn) {
                        ticketsViewModel.cargarTickets()
                    }
                }

                // Muestra errores de red o validación de Laravel al tomar/cerrar tickets
                LaunchedEffect(ticketError) {
                    ticketError?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        ticketsViewModel.errorMessage.value = null
                    }
                }

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