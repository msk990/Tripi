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
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var objectDetector: ObjectDetector
    private lateinit var classifier: ImageClassifier

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .build()
        objectDetector = ObjectDetection.getClient(options)

        val classifierOptions = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(1)
            .build()
        classifier = ImageClassifier.createFromFileAndOptions(
            requireContext(),
            "models/1.tflite",
            classifierOptions
        )
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
            val imageAnalyzer = ImageAnalysis.Builder().build().also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(imageProxy)
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
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)
            val bitmap = imageProxyToBitmap(imageProxy)
            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    val results = mutableListOf<DetectionResult>()
                    for (obj in objects) {
                        val box = obj.boundingBox
                        val objectBitmap = cropToBoundingBox(bitmap, box)
                        val tensorImage = TensorImage.fromBitmap(objectBitmap)
                        val classification = classifier.classify(tensorImage)
                        val category = classification.firstOrNull()?.categories?.firstOrNull()
                        val label = category?.label
                        val score = category?.score
                        results.add(DetectionResult(box, label, score))
                    }
                    binding.overlay.update(results, image.width, image.height)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Object detection failed", e)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val yuv = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(yuv, 0, yuv.size)

        val matrix = Matrix()
        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        return bitmap
    }

    private fun cropToBoundingBox(bitmap: Bitmap, box: Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            box.left.coerceAtLeast(0),
            box.top.coerceAtLeast(0),
            box.width().coerceAtMost(bitmap.width - box.left),
            box.height().coerceAtMost(bitmap.height - box.top)
        )
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

    companion object {
        private const val TAG = "CameraFragment"
        private const val REQUEST_CAMERA_PERMISSION = 10
    }
}
