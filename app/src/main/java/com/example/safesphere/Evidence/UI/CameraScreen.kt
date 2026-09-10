package com.example.safesphere.Evidence.UI

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.safesphere.Evidence.API.SupabaseManager
import com.example.safesphere.Evidence.State.EvidenceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CameraScreen(
    onClose: () -> Unit = {},
    isAutoEmergencyMode: Boolean = EvidenceState.showCamera
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var cameraReady by remember {
        mutableStateOf(false)
    }

    var automaticCaptureStarted by remember {
        mutableStateOf(false)
    }

    var capturedCount by remember {
        mutableStateOf(0)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    if (!hasPermission) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Button(
                onClick = {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            ) {
                Text("Allow Camera")
            }
        }

        return
    }

    val previewView = remember {
        PreviewView(context)
    }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    /*
     * START CAMERA
     */
    LaunchedEffect(Unit) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            try {

                val cameraProvider =
                    cameraProviderFuture.get()

                val preview =
                    Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider =
                                previewView.surfaceProvider
                        }

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

                cameraReady = true

            } catch (exception: Exception) {

                EvidenceState.currentStatus =
                    "❌ Camera initialization failed"
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /*
     * AUTOMATIC MULTIPLE PHOTO CAPTURE (Only run if isAutoEmergencyMode or showCamera is true)
     */
    LaunchedEffect(cameraReady) {

        if ((isAutoEmergencyMode || EvidenceState.showCamera) && cameraReady && !automaticCaptureStarted) {
            Log.d(
                "SafeSphere",
                "AUTO CAPTURE STARTED"
            )
            automaticCaptureStarted = true

            // Give the camera time to stabilize
            delay(1000)

            val totalPhotos = 4

            for (i in 1..totalPhotos) {

                captureAndSavePhoto(
                    context = context,
                    imageCapture = imageCapture
                ) { bitmap ->

                    if (bitmap != null) {

                        capturedBitmap = bitmap

                        capturedCount++

                        EvidenceState.currentStatus =
                            "📸 Photo $capturedCount of $totalPhotos captured & uploading..."

                    } else {

                        EvidenceState.currentStatus =
                            "❌ Photo $capturedCount failed"
                    }
                }

                // Wait 2 seconds before next photo
                if (i < totalPhotos) {
                    delay(2000)
                }
            }

            EvidenceState.currentStatus =
                "✅ 4 emergency photos saved to Supabase!"

            delay(2000)

            EvidenceState.showCamera = false

            EvidenceState.currentStatus =
                "🟢 Safety Mode Active\nListening for emergency..."

            EvidenceState.restartListening = true
        }
    }

    /*
     * CAMERA UI
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        AndroidView(
            factory = {
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // TOP BAR: CLOSE / BACK BUTTON & STATUS BANNER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onClose()
                    EvidenceState.showCamera = false
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = if (capturedCount == 0) {
                        "📷 Camera Active • Ready"
                    } else {
                        "📸 Captured $capturedCount photo(s) • Saved & Uploaded"
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        /*
         * SHUTTER BUTTON (MANUAL CAPTURE & SUPABASE UPLOAD)
         */
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap to take photo & upload to Supabase",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = {
                    captureAndSavePhoto(
                        context = context,
                        imageCapture = imageCapture
                    ) { bitmap ->
                        if (bitmap != null) {
                            capturedBitmap = bitmap
                            capturedCount++
                            Toast.makeText(
                                context,
                                "📸 Photo #$capturedCount captured & saved to Vault!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "❌ Photo captured & uploading...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Capture Photo",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


/*
 * CAPTURE PHOTO AND SAVE TO GALLERY AND SUPABASE
 */
private fun captureAndSavePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Bitmap?) -> Unit
) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    val fileName = "SafeSphere_$timestamp.jpg"
    val tempFile = File(context.cacheDir, fileName)

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(tempFile)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Log.d("SafeSphere", "PHOTO CAPTURE SUCCESS: ${tempFile.absolutePath}")

                // 1. Decode bitmap from tempFile first
                val bitmap = try {
                    BitmapFactory.decodeFile(tempFile.absolutePath)
                } catch (e: Exception) {
                    Log.e("SafeSphere", "BITMAP DECODE ERROR: ${e.message}")
                    null
                }

                // 2. Save to Gallery
                saveToGallery(
                    context = context,
                    file = tempFile,
                    fileName = fileName
                )

                // 3. Callback to UI immediately
                onCaptured(bitmap)

                // 4. Asynchronously upload to Supabase, then delete temp file
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url = SupabaseManager.uploadFile(tempFile)
                        SupabaseManager.saveEvidenceUrl(url)
                        Log.d("SafeSphere", "PHOTO URL SAVED TO SUPABASE: $url")
                    } catch (e: Exception) {
                        Log.e("SafeSphere", "SUPABASE UPLOAD FAILED: ${e.message}", e)
                    } finally {
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("SafeSphere", "PHOTO CAPTURE ERROR: ${exception.message}", exception)
                onCaptured(null)
            }
        }
    )
}


/*
 * SAVE IMAGE INTO PHONE GALLERY
 */
private fun saveToGallery(
    context: Context,
    file: File,
    fileName: String
) {

    val resolver =
        context.contentResolver

    val contentValues =
        ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                fileName
            )

            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg"
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/SafeSphere"
                )

                put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                )
            }
        }

    val imageUri =
        resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

    if (imageUri != null) {

        resolver.openOutputStream(imageUri)?.use { outputStream ->

            file.inputStream().use { inputStream ->

                inputStream.copyTo(outputStream)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val completedValues =
                ContentValues().apply {

                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        0
                    )
                }

            resolver.update(
                imageUri,
                completedValues,
                null,
                null
            )
        }
    }
}
