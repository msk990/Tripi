package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.objects.DetectedObject

class GraphicOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val stickers: List<Bitmap> = listOf("hi.png", "sing.png").map { name ->
        context.assets.open("stickers/$name").use { input ->
            BitmapFactory.decodeStream(input)
        }
    }

    private var objects: List<DetectedObject> = emptyList()
    private var scaleXFactor: Float = 1f
    private var scaleYFactor: Float = 1f

    fun update(objects: List<DetectedObject>, imageWidth: Int, imageHeight: Int) {
        this.objects = objects
        scaleXFactor = width.toFloat() / imageWidth.toFloat()
        scaleYFactor = height.toFloat() / imageHeight.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (obj in objects) {
            val box = obj.boundingBox
            val left = box.left * scaleXFactor
            val top = box.top * scaleYFactor
            val right = box.right * scaleXFactor
            val bottom = box.bottom * scaleYFactor
            canvas.drawRect(left, top, right, bottom, boxPaint)

            val sticker = stickers.random()
            val destRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            canvas.drawBitmap(sticker, null, destRect, null)
        }
    }
}
