package com.onetap.orderdesk.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_lines",
    foreignKeys = [ForeignKey(entity = Order::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["orderId"]), Index(value = ["productId"])]
)
data class OrderLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderId: Long = 0L,
    val productId: Int,
    val productName: String,
    val barcode: String,
    val quantity: Int,
    val rate: Long,
    val discount: Long = 0L,
    val lineTotal: Long
)
