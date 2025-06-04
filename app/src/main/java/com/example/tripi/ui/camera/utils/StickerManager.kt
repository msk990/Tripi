package com.example.tripi.ui.camera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale

object StickerManager {
    private val labelToSticker = mutableMapOf<String, Bitmap>()
    private val stickerCache = mutableMapOf<Pair<String, Int>, Bitmap>()

    fun registerSticker(label: String, bitmap: Bitmap) {
        labelToSticker[label.lowercase()] = bitmap
    }

    fun loadFromAssets(context: Context, labelToPath: Map<String, String>) {
        labelToPath.forEach { (label, path) ->
            context.assets.open(path).use {
                labelToSticker[label.lowercase()] = BitmapFactory.decodeStream(it)
            }
        }
    }

    fun getScaledSticker(label: String, width: Int): Bitmap? {
        val base = labelToSticker[label.lowercase()] ?: return null
        val key = label.lowercase() to width
        return stickerCache.getOrPut(key) {
            val scale = width.toFloat() / base.width
            base.scale(width, (base.height * scale).toInt())
        }
    }
}
