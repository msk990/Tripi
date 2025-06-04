package com.example.tripi.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

object BitmapUtils {

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun resizeWithAspectRatioAndPadding(bitmap: Bitmap, targetSize: Int): Bitmap {
        val scale = targetSize.toFloat() / max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        val scaled = bitmap.scale(newWidth, newHeight)
        val output = createBitmap(targetSize, targetSize)

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
            Log.d("BitmapUtils", "Saved image to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("BitmapUtils", "Failed to save image", e)
        }
    }
}
