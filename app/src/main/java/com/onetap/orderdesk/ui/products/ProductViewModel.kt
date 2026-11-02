package com.onetap.orderdesk.ui.products
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onetap.orderdesk.data.local.OrderDeskDatabase
import com.onetap.orderdesk.data.local.entity.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val productDao = OrderDeskDatabase.getDatabase(application).productDao()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val products: StateFlow<List<Product>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) productDao.getAllProducts() else productDao.searchProducts(query.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun insertProduct(name: String, barcode: String, description: String? = null, color: String? = null, size: String? = null, notes: String? = null, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try { productDao.insertProduct(Product(name = name.trim(), barcode = barcode.trim(), description = description, color = color, size = size, notes = notes)); onComplete?.invoke(true, null) } 
            catch (e: Exception) { onComplete?.invoke(false, e.localizedMessage) }
        }
    }
    fun updateProduct(product: Product, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try { productDao.updateProduct(product); onComplete?.invoke(true, null) } catch (e: Exception) { onComplete?.invoke(false, e.localizedMessage) }
        }
    }
    fun deleteProduct(product: Product, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try { productDao.deleteProduct(product); onComplete?.invoke(true, null) } catch (e: Exception) { onComplete?.invoke(false, e.localizedMessage) }
        }
    }
}
