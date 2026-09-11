package com.example.safesphere.Evidence.UI

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.safesphere.Evidence.API.SupabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

enum class EvidenceVaultType {
    PHOTO, AUDIO
}

data class VaultEvidenceItem(
    val id: String,
    val title: String,
    val type: EvidenceVaultType,
    val url: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceVaultScreen(
    onNavigateBack: () -> Unit = {},
    onStartNewEvidence: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") } // "All", "Photos", "Audios"
    var selectedItem by remember { mutableStateOf<VaultEvidenceItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(1) }

    var evidenceList by remember { mutableStateOf<List<VaultEvidenceItem>>(emptyList()) }

    // Media Player State for Audio Playback
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    fun stopAudioPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isAudioPlaying = false
    }

    fun playAudio(audioUrlOrPath: String) {
        try {
            stopAudioPlayback()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioUrlOrPath))
                prepareAsync()
                setOnPreparedListener {
                    start()
                    isAudioPlaying = true
                }
                setOnCompletionListener {
                    isAudioPlaying = false
                }
            }
        } catch (e: Exception) {
            Log.e("SafeSphere", "AUDIO PLAYBACK FAILED: ${e.message}", e)
            Toast.makeText(context, "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadAudioToPhone(urlString: String, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SafeSphere")
                        put(MediaStore.Audio.Media.IS_PENDING, 1)
                    }
                }

                val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (audioUri != null) {
                    val inputStream = URL(urlString).openStream()
                    resolver.openOutputStream(audioUri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val completedValues = ContentValues().apply {
                            put(MediaStore.Audio.Media.IS_PENDING, 0)
                        }
                        resolver.update(audioUri, completedValues, null, null)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Audio saved to Phone Music folder!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SafeSphere", "AUDIO DOWNLOAD FAILED", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download audio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loadEvidence() {
        scope.launch {
            isLoading = true
            val fetchedImages = SupabaseManager.fetchImageUrls()
            val fetchedAudios = SupabaseManager.fetchAudioUrls()

            val storageImages = SupabaseManager.fetchStorageImageUrls()
            val storageAudios = SupabaseManager.fetchStorageAudioUrls()

            // Strictly include ONLY valid Supabase cloud HTTP/HTTPS URLs
            val allImages = (fetchedImages + storageImages)
                .distinct()
                .filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }

            val allAudios = (fetchedAudios + storageAudios)
                .distinct()
                .filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }

            Log.d("SafeSphere", "SUPABASE VAULT SYNC: ${allImages.size} CLOUD IMAGES AND ${allAudios.size} CLOUD AUDIOS")

            val items = mutableListOf<VaultEvidenceItem>()
            var imgCount = 1
            var audCount = 1

            allImages.forEach { url ->
                items.add(
                    VaultEvidenceItem(
                        id = "img_$imgCount",
                        title = "Photo Evidence #${String.format("%03d", imgCount)}",
                        type = EvidenceVaultType.PHOTO,
                        url = url,
                        timestamp = "Photo • Cloud Supabase"
                    )
                )
                imgCount++
            }

            allAudios.forEach { url ->
                items.add(
                    VaultEvidenceItem(
                        id = "aud_$audCount",
                        title = "Audio Evidence #${String.format("%03d", audCount)}",
                        type = EvidenceVaultType.AUDIO,
                        url = url,
                        timestamp = "Audio • Cloud Supabase"
                    )
                )
                audCount++
            }

            evidenceList = items
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadEvidence()
    }

    val photoCount = evidenceList.count { it.type == EvidenceVaultType.PHOTO }
    val audioCount = evidenceList.count { it.type == EvidenceVaultType.AUDIO }

    val filteredList = evidenceList.filter { item ->
        val matchesCategory = when (selectedCategory) {
            "Photos" -> item.type == EvidenceVaultType.PHOTO
            "Audios" -> item.type == EvidenceVaultType.AUDIO
            else -> true
        }
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.timestamp.contains(searchQuery, ignoreCase = true) ||
                item.type.name.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
    }

    DisposableEffect(Unit) {
        onDispose {
            stopAudioPlayback()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9F9FB),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onStartNewEvidence() },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Active",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // HEADER SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Secured Storage",
                        fontSize = 14.sp,
                        color = Color(0xFF7A869A),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Evidence Vault",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF172B4D)
                    )
                }

                // Add button on top right
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .clickable { onStartNewEvidence() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Evidence",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search stored files...",
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFCBD5E1),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // CATEGORY FILTERS (All, Photos, Audios)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf(
                    "All" to "All (${evidenceList.size})",
                    "Photos" to "📷 Photos ($photoCount)",
                    "Audios" to "🎙️ Audios ($audioCount)"
                )

                items(categories.size) { index ->
                    val (key, label) = categories[index]
                    val isSelected = selectedCategory == key

                    Surface(
                        modifier = Modifier.clickable { selectedCategory = key },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REFRESH & LOADING BAR
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // EVIDENCE LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(filteredList) { _, item ->
                    EvidenceVaultCard(
                        item = item,
                        onClick = { selectedItem = item }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // DETAIL MODAL / PREVIEW DIALOG
    selectedItem?.let { item ->
        Dialog(onDismissRequest = {
            stopAudioPlayback()
            selectedItem = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = {
                            stopAudioPlayback()
                            selectedItem = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.type == EvidenceVaultType.PHOTO && item.url.isNotEmpty()) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (item.type == EvidenceVaultType.PHOTO) Color(0xFFF1F5F9) else Color(0xFFFFF7ED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.type == EvidenceVaultType.PHOTO) Icons.Default.Image else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (item.type == EvidenceVaultType.PHOTO) Color(0xFF64748B) else Color(0xFFF97316),
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Category: ${item.type.name} • ${item.timestamp}",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    if (item.url.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (item.type == EvidenceVaultType.AUDIO) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // PLAY / PAUSE BUTTON
                                Button(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            stopAudioPlayback()
                                        } else {
                                            playAudio(item.url)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(if (isAudioPlaying) "Pause" else "Play Audio")
                                }

                                // DOWNLOAD TO PHONE BUTTON
                                Button(
                                    onClick = {
                                        if (item.url.startsWith("http")) {
                                            val fileName = "SafeSphere_Audio_${System.currentTimeMillis()}.m4a"
                                            downloadAudioToPhone(item.url, fileName)
                                        } else {
                                            Toast.makeText(context, "File is already stored locally on phone!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text("Save Phone")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Open Full Image")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvidenceVaultCard(
    item: VaultEvidenceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT ICON BOX
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (item.type == EvidenceVaultType.PHOTO) Color(0xFFF1F5F9) else Color(0xFFFFF7ED)),
                contentAlignment = Alignment.Center
            ) {
                if (item.type == EvidenceVaultType.PHOTO && item.url.isNotEmpty()) {
                    AsyncImage(
                        model = item.url,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (item.type == EvidenceVaultType.PHOTO) Color(0xFFE2E8F0) else Color(0xFFFFEDD5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == EvidenceVaultType.PHOTO) Icons.Default.Image else Icons.Default.PlayArrow,
                            contentDescription = "Media",
                            tint = if (item.type == EvidenceVaultType.PHOTO) Color(0xFF475569) else Color(0xFFF97316),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT DETAILS COLUMN
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // BADGE
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.type == EvidenceVaultType.PHOTO) Color(0xFFDCFCE7) else Color(0xFFFFEDD5)
                ) {
                    Text(
                        text = if (item.type == EvidenceVaultType.PHOTO) "Photo" else "Audio",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.type == EvidenceVaultType.PHOTO) Color(0xFF166534) else Color(0xFF9A3412)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // TITLE
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // TIMESTAMP
                Text(
                    text = item.timestamp,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
