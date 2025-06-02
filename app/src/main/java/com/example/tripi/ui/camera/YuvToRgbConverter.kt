package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

class YuvToRgbConverter(context: Context) {


    private val yuvToRgb = android.graphics.ImageDecoder.OnHeaderDecodedListener { decoder, info, src ->
        decoder.setTargetSize(info.size.width, info.size.height)
    }

    fun convert(imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image is null")
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)

        image.use { img ->
            val planeY = img.planes[0].buffer
            val planeU = img.planes[1].buffer
            val planeV = img.planes[2].buffer

            val ySize = planeY.remaining()
            val uSize = planeU.remaining()
            val vSize = planeV.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            planeY.get(nv21, 0, ySize)
            planeV.get(nv21, ySize, vSize)
            planeU.get(nv21, ySize + vSize, uSize)

            val yuvImage = android.graphics.YuvImage(nv21, ImageFormat.NV21, img.width, img.height, null)
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, img.width, img.height), 100, out)

            val decoded = android.graphics.BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
            val matrix = android.graphics.Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }
    }
}
