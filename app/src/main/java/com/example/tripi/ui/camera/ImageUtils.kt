package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.camera.core.YuvToRgbConverter

fun ImageProxy.toBitmap(context: Context): Bitmap {
    val yuvToRgbConverter = YuvToRgbConverter(context)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    yuvToRgbConverter.yuvToRgb(image!!, bitmap)
    return bitmap
}
