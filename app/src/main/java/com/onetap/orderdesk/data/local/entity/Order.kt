package com.onetap.orderdesk.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerName: String,
    val phone: String,
    val whatsappNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val customerNotes: String? = null,
    val date: Long = System.currentTimeMillis(),
    val status: String = "PENDING",
    val subtotal: Long,
    val discount: Long = 0L,
    val deliveryCharges: Long = 0L,
    val grandTotal: Long,
    val paidAmount: Long = 0L,
    val notes: String? = null
)
