package com.example.tripi.stickers.ui

import android.graphics.RectF
import android.util.Log
import android.widget.FrameLayout
import com.example.tripi.ml.DetectionResult
import com.example.tripi.ui.camera.utils.StickerManager
import com.example.tripi.ui.camera.utils.scaleBox

class StickerPlacementManager(
    private val overlay: FrameLayout,
    private val stickerOverlayManager: StickerOverlayManager
) {

    fun showStickers(results: List<DetectionResult>, imageWidth: Int, imageHeight: Int) {
        val scaleX = overlay.width.toFloat() / imageWidth.toFloat()
        val scaleY = overlay.height.toFloat() / imageHeight.toFloat()

        overlay.removeAllViews()

        for (result in results) {
            val label = result.label.lowercase()
            val box: RectF = scaleBox(result.boundingBox, scaleX, scaleY)
            val sticker = StickerManager.getScaledSticker(label, (box.width() * 0.8f).toInt())

            if (sticker != null) {
                stickerOverlayManager.showSticker(label, box, sticker)
            } else {
                Log.d("StickerPlacementManager", "No sticker for label: $label")
            }
        }
    }
}
