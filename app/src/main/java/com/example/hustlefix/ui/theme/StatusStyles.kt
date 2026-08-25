package com.example.hustlefix.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getStatusColor(status: String?): Color {
    return when (status?.lowercase()) {
        "completed", "done" -> Color(0xFF4CAF50)
        "cancelled", "rejected", "failed" -> Color(0xFFF44336)
        "confirmed", "accepted", "in_progress", "paid", "quoted" -> Color(0xFF2196F3)
        "pending", "open" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
}

fun getStatusIcon(status: String?): ImageVector {
    return when (status?.lowercase()) {
        "completed", "done" -> Icons.Default.CheckCircle
        "cancelled", "rejected", "failed" -> Icons.Default.Cancel
        "confirmed", "accepted", "in_progress", "paid", "quoted" -> Icons.Default.Sync
        "pending", "open" -> Icons.Default.HourglassEmpty
        else -> Icons.Default.Info
    }
}
