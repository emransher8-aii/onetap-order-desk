package com.onetap.orderdesk.ui.dashboard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onetap.orderdesk.ui.theme.*

enum class DashboardNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard), ORDERS("Orders", Icons.Default.ReceiptLong), NEW_ORDER("New Order", Icons.Default.AddCircleOutline), PRODUCTS("Products", Icons.Default.Inventory2), SETTINGS("Settings", Icons.Default.Settings)
}
data class OrderTicket(val id: String, val customerName: String, val itemsSummary: String, val total: String, val isPending: Boolean)
val dummyRecentOrders = listOf(
    OrderTicket("#OD-8421", "Sarah Jenkins", "2x Double Wagyu Burger Set", "$42.00", true),
    OrderTicket("#OD-8420", "Liam Vance", "1x Artisan Margherita Pizza", "$31.50", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(licenseCode: String = "ONETAP-2026-DESK", onNavigateToOrders: () -> Unit = {}, onNavigateToNewOrder: () -> Unit = {}, onNavigateToProducts: () -> Unit = {}, onLockTerminal: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(DashboardNavTab.DASHBOARD) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard", color = TextWhite) }, actions = { IconButton(onClick = onLockTerminal) { Icon(Icons.Default.Lock, "Lock", tint = GoldAccent) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0C1E2F)) {
                DashboardNavTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab, onClick = { selectedTab = tab; if (tab == DashboardNavTab.ORDERS) onNavigateToOrders(); if (tab == DashboardNavTab.NEW_ORDER) onNavigateToNewOrder(); if (tab == DashboardNavTab.PRODUCTS) onNavigateToProducts() },
                        icon = { Icon(tab.icon, tab.label, tint = if (selectedTab == tab) GoldAccent else TextGrey) }, label = { Text(tab.label, color = if (selectedTab == tab) GoldAccent else TextGrey, fontSize = 10.sp) }
                    )
                }
            }
        }, containerColor = TechNavy
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Overview", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            items(dummyRecentOrders) { order -> Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) { Column(Modifier.padding(16.dp)) { Text(order.customerName, color = TextWhite); Text("Total: ${order.total}", color = GoldAccent) } } }
        }
    }
}
