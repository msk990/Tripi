package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap

import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp

class ObjectDetectionHelper(context: Context) {

    private val detector: ObjectDetector

    init {

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(4)
            .setScoreThreshold(0.5f)
            .build()
        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "models/efficientdet.tflite",
            options
        )
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        val image = TensorImage.fromBitmap(bitmap)
        return detector.detect(image)
    }
}
