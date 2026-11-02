package com.onetap.orderdesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.onetap.orderdesk.ui.activation.ActivationScreen
import com.onetap.orderdesk.ui.dashboard.DashboardScreen
import com.onetap.orderdesk.ui.orders.AllOrdersScreen
import com.onetap.orderdesk.ui.products.ProductsScreen
import com.onetap.orderdesk.ui.theme.OrderDeskTheme

enum class Screen { ACTIVATION, DASHBOARD, NEW_ORDER, PRODUCTS, ALL_ORDERS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrderDeskTheme {
                var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
                var activatedLicense by remember { mutableStateOf("ONETAP-2026-DESK") }

                when (currentScreen) {
                    Screen.ACTIVATION -> ActivationScreen(onActivationSuccess = { code -> activatedLicense = code; currentScreen = Screen.DASHBOARD })
                    Screen.DASHBOARD -> DashboardScreen(
                        licenseCode = activatedLicense, 
                        onNavigateToNewOrder = { currentScreen = Screen.NEW_ORDER }, 
                        onNavigateToProducts = { currentScreen = Screen.PRODUCTS }, 
                        onNavigateToOrders = { currentScreen = Screen.ALL_ORDERS },
                        onLockTerminal = { currentScreen = Screen.ACTIVATION }
                    )
                    Screen.NEW_ORDER -> {}
                    Screen.PRODUCTS -> ProductsScreen(onNavigateToDashboard = { currentScreen = Screen.DASHBOARD })
                    Screen.ALL_ORDERS -> AllOrdersScreen(onNavigateToDashboard = { currentScreen = Screen.DASHBOARD })
                }
            }
        }
    }
}
