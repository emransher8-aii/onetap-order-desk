package com.onetap.orderdesk.ui.orders
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onetap.orderdesk.data.local.OrderDeskDatabase
import com.onetap.orderdesk.data.local.entity.Order
import com.onetap.orderdesk.data.local.entity.OrderLine
import com.onetap.orderdesk.data.local.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OrderLineUiState(val tempId: String, val productId: Int, val productName: String, val barcode: String, val quantity: Int = 1, val rate: Long = 1850L, val discount: Long = 0L) {
    val lineTotal: Long get() = (quantity.toLong() * rate) - minOf(quantity.toLong() * rate, discount)
}

class NewOrderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = OrderDeskDatabase.getDatabase(application)
    private val orderDao = db.orderDao()
    private val productDao = db.productDao()

    val customerName = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val whatsappNumber = MutableStateFlow("")
    val city = MutableStateFlow("")
    val address = MutableStateFlow("")
    val customerNotes = MutableStateFlow("")
    val deliveryCharges = MutableStateFlow(0L)
    val searchQuery = MutableStateFlow("")

    val availableProducts: StateFlow<List<Product>> = searchQuery.flatMapLatest { q -> if (q.isBlank()) productDao.getAllProducts() else productDao.searchProducts(q.trim()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _orderLines = MutableStateFlow<List<OrderLineUiState>>(emptyList())
    val orderLines = _orderLines.asStateFlow()
    val subtotal: StateFlow<Long> = _orderLines.map { lines -> lines.sumOf { it.lineTotal } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val grandTotal: StateFlow<Long> = combine(subtotal, deliveryCharges) { sub, del -> sub + del }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun addProduct(product: Product) {
        val current = _orderLines.value
        val idx = current.indexOfFirst { it.productId == product.id }
        if (idx >= 0) {
            val updated = current.toMutableList()
            updated[idx] = updated[idx].copy(quantity = updated[idx].quantity + 1)
            _orderLines.value = updated
        } else {
            _orderLines.value = current + OrderLineUiState(java.util.UUID.randomUUID().toString(), product.id, product.name, product.barcode)
        }
    }
    fun updateQuantity(tempId: String, qty: Int) { if (qty > 0) _orderLines.value = _orderLines.value.map { if (it.tempId == tempId) it.copy(quantity = qty) else it } }
    fun updateRate(tempId: String, rate: Long) { if (rate >= 0) _orderLines.value = _orderLines.value.map { if (it.tempId == tempId) it.copy(rate = rate) else it } }
    fun removeLine(tempId: String) { _orderLines.value = _orderLines.value.filter { it.tempId != tempId } }
    fun setDeliveryCharges(amount: Long) { deliveryCharges.value = maxOf(0L, amount) }
    
    fun clearOrder() {
        customerName.value = ""; phone.value = ""; whatsappNumber.value = ""; city.value = ""; address.value = ""; customerNotes.value = ""; deliveryCharges.value = 0L; _orderLines.value = emptyList()
    }

    fun saveOrder(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val name = customerName.value.trim()
        val lines = _orderLines.value
        if (name.isBlank()) { onError("Customer Name is required"); return }
        if (lines.isEmpty()) { onError("Please add at least one product"); return }

        viewModelScope.launch {
            try {
                val sub = lines.sumOf { it.lineTotal }
                val del = deliveryCharges.value
                val order = Order(customerName = name, phone = phone.value.trim().ifBlank { "N/A" }, whatsappNumber = whatsappNumber.value, address = address.value, city = city.value, customerNotes = customerNotes.value, subtotal = sub, discount = lines.sumOf { it.discount }, deliveryCharges = del, grandTotal = sub + del)
                val linesToInsert = lines.map { OrderLine(orderId = 0L, productId = it.productId, productName = it.productName, barcode = it.barcode, quantity = it.quantity, rate = it.rate, discount = it.discount, lineTotal = it.lineTotal) }
                val id = withContext(Dispatchers.IO) { orderDao.insertOrderWithLines(order, linesToInsert) }
                clearOrder()
                onSuccess(id)
            } catch (e: Exception) { onError(e.localizedMessage ?: "Error saving order") }
        }
    }
}
