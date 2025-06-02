package com.example.tripi

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ImageUtils {

    /** Converts [ImageProxy] to a color-accurate, upright [Bitmap]. */
    fun imageProxyToBitmap(context: Context, imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image is null")
        val bitmap = yuvToRgb(context, image)

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            rotateBitmap(bitmap, rotationDegrees)
        } else {
            bitmap
        }
    }

    /** Converts [Image] in YUV_420_888 format to [Bitmap] using NV21 + YuvImage + JPEG decode. */
    fun yuvToRgb(context: Context, image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpegBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    /** Rotates a [Bitmap] by [rotationDegrees] clockwise. */
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

        fun imageProxyToBitmap(image: ImageProxy): Bitmap {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            // Copy Y
            yBuffer.get(nv21, 0, ySize)
            // Copy VU interleaved (NV21 format)
            val chromaRowStride = image.planes[1].rowStride
            val chromaPixelStride = image.planes[1].pixelStride

            var offset = ySize
            for (row in 0 until image.height / 2) {
                for (col in 0 until image.width / 2) {
                    val vuPos = row * chromaRowStride + col * chromaPixelStride
                    nv21[offset++] = vBuffer.get(vuPos) // V
                    nv21[offset++] = uBuffer.get(vuPos) // U
                }
            }

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
            val jpegBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

            image.close()
            return bitmap
        }

    fun imageProxyToBitmap2(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y
        yBuffer.get(nv21, 0, ySize)
        // Copy VU interleaved (NV21 format)
        val chromaRowStride = image.planes[1].rowStride
        val chromaPixelStride = image.planes[1].pixelStride

        var offset = ySize
        for (row in 0 until image.height / 2) {
            for (col in 0 until image.width / 2) {
                val vuPos = row * chromaRowStride + col * chromaPixelStride
                if (vuPos < vBuffer.limit() && vuPos < uBuffer.limit()) {
                    nv21[offset++] = vBuffer.get(vuPos)
                    nv21[offset++] = uBuffer.get(vuPos)
                } else {
                    // Optionally log or pad with dummy values to avoid crash
                    nv21[offset++] = 127 // neutral V
                    nv21[offset++] = 127 // neutral U
                }
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpegBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)


        Log.d("ImageProxy", "PixelStride: ${image.planes[1].pixelStride}, RowStride: ${image.planes[1].rowStride}")
        image.close()
        return bitmap
    }

    fun rotateBitmap1(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
