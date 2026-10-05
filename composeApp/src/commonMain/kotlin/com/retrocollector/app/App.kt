package com.retrocollector.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retrocollector.app.core.di.appModules
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.SurfaceBase
import com.retrocollector.app.dashboard.presentation.ui.AdaptiveMainScreen
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel
import com.retrocollector.app.discovery.presentation.viewmodel.DiscoveryViewModel
import com.retrocollector.app.wishlist.presentation.viewmodel.WishlistViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.key
import com.retrocollector.app.core.presentation.util.setAppLocale

@Composable
fun App() {
    KoinApplication(application = { modules(appModules) }) {
        val dashboardViewModel = koinInject<DashboardViewModel>()
        val discoveryViewModel = koinInject<DiscoveryViewModel>()
        val wishlistViewModel = koinInject<WishlistViewModel>()
        val dashboardState by dashboardViewModel.uiState.collectAsState()

        setAppLocale(dashboardState.settings.appLanguage)

        key(dashboardState.settings.appLanguage) {
            RetroTactileTheme(themeMode = dashboardState.settings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceBase
                ) {
                    AdaptiveMainScreen(
                        dashboardViewModel = dashboardViewModel,
                        discoveryViewModel = discoveryViewModel,
                        wishlistViewModel = wishlistViewModel
                    )
                }
            }
        }
    }
}

