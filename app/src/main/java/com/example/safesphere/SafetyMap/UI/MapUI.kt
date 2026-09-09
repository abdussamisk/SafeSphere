package com.example.safesphere.SafetyMap.UI

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safesphere.SafetyMap.ViewModels.MapsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.safesphere.R
import kotlin.collections.maxByOrNull

private object SafeSphereColors {
    val BackgroundCream = Color(0xFFF3F1EA)
    val CardSurface = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF2C2A26)
    val TextGray = Color(0xFF8A8A85)
    val PrimaryGreen = Color(0xFF2F4A3B)
    val PrimaryGreenDark = Color(0xFF223A2E)
    val SafeGreen = Color(0xFF4C8062)
    val WarningAmber = Color(0xFFD9932E)
    val DangerRed = Color(0xFFC1503F)
    val Divider = Color(0xFFE3E0D6)
}

@Composable
fun Map(
    viewModel: MapsViewModel = viewModel()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            viewModel.getLocation(context)
        }
        viewModel.get_reports()
    }

    LaunchedEffect(viewModel.lat, viewModel.lon) {
        if (viewModel.lat != 0.0 && viewModel.lon != 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(viewModel.lat, viewModel.lon),
                15f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SafeSphereColors.BackgroundCream)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = remember {
                    try {
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                    } catch (e: Exception) {
                        null
                    }
                }
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = false
            )
        ) {
            viewModel.clusterItems.forEach { item ->
                val isHigh = item.impactLevel.uppercase() == "HIGH"
                val zoneColor = item.zoneColor.toSafeSphereSeverityColor()
                val fillAlpha = if (isHigh) rememberPulseAlpha(true) else 0.16f

                Circle(
                    center = item.itemPosition,
                    radius = item.radiusMeters,
                    fillColor = zoneColor.copy(alpha = fillAlpha),
                    strokeColor = zoneColor.copy(alpha = if (isHigh) 0.9f else 0.6f),
                    strokeWidth = if (isHigh) 3f else 1.5f
                )
            }

            Clustering(
                items = viewModel.clusterItems,
                clusterContent = { cluster ->
                    val severityOrder = mapOf("LOW" to 0, "MEDIUM" to 1, "HIGH" to 2)
                    val dominant = cluster.items
                        .maxByOrNull { severityOrder[it.impactLevel.uppercase()] ?: 0 }
                        ?.zoneColor?.toSafeSphereSeverityColor() ?: SafeSphereColors.DangerRed

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(4.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(SafeSphereColors.PrimaryGreen)
                            .border(2.dp, dominant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cluster.size.toString(),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                clusterItemContent = { item ->
                    val isHigh = item.impactLevel.uppercase() == "HIGH"
                    val zoneColor = item.zoneColor.toSafeSphereSeverityColor()

                    Box(
                        modifier = Modifier
                            .size(if (isHigh) 32.dp else 26.dp)
                            .shadow(3.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(zoneColor)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isHigh) {
                            Icon(
                                painter = painterResource(R.drawable.priority_high_24),
                                contentDescription = "High impact report",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            )
        }

        SafetyLegendCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        )

        FloatingActionButton(
            onClick = {
                if (hasLocationPermission && viewModel.lat != 0.0 && viewModel.lon != 0.0) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(viewModel.lat, viewModel.lon),
                        15f
                    )
                }
            },
            containerColor = SafeSphereColors.PrimaryGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Recenter on my location"
            )
        }
    }
}

@Composable
private fun SafetyLegendCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(20.dp), clip = false),
        shape = RoundedCornerShape(20.dp),
        color = SafeSphereColors.CardSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Live Safety Reports",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SafeSphereColors.TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Nearby reports from your area",
                fontSize = 12.sp,
                color = SafeSphereColors.TextGray
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendDot(color = SafeSphereColors.SafeGreen, label = "Low")
                LegendDot(color = SafeSphereColors.WarningAmber, label = "Medium")
                LegendDot(color = SafeSphereColors.DangerRed, label = "High")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SafeSphereColors.TextDark
        )
    }
}

private fun Color.toSafeSphereSeverityColor(): Color {
    return when {
        red > 0.7f && green < 0.5f -> SafeSphereColors.DangerRed
        red > 0.7f && green >= 0.5f -> SafeSphereColors.WarningAmber
        else -> SafeSphereColors.SafeGreen
    }
}

@Composable
fun rememberPulseAlpha(isActive: Boolean): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "riskPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    return if (isActive) pulse else 0.18f
}

@Preview(showBackground = true)
@Composable
fun MapPreview() {
    Map()
}