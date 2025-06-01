package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap

import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import kotlin.math.min


class ObjectDetectionHelper(context: Context) {

    private val detector: ObjectDetector
    private val inputImageSize = 320

    init {

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(4)
            .setScoreThreshold(0.1f)
            .build()
        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "models/efficientdet.tflite",
            options
        )
    }

    /**
     * Center-crops the input [Bitmap] to a square and resizes it to
     * [inputImageSize] so it matches the 320×320 model input.
     */
    fun detect(bitmap: Bitmap): List<Detection> {
        val size = min(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2
        val squared = Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
        val resized = Bitmap.createScaledBitmap(
            squared,
            inputImageSize,
            inputImageSize,
            true
        )
        val image = TensorImage.fromBitmap(resized)
        return detector.detect(image)
    }
}
