package com.example.tripi.ui.camera

import android.graphics.Rect

/**
 * Holds detection result with bounding box, label and confidence score.
 */
data class DetectionResult(
    val boundingBox: Rect,
    val label: String?,
    val score: Float?
)
