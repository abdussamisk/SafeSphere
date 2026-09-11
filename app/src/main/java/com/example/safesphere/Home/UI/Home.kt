package com.example.safesphere.Home.UI

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.safesphere.Authy.TokenRepository.UserRepository
import com.example.safesphere.Evidence.Helper.AudioHelper
import com.example.safesphere.Evidence.Service.EmergencyListeningService
import com.example.safesphere.Evidence.State.EvidenceState
import com.example.safesphere.Evidence.Voice.VoiceRecognizerHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safesphere.SOSLocationSharing.data.API.RetrofitClient as SOSRetrofitClient
import com.example.safesphere.SOSLocationSharing.data.repository.SOSRepository
import com.example.safesphere.SOSLocationSharing.ui.viewModel.SOSViewModel
import com.example.safesphere.SOSLocationSharing.ui.viewModel.SOSViewModelFactory
import com.example.safesphere.SOSLocationSharing.utils.LocationHelper

@Composable
fun Home(
    onNavigateToMap: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToGuardian: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    val usernameState by userRepository.username.collectAsState(initial = null)
    val displayName = remember(usernameState) {
        if (!usernameState.isNullOrBlank()) usernameState else "Mamitha"
    }

    val locationHelper = remember { LocationHelper(context) }
    val sosRepository = remember { SOSRepository(SOSRetrofitClient.api) }
    val sosViewModel: SOSViewModel = viewModel(
        factory = SOSViewModelFactory(sosRepository)
    )

    val sosPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        sosViewModel.activateSOS(context, locationHelper)
    }

    var isSafetyActive by remember { mutableStateOf(EvidenceState.safetyModeEnabled) }
    val audioHelper = remember { AudioHelper(context) }
    val voiceHelper = remember { VoiceRecognizerHelper(context, audioHelper) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && isSafetyActive) {
            voiceHelper.startVoiceRecognition()
        }
    }

    fun startEvidenceService() {
        EvidenceState.safetyModeEnabled = true
        EvidenceState.currentStatus = "🟢 Safety Mode ON"
        isSafetyActive = true

        val intent = Intent(context, EmergencyListeningService::class.java)
        ContextCompat.startForegroundService(context, intent)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            voiceHelper.startVoiceRecognition()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopEvidenceService() {
        EvidenceState.safetyModeEnabled = false
        EvidenceState.currentStatus = "🔴 Safety Mode OFF"
        isSafetyActive = false

        voiceHelper.stopVoiceRecognition()

        val intent = Intent(context, EmergencyListeningService::class.java)
        context.stopService(intent)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        // TOP BAR: Welcome Greeting & Title + Toggle Switch (Mapped to Evidence Package)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome, $displayName 👋",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SafeSphere",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }

            // TOP RIGHT EVIDENCE TOGGLE SWITCH
            Switch(
                checked = isSafetyActive,
                onCheckedChange = { checked ->
                    if (checked) {
                        startEvidenceService()
                        Toast.makeText(context, "Evidence Protection Activated", Toast.LENGTH_SHORT).show()
                    } else {
                        stopEvidenceService()
                        Toast.makeText(context, "Evidence Protection Paused", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF0F172A),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STATUS CARD ("SYSTEM ACTIVE / You Are Safe")
        val statusCardBg by animateColorAsState(
            targetValue = if (isSafetyActive) Color(0xFFFAFAFA) else Color(0xFFF8FAFC),
            label = "statusBg"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp), clip = false),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = statusCardBg),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSafetyActive) "SYSTEM ACTIVE" else "SYSTEM INACTIVE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafetyActive) Color(0xFF10B981) else Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSafetyActive) "You Are Safe" else "Protection Off",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isSafetyActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSafetyActive) Icons.Default.Shield else Icons.Default.CheckCircle,
                        contentDescription = "System Status",
                        tint = if (isSafetyActive) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SAFE ZONE MAPS CARD CONTAINER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFECE6))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Stylized Map Canvas Background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // River path
                    val riverPath = Path().apply {
                        moveTo(w * 0.15f, 0f)
                        cubicTo(w * 0.35f, h * 0.2f, w * 0.2f, h * 0.5f, w * 0.65f, h * 0.25f)
                        cubicTo(w * 0.8f, h * 0.15f, w * 0.85f, h * 0.05f, w, h * 0.1f)
                        lineTo(w, h * 0.35f)
                        cubicTo(w * 0.85f, h * 0.25f, w * 0.75f, h * 0.35f, w * 0.6f, h * 0.45f)
                        cubicTo(w * 0.2f, h * 0.7f, w * 0.3f, h * 0.35f, w * 0.05f, 0f)
                        close()
                    }
                    drawPath(riverPath, color = Color(0xFF1E293B).copy(alpha = 0.85f))

                    // Green Safe Zone Route Paths
                    val routePath1 = Path().apply {
                        moveTo(w * 0.2f, h * 0.7f)
                        lineTo(w * 0.35f, h * 0.55f)
                        lineTo(w * 0.52f, h * 0.62f)
                        lineTo(w * 0.75f, h * 0.45f)
                    }
                    drawPath(
                        routePath1,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 5f)
                    )

                    // Road Grid Lines
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(w * 0.1f, h * 0.2f),
                        end = Offset(w * 0.9f, h * 0.8f),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(w * 0.05f, h * 0.65f),
                        end = Offset(w * 0.8f, h * 0.3f),
                        strokeWidth = 5f
                    )

                    // Map Safe Haven Markers
                    val pins = listOf(
                        Offset(w * 0.41f, h * 0.53f),
                        Offset(w * 0.51f, h * 0.65f),
                        Offset(w * 0.63f, h * 0.58f)
                    )
                    pins.forEach { pin ->
                        drawCircle(
                            color = Color(0xFF10B981),
                            radius = 12f,
                            center = pin
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = pin
                        )
                    }
                }

                // Overlay Map Location Search bar graphic
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "🔍 Search Location...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                // FLOATING BOTTOM CARD (Safe Zone Maps Button)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(14.dp)
                        .fillMaxWidth()
                        .clickable { onNavigateToMap() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Safe Zone Maps",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "3 active safe havens nearby",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Map Navigation Button (Mapped to Maps UI)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Maps UI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SOS EMERGENCY BUTTON (CONCENTRIC PINK GLOW RINGS)
        var isSosPressed by remember { mutableStateOf(false) }
        val sosScale by animateFloatAsState(
            targetValue = if (isSosPressed) 0.94f else 1.0f,
            animationSpec = tween(150),
            label = "sosScale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring 1
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF1F2).copy(alpha = 0.7f))
            )
            // Middle Ring 2
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD6E0).copy(alpha = 0.85f))
            )
            // Inner Ring 3
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB3C6))
            )

            // Center SOS Touch Circle Button
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB7185))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isSosPressed = true
                                tryAwaitRelease()
                                isSosPressed = false
                            },
                            onTap = {
                                Toast
                                    .makeText(
                                        context,
                                        "🚨 SOS Triggered! Emergency Location & Evidence Broadcasting...",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()

                                val hasLocationPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                val hasSmsPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.SEND_SMS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasLocationPermission && hasSmsPermission) {
                                    sosViewModel.activateSOS(context, locationHelper)
                                } else {
                                    sosPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.SEND_SMS
                                        )
                                    )
                                    sosViewModel.activateSOS(context, locationHelper)
                                }

                                startEvidenceService()
                                onNavigateToCamera()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4C1D95),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "PRESS & HOLD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF881337)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK ACTION BUTTONS BELOW SOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action Button: Shield / Vault Security Icon
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToGuardian() },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Vault Security",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(40.dp))

            // Right Action Button: Camera Symbol (Mapped to Camera manual photo click & Supabase upload)
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToCamera() },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Open Camera & Upload to Supabase",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}