package com.example.safesphere.SafetyMap.Data

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class ReportClusterItem(
    val id: Int,
    val titleText: String,
    val itemPosition: LatLng,
    val impactLevel: String="LOW"
): ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String = titleText
    override fun getSnippet(): String = "Impact: $impactLevel"
    override fun getZIndex(): Float = 0f

    val radiusMeters: Double=when (impactLevel.uppercase()) {
        "HIGH" -> 300.0
        "MEDIUM" -> 150.0
        else -> 75.0
    }
    val zoneColor: Color=when (impactLevel.uppercase()) {
        "HIGH" -> Color.Red
        "MEDIUM" -> Color(0xFFFFA500) // Orange
        else -> Color.Yellow
    }
}