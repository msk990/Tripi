package com.example.tripi.utils

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27],  manifest = Config.NONE) // or your target SDK version
class BitmapUtilsTest {

    @Test
    fun rotateBitmap_by90Degrees_rotatesCorrectly() {
        val original = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val rotated = BitmapUtils.rotateBitmap(original, 90)

        assertEquals(50, rotated.width)
        assertEquals(100, rotated.height)
    }

    @Test
    fun rotateBitmap_by0Degrees_returnsSameDimensions() {
        val original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val rotated = BitmapUtils.rotateBitmap(original, 0)

        assertEquals(original.width, rotated.width)
        assertEquals(original.height, rotated.height)
    }

    @Test
    fun resizeWithAspectRatioAndPadding_squareBitmap_returnsCorrectSize() {
        val original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val resized = BitmapUtils.resizeWithAspectRatioAndPadding(original, 320)

        assertEquals(320, resized.width)
        assertEquals(320, resized.height)
    }

    @Test
    fun resizeWithAspectRatioAndPadding_landscapeBitmap_scalesAndPadsCorrectly() {
        val original = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val resized = BitmapUtils.resizeWithAspectRatioAndPadding(original, 320)

        assertEquals(320, resized.width)
        assertEquals(320, resized.height)
    }

    @Test
    fun resizeWithAspectRatioAndPadding_portraitBitmap_scalesAndPadsCorrectly() {
        val original = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888)
        val resized = BitmapUtils.resizeWithAspectRatioAndPadding(original, 320)

        assertEquals(320, resized.width)
        assertEquals(320, resized.height)
    }



}
