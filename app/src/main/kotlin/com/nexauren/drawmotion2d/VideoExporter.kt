package com.nexauren.drawmotion2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.media.MediaRecorder
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.Surface

object VideoExporter {
    fun export(context: Context, uri: Uri, frames: List<Bitmap>, fps: Int): Boolean {
        if (frames.isEmpty()) return false
        var recorder: MediaRecorder? = null
        var descriptor: ParcelFileDescriptor? = null
        var surface: Surface? = null
        return try {
            val first = frames.first()
            descriptor = context.contentResolver.openFileDescriptor(uri, "w") ?: return false
            recorder = MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize((first.width and -2).coerceAtLeast(2), (first.height and -2).coerceAtLeast(2))
                setVideoFrameRate(fps.coerceIn(1, 30))
                setVideoEncodingBitRate((first.width * first.height * 5).coerceAtLeast(1_000_000))
                setOutputFile(descriptor!!.fileDescriptor)
                prepare()
                start()
            }
            surface = recorder.surface
            val frameDelay = 1000L / fps.coerceIn(1, 30)
            frames.forEach { frame ->
                val canvas = surface!!.lockCanvas(null)
                try {
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(frame, null, RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()), null)
                } finally {
                    surface!!.unlockCanvasAndPost(canvas)
                }
                Thread.sleep(frameDelay)
            }
            recorder.stop()
            true
        } catch (_: Exception) {
            false
        } finally {
            runCatching { surface?.release() }
            runCatching { recorder?.release() }
            runCatching { descriptor?.close() }
        }
    }
}
