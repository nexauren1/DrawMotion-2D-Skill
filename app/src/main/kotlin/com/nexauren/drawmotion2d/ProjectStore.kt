package com.nexauren.drawmotion2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ProjectStore {
    private const val FORMAT = "drawmotion-kotlin"
    private const val VERSION = 1

    fun save(context: Context, uri: Uri, project: ProjectState): Boolean {
        return try {
            val output = context.contentResolver.openOutputStream(uri) ?: return false
            ZipOutputStream(output).use { zip ->
                val meta = JSONObject().apply {
                    put("format", FORMAT)
                    put("version", VERSION)
                    put("name", project.name)
                    put("width", project.width)
                    put("height", project.height)
                    put("fps", project.fps)
                    put("frames", project.frames.size)
                    val frameArray = JSONArray()
                    project.frames.forEach { frame ->
                        val layerArray = JSONArray()
                        frame.layers.forEach { layer ->
                            layerArray.put(JSONObject().apply {
                                put("name", layer.name)
                                put("opacity", layer.opacity)
                                put("visible", layer.visible)
                                put("locked", layer.locked)
                                put("blend", layer.blendMode.name)
                            })
                        }
                        frameArray.put(JSONObject().apply { put("layers", layerArray) })
                    }
                    put("frameData", frameArray)
                }
                zip.putNextEntry(ZipEntry("project.json"))
                zip.write(meta.toString().toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
                project.frames.forEachIndexed { frameIndex, frame ->
                    frame.layers.forEachIndexed { layerIndex, layer ->
                        zip.putNextEntry(ZipEntry(String.format(Locale.US, "frames/f%03d/l%03d.png", frameIndex, layerIndex)))
                        layer.bitmap.compress(Bitmap.CompressFormat.PNG, 100, zip)
                        zip.closeEntry()
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun load(context: Context, uri: Uri): ProjectState? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            var meta: JSONObject? = null
            val images = mutableMapOf<Pair<Int, Int>, ByteArray>()
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val data = ByteArrayOutputStream()
                    zip.copyTo(data)
                    when {
                        entry.name == "project.json" -> meta = JSONObject(data.toByteArray().toString(StandardCharsets.UTF_8))
                        entry.name.startsWith("frames/") -> {
                            val parts = entry.name.split("/")
                            if (parts.size == 3) {
                                val fi = parts[1].removePrefix("f").toIntOrNull() ?: 0
                                val li = parts[2].removePrefix("l").removeSuffix(".png").toIntOrNull() ?: 0
                                images[fi to li] = data.toByteArray()
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            val m = meta ?: return null
            if (m.optString("format") != FORMAT) return null
            val project = ProjectState(m.optInt("width", 720), m.optInt("height", 720), m.optString("name", "Imported Project"))
            project.fps = m.optInt("fps", 12)
            repeat(m.optInt("frames", 1).coerceAtLeast(1)) { project.frames += FrameState() }
            val frameData = m.optJSONArray("frameData")
            if (frameData != null) {
                for (fi in 0 until minOf(frameData.length(), project.frames.size)) {
                    val layers = frameData.getJSONObject(fi).optJSONArray("layers") ?: continue
                    for (li in 0 until layers.length()) {
                        val item = layers.getJSONObject(li)
                        project.frames[fi].layers += LayerState(
                            item.optString("name", "Layer " + (li + 1)),
                            Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888),
                            item.optInt("opacity", 255),
                            item.optBoolean("visible", true),
                            item.optBoolean("locked", false),
                            runCatching { BlendMode.valueOf(item.optString("blend", "NORMAL")) }.getOrDefault(BlendMode.NORMAL)
                        )
                    }
                }
            }
            images.forEach { (key, bytes) ->
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@forEach
                val bitmap = if (decoded.width == project.width && decoded.height == project.height) decoded else Bitmap.createScaledBitmap(decoded, project.width, project.height, true).also { decoded.recycle() }
                val frame = project.frames[key.first]
                while (frame.layers.size <= key.second) {
                    frame.layers += LayerState("Layer " + (frame.layers.size + 1), Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888))
                }
                frame.layers[key.second].bitmap.recycle()
                frame.layers[key.second].bitmap = bitmap
            }
            project.frames.forEach { frame ->
                if (frame.layers.isEmpty()) {
                    frame.layers += LayerState("Background", Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888), locked = true)
                    frame.layers += LayerState("Artwork", Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888))
                }
            }
            project.animationMode = project.frames.size > 1
            project.ensure()
            project
        } catch (_: Exception) {
            null
        }
    }
}
