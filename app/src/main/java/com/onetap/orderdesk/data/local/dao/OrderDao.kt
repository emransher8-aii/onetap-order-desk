package com.onetap.orderdesk.data.local.dao
import androidx.room.*
import com.onetap.orderdesk.data.local.entity.Order
import com.onetap.orderdesk.data.local.entity.OrderLine
import com.onetap.orderdesk.data.local.relation.OrderWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrder(order: Order): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrderLines(lines: List<OrderLine>): List<Long>
    
    @Transaction
    suspend fun insertOrderWithLines(order: Order, lines: List<OrderLine>): Long {
        val generatedOrderId = insertOrder(order)
        val linkedLines = lines.map { line -> line.copy(orderId = generatedOrderId) }
        insertOrderLines(linkedLines)
        return generatedOrderId
    }
    
    @Transaction @Query("SELECT * FROM orders ORDER BY date DESC") fun getAllOrdersWithLines(): Flow<List<OrderWithLines>>
    @Transaction @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1") suspend fun getOrderWithLinesById(orderId: Long): OrderWithLines?
    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId") suspend fun updateOrderStatus(orderId: Long, newStatus: String)
}
