package com.onetap.orderdesk.ui.orders
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onetap.orderdesk.data.local.OrderDeskDatabase
import com.onetap.orderdesk.data.local.relation.OrderWithLines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val orderDao = OrderDeskDatabase.getDatabase(application).orderDao()
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("All")
    val allOrders: StateFlow<List<OrderWithLines>> = orderDao.getAllOrdersWithLines().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val filteredOrders = combine(allOrders, searchQuery, statusFilter) { orders, query, filter ->
        orders.filter { o ->
            val matchFilter = if (filter == "All") true else o.order.status.equals(filter, true)
            val matchQuery = if (query.isBlank()) true else o.order.customerName.contains(query, true) || o.order.phone.contains(query) || o.order.id.toString().contains(query)
            matchFilter && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateOrderStatus(orderId: Long, newStatus: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch { withContext(Dispatchers.IO) { orderDao.updateOrderStatus(orderId, newStatus) }; onComplete() }
    }
}
