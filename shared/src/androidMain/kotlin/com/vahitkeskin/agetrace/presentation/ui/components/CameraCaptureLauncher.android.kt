package com.vahitkeskin.agetrace.presentation.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberCameraCaptureLauncher(
    onImageCaptured: (name: String, bytes: ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val tempImageUri = remember { mutableStateOf<Uri?>(null) }

    // High-resolution picture capture with FileProvider
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uri = tempImageUri.value
            if (uri != null) {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        val fileName = "kamera_${System.currentTimeMillis()}.jpg"
                        onImageCaptured(fileName, bytes)
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    // Direct bitmap preview launcher (fallback)
    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
                val bytes = stream.toByteArray()
                if (bytes.isNotEmpty()) {
                    val fileName = "kamera_${System.currentTimeMillis()}.jpg"
                    onImageCaptured(fileName, bytes)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    // Camera execution helper
    val launchCamera = {
        try {
            val cacheFile = File.createTempFile("camera_capture_", ".jpg", context.cacheDir)
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, cacheFile)
            tempImageUri.value = uri
            takePictureLauncher.launch(uri)
        } catch (t: Throwable) {
            t.printStackTrace()
            // Fallback to preview contract if FileProvider fails
            takePicturePreviewLauncher.launch(null)
        }
    }

    // Runtime Camera Permission Requester (Mandatory for Android 6.0+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "Kamera izni verilmedi. Fotoğraf çekmek için lütfen kamera iznini onaylayın.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    return {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
