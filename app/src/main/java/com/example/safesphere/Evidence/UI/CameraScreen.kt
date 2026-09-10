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

@Composable
fun CameraScreen() {

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
     * AUTOMATIC MULTIPLE PHOTO CAPTURE
     */
    LaunchedEffect(cameraReady) {

        if (cameraReady && !automaticCaptureStarted) {
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
                            "📸 Photo $capturedCount of $totalPhotos captured"

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
                "✅ 4 emergency photos saved!"

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
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = {
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = if (capturedCount == 0) {
                    "🚨 Emergency Camera Active"
                } else {
                    "📸 Captured $capturedCount / 4 photos"
                }
            )

            if (capturedBitmap != null) {

                Image(
                    bitmap =
                        capturedBitmap!!.asImageBitmap(),

                    contentDescription =
                        "Latest emergency photo",

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                )
            }
        }

        /*
         * MANUAL TEST BUTTON
         */
        Button(
            onClick = {

                captureAndSavePhoto(
                    context = context,
                    imageCapture = imageCapture
                ) { bitmap ->

                    if (bitmap != null) {

                        capturedBitmap = bitmap
                        capturedCount++

                        EvidenceState.currentStatus =
                            "📸 Manual photo saved!"

                    } else {

                        EvidenceState.currentStatus =
                            "❌ Photo capture failed"
                    }
                }

            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {

            Text("Capture Photo")
        }
    }
}


/*
 * CAPTURE PHOTO AND SAVE TO GALLERY
 */
private fun captureAndSavePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Bitmap?) -> Unit
) {

    val timestamp =
        SimpleDateFormat(
            "yyyyMMdd_HHmmss_SSS",
            Locale.US
        ).format(Date())

    val fileName =
        "SafeSphere_$timestamp.jpg"

    /*
     * First capture into a temporary file.
     */
    val tempFile = File(
        context.cacheDir,
        fileName
    )

    val outputOptions =
        ImageCapture.OutputFileOptions
            .Builder(tempFile)
            .build()

    imageCapture.takePicture(
        outputOptions,

        ContextCompat.getMainExecutor(context),

        object : ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(
                outputFileResults:
                ImageCapture.OutputFileResults
            ) {

                /*
                 * Save the captured image
                 * permanently into Gallery.
                 */
                Log.d(
                    "SafeSphere",
                    "PHOTO CAPTURE SUCCESS"
                )
                saveToGallery(
                    context = context,
                    file = tempFile,
                    fileName = fileName
                )

                /*
                 * Load image so we can show
                 * the latest photo on screen.
                 */

                CoroutineScope(Dispatchers.IO).launch {

                    try {

                        val url = SupabaseManager.uploadFile(tempFile)

                        SupabaseManager.saveEvidenceUrl(url)

                        Log.d(
                            "SafeSphere",
                            "PHOTO URL SAVED: $url"
                        )

                    } catch (e: Exception) {

                        Log.e(
                            "SafeSphere",
                            "UPLOAD FAILED: ${e.message}"
                        )

                    } finally {

                        tempFile.delete()
                    }
                }
                val bitmap =
                    BitmapFactory.decodeFile(
                        tempFile.absolutePath
                    )

                onCaptured(bitmap)


            }

            override fun onError(
                exception: ImageCaptureException
            ) {
                Log.e(
                    "SafeSphere",
                    "PHOTO FAILED: ${exception.message}"
                )
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
