package com.example.tripi.stickers.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.tripi.R
import com.example.tripi.ml.DetectionResult
import com.example.tripi.stickers.model.StickerAssetMap
import com.example.tripi.stickers.model.StickerType
import com.example.tripi.storage.StickerRepository
import com.example.tripi.ui.camera.utils.scaleBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class StickerOverlayManager(
    private val container: FrameLayout,
    private val appContext: Context,
    private val konfettiView: KonfettiView,
    private val takePhotoButton: Button,
    private val coroutineScope: CoroutineScope
) {

    fun clear() {
        container.removeAllViews()
        takePhotoButton.visibility = View.GONE
        takePhotoButton.isEnabled = true
    }

    fun showSticker(label: String, box: RectF, sticker: Bitmap) {
        when (StickerAssetMap.getStickerInfo(label)?.type) {
            StickerType.COLLECTIBLE -> addCollectibleSticker(label, box, sticker)
            StickerType.INTERACTIVE -> addInteractiveSticker(label, box, sticker)
            else -> addInteractiveSticker(label, box, sticker)
        }
    }

    private fun addCollectibleSticker(label: String, box: RectF, sticker: Bitmap) {
        val imageView = ImageView(appContext).apply {
            isClickable = true
            isFocusable = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0x00000000)
            setImageBitmap(sticker)
            layoutParams = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                leftMargin = (box.centerX() - sticker.width / 2).toInt()
                topMargin = (box.centerY() - sticker.height / 2).toInt()
            }
            contentDescription = label

            setOnClickListener {
                val description = StickerAssetMap.getStickerInfo(label)?.description
                    ?: "You found a $label sticker!"

                showCollectibleDialog(label, description, sticker) {
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
        }
        container.addView(imageView)
    }

    private fun addInteractiveSticker(label: String, box: RectF, sticker: Bitmap) {
        val imageView = ImageView(appContext).apply {
            setImageBitmap(sticker)
            layoutParams = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                leftMargin = (box.centerX() - sticker.width / 2).toInt()
                topMargin = (box.centerY() - sticker.height / 2).toInt()
            }
            contentDescription = label
        }

        takePhotoButton.setOnClickListener {
            takePhotoButton.isEnabled = false
            val loader = ProgressBar(appContext).apply {
                layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                    gravity = Gravity.CENTER
                }
            }
            container.addView(loader)

            Handler(Looper.getMainLooper()).postDelayed({
                container.removeView(loader)
                showPhotoRewardDialog(label, sticker) {
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
                    container.removeView(imageView)
                    takePhotoButton.visibility = View.GONE
                    takePhotoButton.isEnabled = true
                }
            }, 1000)
        }

        takePhotoButton.visibility = View.VISIBLE
        takePhotoButton.bringToFront()
        container.addView(imageView)
    }

    private fun showCollectibleDialog(label: String, description: String, sticker: Bitmap, onCollect: () -> Unit) {
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_collectible_sticker, null)

        val dialog = AlertDialog.Builder(appContext)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<ImageView>(R.id.stickerImage).setImageBitmap(sticker)
        dialogView.findViewById<TextView>(R.id.stickerTitle).text = label
        dialogView.findViewById<TextView>(R.id.stickerDescription).text = description

        dialogView.findViewById<Button>(R.id.collectButton).setOnClickListener {
            coroutineScope.launch {
                StickerRepository.collect(label)
            }

            onCollect()
            dialog.dismiss()
        }


        dialog.show()
    }

    private fun showPhotoRewardDialog(label: String, sticker: Bitmap, onClose: () -> Unit) {
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_photo_reward, null)

        val dialog = AlertDialog.Builder(appContext)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<ImageView>(R.id.rewardStickerImage).setImageBitmap(sticker)
        dialogView.findViewById<Button>(R.id.rewardCloseButton).setOnClickListener {
            coroutineScope.launch {
                Log.d("Overlay", "Adding 10 points...")
                StickerRepository.addPoints(10)

//                walletViewModel.incrementPoints(10)

            }
            onClose()
            dialog.dismiss()

        }

        dialog.show()
    }
}
