package com.example.reminderapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.reminderapp.ui.components.TopAppBarComponent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onPhotoTaken: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Take Photo",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    if (capturedImageUri != null) {
                        PhotoPreviewContent(
                            imageUri = capturedImageUri!!,
                            onRetake = { capturedImageUri = null },
                            onConfirm = {
                                onPhotoTaken(capturedImageUri.toString())
                            }
                        )
                    } else {
                        CameraPreviewContent(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            onImageCaptureReady = { imageCapture = it },
                            onCapture = {
                                imageCapture?.let { capture ->
                                    takePicture(
                                        context = context,
                                        imageCapture = capture,
                                        onImageSaved = { uri ->
                                            capturedImageUri = uri
                                        },
                                        onError = { exception ->
                                            android.util.Log.e("CameraScreen", "Capture failed", exception)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }

                else -> {
                    PermissionRequestContent(
                        shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCapture: () -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()

                    onImageCaptureReady(imageCapture)

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CameraPreview", "Binding failed", e)
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = onCapture,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Take photo",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun PhotoPreviewContent(
    imageUri: Uri,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured photo",
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = onRetake,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(Icons.Default.Close, contentDescription = "Retake")
            }

            FloatingActionButton(
                onClick = onConfirm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Use photo")
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (shouldShowRationale) {
                "Camera permission is needed to take photos for your reminders. Please grant permission in settings."
            } else {
                "This app needs access to your camera to take photos."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onImageSaved: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.getExternalFilesDir(null),
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageSaved(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
