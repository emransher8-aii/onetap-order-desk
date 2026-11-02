package com.onetap.orderdesk.ui.orders
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onetap.orderdesk.data.local.relation.OrderWithLines
import com.onetap.orderdesk.ui.theme.*
import com.onetap.orderdesk.utils.InvoiceGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllOrdersScreen(viewModel: OrderViewModel = viewModel(), onNavigateToDashboard: () -> Unit = {}) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val orders by viewModel.filteredOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<OrderWithLines?>(null) }
    val filters = listOf("All", "PENDING", "DISPATCHED", "DELIVERED")

    Scaffold(
        topBar = { TopAppBar(title = { Text("All Orders", color = TextWhite) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)) }, containerColor = TechNavy
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = searchQuery, onValueChange = { viewModel.searchQuery.value = it }, placeholder = { Text("Search...", color = TextGrey) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f -> FilterChip(selected = statusFilter == f, onClick = { viewModel.statusFilter.value = f }, label = { Text(f) }) }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders) { o -> 
                    Card(onClick = { selectedOrder = o }, colors = CardDefaults.cardColors(containerColor = NavySurface)) { 
                        Column(Modifier.padding(12.dp)) { 
                            Text("#${o.order.id} - ${o.order.customerName}", color = TextWhite)
                            Text("Total: PKR ${formatMoney(o.order.grandTotal)}", color = GoldAccent) 
                        } 
                    } 
                }
            }
        }
    }

    selectedOrder?.let { item ->
        ModalBottomSheet(onDismissRequest = { selectedOrder = null }, containerColor = NavySurface) {
            Column(Modifier.padding(16.dp)) {
                Text("Order #${item.order.id}", color = TextWhite, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { InvoiceGenerator.generateAndShareInvoice(context, item.order, item.lines) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                    Text("Share Invoice on WhatsApp")
                }
            }
        }
    }
}
