package com.onetap.orderdesk.ui.orders
import java.util.Locale
fun formatMoney(minorUnits: Long): String {
    val dollars = minorUnits / 100
    val cents = Math.abs(minorUnits % 100)
    return String.format(Locale.US, "%d.%02d", dollars, cents)
}
