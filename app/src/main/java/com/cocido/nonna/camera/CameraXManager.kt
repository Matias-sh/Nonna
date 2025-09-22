package com.cocido.nonna.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para CameraX que maneja la captura de fotos
 */
@Singleton
class CameraXManager @Inject constructor() {
    
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    
    fun initialize() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // ImageCapture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Seleccionar cámara trasera por defecto
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // Unbind use cases before rebinding
                    cameraProvider?.unbindAll()
                    
                    // Bind use cases to camera
                    camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    
                } catch (exc: Exception) {
                    onError("Error al iniciar la cámara: ${exc.message}")
                }
                
            } catch (exc: Exception) {
                onError("Error al inicializar CameraX: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    fun takePhoto(
        context: Context,
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("ImageCapture no está inicializado")
            return
        }
        
        // Crear archivo para la foto
        val photoFile = createPhotoFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Tomar la foto
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    onError("Error al tomar la foto: ${exception.message}")
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }
            }
        )
    }
    
    fun switchCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onError: (String) -> Unit
    ) {
        val cameraProvider = cameraProvider ?: return
        
        val currentCameraSelector = camera?.cameraInfo?.cameraSelector
        val newCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        try {
            cameraProvider.unbindAll()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                newCameraSelector,
                preview,
                imageCapture
            )
            
        } catch (exc: Exception) {
            onError("Error al cambiar de cámara: ${exc.message}")
        }
    }
    
    fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        imageCapture = null
    }
    
    fun cleanup() {
        stopCamera()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
    
    private fun createPhotoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.getExternalFilesDir(null), "photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "NONNA_${timeStamp}.jpg")
    }
}


