package com.onetap.orderdesk.ui.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onetap.orderdesk.data.local.entity.Product
import com.onetap.orderdesk.ui.scanner.BarcodeScannerDialog
import com.onetap.orderdesk.ui.theme.*
import java.util.Locale

fun parseMoneyToMinorUnits(input: String): Long {
    val cleaned = input.replace("[^0-9.]".toRegex(), "")
    if (cleaned.isBlank()) return 0L
    return try { java.math.BigDecimal(cleaned).multiply(java.math.BigDecimal(100)).toLong() } catch (e: Exception) { 0L }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(viewModel: NewOrderViewModel = viewModel(), onNavigateToDashboard: () -> Unit) {
    val context = LocalContext.current
    val customerName by viewModel.customerName.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val whatsappNumber by viewModel.whatsappNumber.collectAsState()
    val city by viewModel.city.collectAsState()
    val address by viewModel.address.collectAsState()
    val customerNotes by viewModel.customerNotes.collectAsState()
    val orderLines by viewModel.orderLines.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val deliveryCharges by viewModel.deliveryCharges.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()

    var showProductPicker by remember { mutableStateOf(false) }
    var showAddOptions by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var deliveryInput by remember { mutableStateOf("0.00") }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Order", color = TextWhite, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)) },
        containerColor = TechNavy
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Customer Details
            item {
                Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CUSTOMER DETAILS", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = customerName, onValueChange = { viewModel.customerName.value = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = phone, onValueChange = { viewModel.phone.value = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                            OutlinedTextField(value = whatsappNumber, onValueChange = { viewModel.whatsappNumber.value = it }, label = { Text("WhatsApp") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        }
                        OutlinedTextField(value = city, onValueChange = { viewModel.city.value = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { viewModel.address.value = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = customerNotes, onValueChange = { viewModel.customerNotes.value = it }, label = { Text("Notes / Reminder") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Add Product Button
            item {
                Button(onClick = { showAddOptions = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) {
                    Icon(Icons.Default.Add, "Add", tint = TechNavy)
                    Text(" ADD PRODUCT", color = TechNavy, fontWeight = FontWeight.Bold)
                }
            }

            // Product Lines
            items(orderLines, key = { it.tempId }) { line ->
                Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(line.productName, color = TextWhite, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.removeLine(line.tempId) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "Del", tint = Color.Red) }
                        }
                        Text("Barcode: ${line.barcode}", color = TextGrey, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TechNavy).border(1.dp, NavyBorder, RoundedCornerShape(8.dp))) {
                                IconButton(onClick = { viewModel.updateQuantity(line.tempId, line.quantity - 1) }, modifier = Modifier.size(30.dp)) { Text("-", color = TextWhite) }
                                Text("${line.quantity}", color = TextWhite, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { viewModel.updateQuantity(line.tempId, line.quantity + 1) }, modifier = Modifier.size(30.dp)) { Text("+", color = TextWhite) }
                            }
                            OutlinedTextField(value = formatMoney(line.rate), onValueChange = { viewModel.updateRate(line.tempId, parseMoneyToMinorUnits(it)) }, label = { Text("Rate") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                }
            }

            // Summary
            item {
                Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = TextGrey)
                            Text("PKR ${formatMoney(subtotal)}", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Delivery", color = TextGrey)
                            OutlinedTextField(value = deliveryInput, onValueChange = { deliveryInput = it; viewModel.setDeliveryCharges(parseMoneyToMinorUnits(it)) }, modifier = Modifier.width(100.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Divider(color = NavyBorder)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", color = TextWhite, fontWeight = FontWeight.Bold)
                            Text("PKR ${formatMoney(grandTotal)}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Button(
                            onClick = {
                                isSaving = true
                                viewModel.saveOrder(
                                    onSuccess = { Toast.makeText(context, "Order Saved!", Toast.LENGTH_SHORT).show(); onNavigateToDashboard() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); isSaving = false }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            if (isSaving) CircularProgressIndicator(color = TechNavy, modifier = Modifier.size(24.dp)) else Text("SAVE ORDER", color = TechNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddOptions) {
        AlertDialog(
            onDismissRequest = { showAddOptions = false }, title = { Text("Add Product", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showAddOptions = false; showProductPicker = true }, modifier = Modifier.fillMaxWidth()) { Text("Search Manually") }
                    Button(onClick = { showAddOptions = false; showScanner = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) { Text("Scan Barcode", color = TechNavy) }
                }
            }, confirmButton = {}, containerColor = NavySurface
        )
    }

    if (showProductPicker) {
        AlertDialog(
            onDismissRequest = { showProductPicker = false }, title = { Text("Select Product", color = TextWhite) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(availableProducts) { p ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.addProduct(p); showProductPicker = false }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name, color = TextWhite)
                            Text(p.barcode, color = GoldAccent, fontSize = 12.sp)
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { showProductPicker = false }) { Text("Close") } }, containerColor = NavySurface
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismissRequest = { showScanner = false },
            onBarcodeScanned = { code ->
                val found = availableProducts.find { it.barcode == code }
                if (found != null) { viewModel.addProduct(found); Toast.makeText(context, "Added ${found.name}", Toast.LENGTH_SHORT).show() }
                else { Toast.makeText(context, "Product not found in DB", Toast.LENGTH_SHORT).show() }
                showScanner = false
            }
        )
    }
}
