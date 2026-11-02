package com.onetap.orderdesk.data.local.entity
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products", indices = [Index(value = ["barcode"], unique = true)])
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val barcode: String,
    val description: String? = null,
    val color: String? = null,
    val size: String? = null,
    val notes: String? = null
)
