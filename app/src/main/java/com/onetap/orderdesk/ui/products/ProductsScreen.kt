package com.onetap.orderdesk.ui.products
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onetap.orderdesk.data.local.entity.Product
import com.onetap.orderdesk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: ProductViewModel = viewModel(), onNavigateToDashboard: () -> Unit = {}) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Product Master", color = TextWhite) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }, containerColor = GoldAccent) { Icon(Icons.Default.Add, "Add", tint = TechNavy) } },
        containerColor = TechNavy
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = searchQuery, onValueChange = { viewModel.onSearchQueryChanged(it) }, placeholder = { Text("Search products...", color = TextGrey) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { p -> Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) { Column(Modifier.padding(12.dp)) { Text(p.name, color = TextWhite); Text(p.barcode, color = GoldAccent) } } }
            }
        }
    }
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var barcode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false }, title = { Text("Add Product", color = TextWhite) },
            text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }); OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }) } },
            confirmButton = { Button(onClick = { viewModel.insertProduct(name, barcode); showAdd = false }) { Text("Save") } }, containerColor = NavySurface
        )
    }
}
