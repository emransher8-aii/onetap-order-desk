package com.onetap.orderdesk.data.local.relation
import androidx.room.Embedded
import androidx.room.Relation
import com.onetap.orderdesk.data.local.entity.Order
import com.onetap.orderdesk.data.local.entity.OrderLine

data class OrderWithLines(
    @Embedded val order: Order,
    @Relation(parentColumn = "id", entityColumn = "orderId") val lines: List<OrderLine>
)
