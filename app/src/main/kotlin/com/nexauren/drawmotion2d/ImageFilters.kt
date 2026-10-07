package com.nexauren.drawmotion2d

import android.graphics.Bitmap
import android.graphics.Color

object ImageFilters {
    const val BRIGHTNESS = 0
    const val CONTRAST = 1
    const val GRAYSCALE = 2
    const val INVERT = 3
    const val SEPIA = 4

    fun apply(source: Bitmap, type: Int, amount: Float): Bitmap {
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val width = output.width
        val height = output.height
        val pixels = IntArray(width * height)
        output.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            var r = Color.red(color)
            var g = Color.green(color)
            var b = Color.blue(color)
            val a = Color.alpha(color)

            when (type) {
                BRIGHTNESS -> {
                    r = clamp((r + amount).toInt())
                    g = clamp((g + amount).toInt())
                    b = clamp((b + amount).toInt())
                }
                CONTRAST -> {
                    val factor = (259f * (amount + 255f)) / (255f * (259f - amount))
                    r = clamp((factor * (r - 128) + 128).toInt())
                    g = clamp((factor * (g - 128) + 128).toInt())
                    b = clamp((factor * (b - 128) + 128).toInt())
                }
                GRAYSCALE -> {
                    val y = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                    r = y
                    g = y
                    b = y
                }
                INVERT -> {
                    r = 255 - r
                    g = 255 - g
                    b = 255 - b
                }
                SEPIA -> {
                    val nr = (0.393f * r + 0.769f * g + 0.189f * b).toInt()
                    val ng = (0.349f * r + 0.686f * g + 0.168f * b).toInt()
                    val nb = (0.272f * r + 0.534f * g + 0.131f * b).toInt()
                    r = clamp(nr)
                    g = clamp(ng)
                    b = clamp(nb)
                }
            }

            pixels[i] = Color.argb(a, clamp(r), clamp(g), clamp(b))
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun clamp(value: Int): Int = value.coerceIn(0, 255)
}
