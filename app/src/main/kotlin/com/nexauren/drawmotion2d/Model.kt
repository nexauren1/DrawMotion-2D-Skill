package com.nexauren.drawmotion2d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

enum class EditorTool { BRUSH, LINE, RECTANGLE, ELLIPSE, FILL, PICKER, ERASER, MOVE }

enum class BrushPreset {
    INK, PENCIL, MARKER, AIRBRUSH, NEON, PIXEL, CHALK, OIL, WATERCOLOR, CALLIGRAPHY, G_PEN, GLOW
}

enum class RulerMode { OFF, STRAIGHT, GRID, PERSPECTIVE, RADIAL }

enum class SymmetryMode { OFF, VERTICAL, HORIZONTAL, MIRROR, RADIAL_4 }

enum class BlendMode { NORMAL, MULTIPLY, SCREEN, ADD, OVERLAY }

data class LayerState(
    var name: String,
    var bitmap: Bitmap,
    var opacity: Int = 255,
    var visible: Boolean = true,
    var locked: Boolean = false,
    var blendMode: BlendMode = BlendMode.NORMAL
) {
    fun copyDeep(): LayerState = LayerState(
        name,
        bitmap.copy(Bitmap.Config.ARGB_8888, true),
        opacity,
        visible,
        locked,
        blendMode
    )

    fun draw(canvas: Canvas, paint: Paint) {
        if (!visible || bitmap.isRecycled) return
        val oldAlpha = paint.alpha
        val oldXfer = paint.xfermode
        paint.alpha = opacity
        paint.xfermode = when (blendMode) {
            BlendMode.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            BlendMode.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            BlendMode.ADD -> PorterDuffXfermode(PorterDuff.Mode.ADD)
            BlendMode.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
            BlendMode.NORMAL -> null
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        paint.alpha = oldAlpha
        paint.xfermode = oldXfer
    }

    fun dispose() {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}

data class FrameState(
    val layers: MutableList<LayerState> = mutableListOf()
) {
    fun copyDeep(): FrameState = FrameState(layers.map { it.copyDeep() }.toMutableList())

    fun dispose() {
        layers.forEach { it.dispose() }
        layers.clear()
    }
}

class ProjectState(
    var width: Int = 720,
    var height: Int = 720,
    var name: String = "Untitled"
) {
    var fps: Int = 12
    var animationMode: Boolean = false
    var currentFrame: Int = 0
    var currentLayer: Int = 1
    val frames: MutableList<FrameState> = mutableListOf()

    fun ensure() {
        if (frames.isEmpty()) {
            frames += FrameState(
                mutableListOf(
                    LayerState(
                        "Background",
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888),
                        locked = true
                    ),
                    LayerState(
                        "Artwork",
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    )
                )
            )
        }
        currentFrame = currentFrame.coerceIn(0, frames.lastIndex)
        currentLayer = currentLayer.coerceIn(0, frames[currentFrame].layers.lastIndex)
    }

    fun current(): FrameState {
        ensure()
        return frames[currentFrame]
    }

    fun layer(): LayerState {
        ensure()
        return frames[currentFrame].layers[currentLayer]
    }

    fun dispose() {
        frames.forEach { it.dispose() }
        frames.clear()
    }
}
