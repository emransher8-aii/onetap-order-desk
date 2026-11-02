package com.onetap.orderdesk.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.onetap.orderdesk.data.local.dao.OrderDao
import com.onetap.orderdesk.data.local.dao.ProductDao
import com.onetap.orderdesk.data.local.entity.Order
import com.onetap.orderdesk.data.local.entity.OrderLine
import com.onetap.orderdesk.data.local.entity.Product

@Database(entities = [Product::class, Order::class, OrderLine::class], version = 3, exportSchema = false)
abstract class OrderDeskDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    companion object {
        @Volatile private var INSTANCE: OrderDeskDatabase? = null
        fun getDatabase(context: Context): OrderDeskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, OrderDeskDatabase::class.java, "order_desk_database").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
