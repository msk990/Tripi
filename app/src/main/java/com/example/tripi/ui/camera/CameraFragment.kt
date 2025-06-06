package com.example.tripi.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripi.databinding.FragmentCameraBinding
import com.example.tripi.ml.ObjectDetectionHelper
import com.example.tripi.ml.DetectionResult
import com.example.tripi.stickers.model.StickerAssetMap
import com.example.tripi.stickers.model.stickerTypeMap
import com.example.tripi.stickers.ui.StickerOverlayManager
import com.example.tripi.stickers.ui.StickerPlacementManager
import com.example.tripi.ui.camera.utils.StickerManager
import com.example.tripi.ui.camera.utils.scaleBox
import com.example.tripi.utils.BitmapUtils.rotateBitmap
import com.example.tripi.utils.BitmapUtils.resizeWithAspectRatioAndPadding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetectorHelper: ObjectDetectionHelper
    private lateinit var stickerOverlayManager: StickerOverlayManager
    private lateinit var stickerPlacementManager: StickerPlacementManager


    @Volatile
    private var lastProcessedTimestampMs: Long = 0L
    private val frameProcessingIntervalMs: Long = 200L
    private val modelInputSize = 320

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        objectDetectorHelper = ObjectDetectionHelper(requireContext())

        stickerOverlayManager = StickerOverlayManager(
            binding.overlayContainer,
            requireContext(),
            binding.konfettiView
        )
        stickerPlacementManager = StickerPlacementManager(binding.overlayContainer, stickerOverlayManager)


        val assetMap = StickerAssetMap.loadFromJson(requireContext())
        StickerManager.loadFromAssets(requireContext(), assetMap)


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
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val currentTimeMs = System.currentTimeMillis()
                        if (currentTimeMs - lastProcessedTimestampMs >= frameProcessingIntervalMs) {
                            lastProcessedTimestampMs = currentTimeMs
                            processImageProxy(imageProxy)
                        } else {
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
            val resized = resizeWithAspectRatioAndPadding(rotated, modelInputSize)
            val results = objectDetectorHelper.detectFormatted(resized)
            Log.d("CameraFragment", "Detected ${results.size} objects")

            requireActivity().runOnUiThread {
                stickerPlacementManager.showStickers(results, resized.width, resized.height)

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
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
}
