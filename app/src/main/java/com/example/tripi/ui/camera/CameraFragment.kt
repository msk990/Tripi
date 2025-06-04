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

import com.example.tripi.ml.ObjectDetectionHelper


import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.example.tripi.utils.BitmapUtils.rotateBitmap
import com.example.tripi.utils.BitmapUtils.resizeWithAspectRatioAndPadding

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetectorHelper: ObjectDetectionHelper
    @Volatile // Ensure visibility across threads
    private var lastProcessedTimestampMs: Long = 0L
    private val frameProcessingIntervalMs: Long = 200L // Process roughly every 1 second (1000 ms)
    private val modelInputSize = 320

//    private val glThread = HandlerThread("GLThread").apply { start() }
//    private val glHandler = Handler(glThread.looper)

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


private fun processImageProxy(imageProxy: ImageProxy) {
    try {
        val bitmap = YuvtoRGBConverter.convert(imageProxy)
        val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
        val resized = resizeWithAspectRatioAndPadding(rotated, 320)
        val results = objectDetectorHelper.detectFormatted(resized)

        requireActivity().runOnUiThread {
            binding.overlay.update(results, resized.width, resized.height)
        }

    } catch (e: Exception) {
        Log.e("CameraFragment", "Processing failed", e)
    } finally {
        imageProxy.close()
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
//        glThread.quitSafely()
    }
    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
}

