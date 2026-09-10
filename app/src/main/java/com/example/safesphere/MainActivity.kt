package com.example.safesphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.Authy.UI.Loading
import com.example.safesphere.Authy.UI.Login
import com.example.safesphere.Authy.UI.SignUp
import com.example.safesphere.Authy.UI.ViewModel.AuthViewModel
import com.example.safesphere.Authy.UI.ViewModel.AuthViewModelFactory
import com.example.safesphere.Authy.UI.Welcome
import com.example.safesphere.Evidence.State.EvidenceState
import com.example.safesphere.Evidence.UI.CameraScreen
import com.example.safesphere.Evidence.UI.EvidenceVaultScreen
import com.example.safesphere.Home.UI.Home
import com.example.safesphere.IncidentReport.UI.ReportUI
import com.example.safesphere.SafetyMap.UI.Map
import com.example.safesphere.Settings.UI.Setting
import com.example.safesphere.ui.theme.SafeSphereTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBJtVkfZJrKm48eOiJQGJtUF7ocbktWz6M")
        }
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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome"
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
                navController = navController
            )
        }

        composable("signup") {
            SignUp(
                navController = navController
            )
        }
    }
}

@Composable
fun HomeNavigation() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    if (EvidenceState.showCamera) {
        CameraScreen(
            onClose = { EvidenceState.showCamera = false },
            isAutoEmergencyMode = true
        )
        return
    }

    NavHost(
        navController = navController,
        startDestination = "main_tabs"
    ) {
        composable("main_tabs") {
            Scaffold(
                containerColor = Color.White,
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFFF1F5F9)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Folder, contentDescription = "Vault") },
                            label = { Text("Vault", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFFF1F5F9)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Outlined.Description, contentDescription = "Reports") },
                            label = { Text("Reports", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFFF1F5F9)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F172A),
                                selectedTextColor = Color(0xFF0F172A),
                                indicatorColor = Color(0xFFF1F5F9)
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> Home(
                            onNavigateToMap = { navController.navigate("map") },
                            onNavigateToCamera = { navController.navigate("camera") },
                            onNavigateToVault = { selectedTab = 1 }
                        )
                        1 -> EvidenceVaultScreen(
                            onNavigateBack = { selectedTab = 0 },
                            onStartNewEvidence = { navController.navigate("camera") }
                        )
                        2 -> ReportUI(
                            onBack = { selectedTab = 0 },
                            onCancel = { selectedTab = 0 }
                        )
                        3 -> ProfileScreen()
                    }
                }
            }
        }

        composable("home") {
            Home(
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToVault = { selectedTab = 1 }
            )
        }

        composable("map") {
            Map()
        }

        composable("camera") {
            CameraScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable("vault") {
            EvidenceVaultScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartNewEvidence = { navController.navigate("camera") }
            )
        }

        composable("report") {
            ReportUI(
                onBack = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("settings") {
            val context = LocalContext.current
            val userRepository = remember { UserRepository(context) }

            Setting(userRepository = userRepository)
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👤 SafeSphere User Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}