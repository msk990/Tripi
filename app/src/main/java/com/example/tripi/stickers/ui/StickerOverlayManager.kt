package com.example.tripi.stickers.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.example.tripi.stickers.model.StickerType
import com.example.tripi.stickers.model.stickerTypeMap

import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class StickerOverlayManager(
    private val container: FrameLayout,
    private val context: Context,
    private val konfettiView: KonfettiView
) {

    fun showSticker(label: String, box: RectF, sticker: Bitmap) {
        when (stickerTypeMap[label]) {
            StickerType.COLLECTIBLE -> addCollectibleSticker(label, box, sticker)
            StickerType.INTERACTIVE -> addInteractiveSticker(label, box, sticker)
            else -> Log.w("StickerOverlay", "Unknown type for label: $label")
        }
    }

    private fun addCollectibleSticker(label: String, box: RectF, sticker: Bitmap) {
        val imageView = ImageView(context).apply {
            setImageBitmap(sticker)
            layoutParams = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                leftMargin = (box.centerX() - sticker.width / 2).toInt()
                topMargin = (box.centerY() - sticker.height / 2).toInt()
            }
            isClickable = true
            contentDescription = label

            setOnClickListener {
                Toast.makeText(context, "+10 points for $label!", Toast.LENGTH_SHORT).show()

                konfettiView.start(
                    Party(
                        speed = 30f,
                        maxSpeed = 50f,
                        damping = 0.9f,
                        spread = 360,
                        size = listOf(Size(12), Size(24)),
                        emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).perSecond(200),
                        colors = listOf(0xFFE91E63.toInt(), 0xFFFFC107.toInt(), 0xFF00BCD4.toInt()),
                        position = Position.Relative(0.5, 0.5)
                    )
                )

                container.removeView(this)
            }
        }
        container.addView(imageView)
    }

    private fun addInteractiveSticker(label: String, box: RectF, sticker: Bitmap) {
        val imageView = ImageView(context).apply {
            setImageBitmap(sticker)
            layoutParams = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                leftMargin = (box.centerX() - sticker.width / 2).toInt()
                topMargin = (box.centerY() - sticker.height / 2).toInt()
            }
            contentDescription = label

            setOnClickListener {
                Toast.makeText(context, "Launch photo capture for $label", Toast.LENGTH_SHORT).show()
                // TODO: Trigger camera intent or internal capture logic
            }
        }
        container.addView(imageView)
    }
}
