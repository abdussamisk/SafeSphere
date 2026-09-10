package com.example.safesphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safesphere.Authy.UI.Loading
import com.example.safesphere.Authy.UI.Login
import com.example.safesphere.Authy.UI.SignUp
import com.example.safesphere.Authy.UI.ViewModel.AuthViewModel
import com.example.safesphere.Authy.UI.ViewModel.AuthViewModelFactory
import com.example.safesphere.Authy.UI.Welcome
import com.example.safesphere.Evidence.UI.SafeSphereApp
import com.example.safesphere.Home.UI.Home
import com.example.safesphere.SafetyMap.UI.Map
import com.example.safesphere.ui.theme.SafeSphereTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeSphereTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    when (isLoggedIn) {

        null -> {
            Loading()
        }

        true -> {
            HomeNavigation()
        }

        false -> {
            AuthNavigation()
        }
    }
}

@Composable
fun AuthNavigation() {
    val navController=rememberNavController()

    NavHost(
        navController=navController,
        startDestination="welcome"
    ) {
        composable("welcome") {
            Welcome(
                onLoginClick = {
                    navController.navigate("login")
                },
                onGetStartedClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("login") {
            Login(
                navController=navController
            )
        }

        composable("signup") {
            SignUp(
                navController=navController
            )
        }
    }
}

@Composable
fun HomeNavigation() {
    val navController=rememberNavController()

    NavHost(
        navController=navController,
        startDestination="evidence"
    ) {
        composable("evidence") {
            SafeSphereApp()
        }

        composable("home") {
            Home()
        }

        composable("map") {
            Map()
        }

        composable("settings") {
            //Call Settings
        }
    }
}