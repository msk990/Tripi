package com.example.tripi.ui.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 48f
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
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
            val label = obj.labels.firstOrNull()?.text
            if (label != null) {
                val centerX = (left + right) / 2f
                val centerY = (top + bottom) / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
                canvas.drawText(label, centerX, centerY, textPaint)
            }
        }
    }
}
