package com.example.tripi.ui.camera

import android.graphics.Rect

data class ClassifiedObject(
    val boundingBox: Rect,
    val label: String,
    val score: Float
)
