package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetectionHelper(context: Context) {

    private val detector: ObjectDetector
    private val inputImageSize = 320 // Matches efficientdet-lite0

    init {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5) // Increased slightly for testing
            .setScoreThreshold(0.2f) // Keep low for testing
            // No need to set numThreads or delegates for now, let Task Lib use defaults
            .build()
        try {
            detector = ObjectDetector.createFromFileAndOptions(
                context,
                "models/efficientdet.tflite", // Make sure filename matches
                options
            )
        } catch (e: Exception) {
            // Consider proper error handling/logging for production
            throw RuntimeException("Error initializing ObjectDetector!", e)
        }
    }

    fun detect(bitmap: Bitmap, rotationDegrees: Int): List<Detection> {
        var tensorImage = TensorImage.fromBitmap(bitmap) // Bitmap (0-255) -> TensorImage (float32, 0-255)

        // 1. Rotate the TensorImage to be upright
        // The number of rotations needed by Rot90Op might need adjustment
        // depending on how rotationDegrees maps to 90-degree turns (clockwise/counter-clockwise)
        if (rotationDegrees != 0) {
            // Rot90Op typically rotates counter-clockwise.
            // Camera rotationDegrees are usually clockwise.
            // So, if rotationDegrees is 90 (clockwise), you need 3 counter-clockwise rotations (or -1 clockwise).
            val numRotations = -rotationDegrees / 90
            tensorImage = ImageProcessor.Builder().add(Rot90Op(numRotations)).build().process(tensorImage)
        }

        // 2. Preprocess for the model (crop to square 320x320)
        // Normalization to [-1,1] should be handled by the Task Library using model metadata.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(inputImageSize, inputImageSize))
            // NO NormalizeOp here - let metadata do its job
            .build()
        val processedTensorImage = imageProcessor.process(tensorImage)

        // The 'detector.detect()' call will use the metadata from the model
        // to perform any necessary input normalization (e.g., to [-1,1]).
        return detector.detect(processedTensorImage)
    }
}