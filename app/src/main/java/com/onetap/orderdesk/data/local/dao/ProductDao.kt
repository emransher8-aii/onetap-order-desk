package com.onetap.orderdesk.data.local.dao
import androidx.room.*
import com.onetap.orderdesk.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProduct(product: Product): Long
    @Update suspend fun updateProduct(product: Product)
    @Delete suspend fun deleteProduct(product: Product)
    @Query("SELECT * FROM products ORDER BY id DESC") fun getAllProducts(): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY id DESC") fun searchProducts(query: String): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1") suspend fun getProductByBarcode(barcode: String): Product?
}
