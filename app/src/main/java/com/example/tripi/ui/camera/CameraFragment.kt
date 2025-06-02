package com.example.tripi.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripi.databinding.FragmentCameraBinding
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.example.tripi.ml.ObjectDetectionHelper

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.example.tripi.ImageUtils.rotateBitmap1
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetectorHelper: ObjectDetectionHelper
    @Volatile // Ensure visibility across threads
    private var lastProcessedTimestampMs: Long = 0L
    private val frameProcessingIntervalMs: Long = 200L // Process roughly every 1 second (1000 ms)
    private val modelInputSize = 320

    private val glThread = HandlerThread("GLThread").apply { start() }
    private val glHandler = Handler(glThread.looper)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()

        objectDetectorHelper = ObjectDetectionHelper(requireContext())
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                // Model expects 320x320 input, so we could set it explicitly if desired
                // .setTargetResolution(Size(modelInputSize, modelInputSize))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Important!
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val currentTimeMs = System.currentTimeMillis()
                        if (currentTimeMs - lastProcessedTimestampMs >= frameProcessingIntervalMs) {
                            // --- Time to process this frame ---
                            lastProcessedTimestampMs = currentTimeMs


                            processImageProxy(imageProxy)

                        } else {
                            // Not time to process yet, close the image to release it
                            imageProxy.close()
                        }
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }

//    private fun processImageProxy(imageProxy: ImageProxy) {
//        val bitmap = imageProxyToBitmap(imageProxy)
//        val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
//        val results: List<Detection> = objectDetectorHelper.detect(rotated)
//        binding.overlay.update(results, rotated.width, rotated.height)
//        imageProxy.close()
//    }
private fun processImageProxy(imageProxy: ImageProxy) {
    glHandler.post {
        try {
            Log.d("GpuConvert", "Starting OpenGL conversion on thread: ${Thread.currentThread().name}")

            val bitmap = GpuYuvConverter.convert(imageProxy)

            // You probably want to run this on the main thread:
            requireActivity().runOnUiThread {
                val rotated = rotateBitmap1(bitmap, imageProxy.imageInfo.rotationDegrees)
                val resized = resizeWithAspectRatioAndPadding(rotated, 320)
                val results = objectDetectorHelper.detect(resized)
                binding.overlay.update(results, resized.width, resized.height)
                saveBitmapToInternalStorage(requireContext(), resized)
            }

        } catch (e: Exception) {
            Log.e("GpuConvert", "Conversion failed", e)
        } finally {
            imageProxy.close()
        }
    }
}


    fun resizeWithAspectRatioAndPadding(bitmap: Bitmap, targetSize: Int): Bitmap {
        val scale = targetSize.toFloat() / max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val dx = ((targetSize - newWidth) / 2).toFloat()
        val dy = ((targetSize - newHeight) / 2).toFloat()

        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(scaled, dx, dy, null)

        return output
    }
    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        filename: String = "model_input_${System.currentTimeMillis()}.png"
    ) {
        try {
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Log.d("ImageDebug", "Saved image to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("ImageDebug", "Failed to save image: ${e.message}", e)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        glThread.quitSafely()
    }
    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
}

