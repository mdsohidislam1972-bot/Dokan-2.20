package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.StoreRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainShopScreen
import com.example.ui.screens.StoreOnboardingScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.StoreOSTheme
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.StoreViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val repository = StoreRepository(this)

        setContent {
            StoreOSTheme {
                val authViewModel = remember { AuthViewModel(repository) }
                AppRoot(
                    authViewModel = authViewModel,
                    repository = repository
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    repository: StoreRepository
) {
    val uiState by authViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AuthUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = VividGreen,
                    strokeWidth = 3.dp
                )
            }
        }

        is AuthUiState.Unauthenticated -> {
            AuthScreen(authViewModel = authViewModel)
        }

        is AuthUiState.AuthenticatedNoStore -> {
            StoreOnboardingScreen(authViewModel = authViewModel)
        }

        is AuthUiState.AuthenticatedWithStore -> {
            // Keyed by uid and storeId to ensure zero state leaks across accounts or stores
            key(state.user.uid, state.storeId) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val networkMonitor = remember { com.example.util.NetworkMonitor(context) }
                val storeViewModel = remember(state.user.uid, state.storeId) {
                    StoreViewModel(state.storeId, repository, networkMonitor)
                }
                MainShopScreen(
                    authViewModel = authViewModel,
                    storeViewModel = storeViewModel
                )
            }
        }

        is AuthUiState.DemoMode -> {
            key("demo_user", state.storeId) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val networkMonitor = remember { com.example.util.NetworkMonitor(context) }
                val storeViewModel = remember(state.storeId) {
                    StoreViewModel(state.storeId, repository, networkMonitor)
                }
                MainShopScreen(
                    authViewModel = authViewModel,
                    storeViewModel = storeViewModel
                )
            }
        }
    }
}
