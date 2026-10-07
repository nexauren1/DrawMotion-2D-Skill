package com.nexauren.drawmotion2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class DrawCanvasView(context: Context) : View(context) {
    val project = ProjectState()

    var tool: EditorTool = EditorTool.BRUSH
        private set
    var brush: BrushPreset = BrushPreset.INK
        private set
    var ruler: RulerMode = RulerMode.OFF
        private set
    var symmetry: SymmetryMode = SymmetryMode.OFF
        private set

    var brushColor: Int = Color.rgb(31, 41, 55)
        private set
    var brushSize: Float = 18f
        private set

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val undo = ArrayDeque<Bitmap>()
    private val redo = ArrayDeque<Bitmap>()

    private var canvasRect = RectF()
    private var scale = 1f
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var playing = false
    private var playIndex = 0
    private var playback: Runnable? = null

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        project.ensure()
        isFocusable = true
    }

    fun setAnimation(enabled: Boolean) {
        project.animationMode = enabled
        invalidate()
    }

    fun setTool(value: EditorTool) {
        tool = value
        invalidate()
    }

    fun setBrushPreset(value: BrushPreset) {
        brush = value
        if (tool == EditorTool.ERASER) tool = EditorTool.BRUSH
        invalidate()
    }

    fun setBrushColor(value: Int) {
        brushColor = value
        if (tool == EditorTool.ERASER) tool = EditorTool.BRUSH
        invalidate()
    }

    fun setBrushSize(value: Float) {
        brushSize = value.coerceIn(1f, 120f)
    }

    fun setRuler(value: RulerMode) {
        ruler = value
        invalidate()
    }

    fun setSymmetry(value: SymmetryMode) {
        symmetry = value
        invalidate()
    }

    fun setGrid(enabled: Boolean) {
        ruler = if (enabled) RulerMode.GRID else RulerMode.OFF
        invalidate()
    }

    fun frameCount(): Int = project.frames.size
    fun currentFrame(): Int = project.currentFrame
    fun playing(): Boolean = playing

    fun setFps(value: Int) {
        project.fps = value.coerceIn(1, 30)
    }

    fun undo() {
        val layer = project.layer()
        if (undo.isEmpty() || layer.locked) return
        redo.addLast(layer.bitmap.copy(Bitmap.Config.ARGB_8888, true))
        replaceLayerBitmap(layer, undo.removeLast())
        invalidate()
    }

    fun redo() {
        val layer = project.layer()
        if (redo.isEmpty() || layer.locked) return
        undo.addLast(layer.bitmap.copy(Bitmap.Config.ARGB_8888, true))
        replaceLayerBitmap(layer, redo.removeLast())
        invalidate()
    }

    fun addFrame() {
        project.ensure()
        val source = project.current()
        val next = FrameState()
        source.layers.forEach { sourceLayer ->
            next.layers += LayerState(
                sourceLayer.name,
                Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888),
                sourceLayer.opacity,
                sourceLayer.visible,
                sourceLayer.locked,
                sourceLayer.blendMode
            )
        }
        project.frames.add(project.currentFrame + 1, next)
        project.currentFrame++
        project.currentLayer = min(project.currentLayer, next.layers.lastIndex)
        clearHistory()
        invalidate()
    }

    fun duplicateFrame() {
        project.frames.add(project.currentFrame + 1, project.current().copyDeep())
        project.currentFrame++
        clearHistory()
        invalidate()
    }

    fun deleteFrame() {
        if (!project.animationMode || project.frames.size <= 1) return
        project.frames.removeAt(project.currentFrame).dispose()
        project.currentFrame = project.currentFrame.coerceAtMost(project.frames.lastIndex)
        project.currentLayer = project.currentLayer.coerceAtMost(project.current().layers.lastIndex)
        clearHistory()
        invalidate()
    }

    fun selectFrame(index: Int) {
        if (index !in project.frames.indices) return
        project.currentFrame = index
        project.currentLayer = project.currentLayer.coerceAtMost(project.current().layers.lastIndex)
        clearHistory()
        invalidate()
    }

    fun layerNames(): List<String> {
        return project.current().layers.mapIndexed { index, layer ->
            (if (index == project.currentLayer) "● " else "") +
                layer.name +
                if (layer.locked) "  Lock" else ""
        }
    }

    fun selectLayer(index: Int) {
        if (index !in project.current().layers.indices) return
        project.currentLayer = index
        clearHistory()
        invalidate()
    }

    fun addLayer() {
        val frame = project.current()
        frame.layers.add(
            project.currentLayer + 1,
            LayerState(
                "Layer " + (frame.layers.size + 1),
                Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888)
            )
        )
        project.currentLayer++
        invalidate()
    }

    fun duplicateLayer() {
        val layer = project.layer()
        if (layer.locked) return
        frameSafeAddLayer(layer.copyDeep().apply { name += " Copy" })
        invalidate()
    }

    private fun frameSafeAddLayer(layer: LayerState) {
        project.current().layers.add(project.currentLayer + 1, layer)
        project.currentLayer++
    }

    fun deleteLayer() {
        val frame = project.current()
        if (project.currentLayer <= 0 || frame.layers.size <= 2) return
        frame.layers.removeAt(project.currentLayer).dispose()
        project.currentLayer--
        invalidate()
    }

    fun moveLayerUp() {
        val frame = project.current()
        if (project.currentLayer < frame.layers.lastIndex) {
            val item = frame.layers.removeAt(project.currentLayer)
            project.currentLayer++
            frame.layers.add(project.currentLayer, item)
            invalidate()
        }
    }

    fun moveLayerDown() {
        val frame = project.current()
        if (project.currentLayer > 1) {
            val item = frame.layers.removeAt(project.currentLayer)
            project.currentLayer--
            frame.layers.add(project.currentLayer, item)
            invalidate()
        }
    }

    fun toggleLayerVisibility() {
        project.layer().visible = !project.layer().visible
        invalidate()
    }

    fun setLayerOpacity(value: Int) {
        project.layer().opacity = value.coerceIn(0, 255)
        invalidate()
    }

    fun cycleBlendMode() {
        val values = BlendMode.values()
        val layer = project.layer()
        layer.blendMode = values[(layer.blendMode.ordinal + 1) % values.size]
        invalidate()
    }

    fun currentLayerTitle(): String = project.layer().name

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateCanvasRect()
    }

    private fun updateCanvasRect() {
        val pad = 18f * resources.displayMetrics.density
        val availableW = max(1f, width - pad * 2f)
        val availableH = max(1f, height - pad * 2f)
        scale = min(availableW / project.width, availableH / project.height)
        val drawW = project.width * scale
        val drawH = project.height * scale
        canvasRect = RectF(
            (width - drawW) / 2f,
            (height - drawH) / 2f,
            (width + drawW) / 2f,
            (height + drawH) / 2f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateCanvasRect()
        canvas.drawColor(Color.rgb(241, 245, 249))
        canvas.save()
        canvas.translate(canvasRect.left, canvasRect.top)
        canvas.scale(scale, scale)

        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, project.width.toFloat(), project.height.toFloat(), paint)

        if (project.animationMode && project.currentFrame > 0) {
            bitmapPaint.alpha = 55
            project.frames[project.currentFrame - 1].layers.lastOrNull()?.bitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
            }
        }

        if (project.animationMode && project.currentFrame + 1 < project.frames.size) {
            bitmapPaint.alpha = 40
            project.frames[project.currentFrame + 1].layers.lastOrNull()?.bitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
            }
        }

        bitmapPaint.alpha = 255
        project.current().layers.forEach { it.draw(canvas, bitmapPaint) }
        drawGuides(canvas)
        canvas.restore()
    }

    private fun drawGuides(canvas: Canvas) {
        if (ruler == RulerMode.GRID) {
            paint.style = Paint.Style.STROKE
            paint.color = 0x22334155
            paint.strokeWidth = 1f
            for (x in 0 until project.width step 60)
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), project.height.toFloat(), paint)
            for (y in 0 until project.height step 60)
                canvas.drawLine(0f, y.toFloat(), project.width.toFloat(), y.toFloat(), paint)
        }

        if (ruler == RulerMode.STRAIGHT) {
            paint.color = 0x5594A3B8
            canvas.drawLine(0f, project.height/2f, project.width.toFloat(), project.height/2f, paint)
        }

        if (ruler == RulerMode.PERSPECTIVE) {
            paint.color = 0x5594A3B8
            val vx = project.width/2f
            val vy = project.height*.38f
            for (y in 80 until project.height step 80) {
                canvas.drawLine(vx, vy, 0f, y.toFloat(), paint)
                canvas.drawLine(vx, vy, project.width.toFloat(), y.toFloat(), paint)
            }
        }

        if (ruler == RulerMode.RADIAL) {
            paint.color = 0x5594A3B8
            val cx = project.width/2f
            val cy = project.height/2f
            repeat(12) { i ->
                val a = i*Math.PI/6.0
                canvas.drawLine(
                    cx, cy,
                    cx+cos(a).toFloat()*project.width,
                    cy+sin(a).toFloat()*project.height,
                    paint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (playing || !canvasRect.contains(event.x, event.y)) return true

        val x = ((event.x-canvasRect.left)/scale).coerceIn(0f, project.width-1f)
        val y = ((event.y-canvasRect.top)/scale).coerceIn(0f, project.height-1f)
        val layer = project.layer()
        if (layer.locked) return true

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX=x; downY=y; lastX=x; lastY=y; moveX=x; moveY=y
                if (tool==EditorTool.PICKER) {
                    pickColor(x.toInt(),y.toInt())
                } else {
                    snapshot()
                    when(tool) {
                        EditorTool.FILL -> floodFill(x.toInt(),y.toInt())
                        EditorTool.BRUSH, EditorTool.ERASER -> drawPoint(Canvas(layer.bitmap),x,y)
                        else -> Unit
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                when(tool) {
                    EditorTool.BRUSH, EditorTool.ERASER -> {
                        drawLine(Canvas(layer.bitmap),lastX,lastY,x,y)
                        lastX=x; lastY=y
                        invalidate()
                    }
                    EditorTool.MOVE -> {
                        moveBitmap(layer.bitmap,x-moveX,y-moveY)
                        moveX=x; moveY=y
                        invalidate()
                    }
                    else -> Unit
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                when(tool) {
                    EditorTool.LINE, EditorTool.RECTANGLE, EditorTool.ELLIPSE ->
                        drawShape(Canvas(layer.bitmap),downX,downY,x,y)
                    else -> Unit
                }
                invalidate()
                return true
            }
        }
        return true
    }

    private fun configureStroke() {
        paint.reset()
        paint.isAntiAlias = brush != BrushPreset.PIXEL
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = if (brush==BrushPreset.PIXEL) Paint.Cap.SQUARE else Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = brushSize
        paint.maskFilter = null
        paint.xfermode = null

        when(brush) {
            BrushPreset.PENCIL -> paint.strokeWidth=brushSize*.8f
            BrushPreset.MARKER -> paint.strokeWidth=brushSize*1.35f
            BrushPreset.AIRBRUSH -> {
                paint.strokeWidth=brushSize*2f
                paint.maskFilter=android.graphics.BlurMaskFilter(
                    max(1f,brushSize*.7f),android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            BrushPreset.NEON, BrushPreset.GLOW -> paint.setShadowLayer(brushSize,0f,0f,brushColor)
            BrushPreset.CHALK -> paint.alpha=180
            BrushPreset.OIL -> paint.strokeWidth=brushSize*1.7f
            BrushPreset.WATERCOLOR -> {
                paint.alpha=90
                paint.strokeWidth=brushSize*1.3f
            }
            BrushPreset.CALLIGRAPHY -> {
                paint.strokeWidth=brushSize*.55f
                paint.strokeCap=Paint.Cap.SQUARE
            }
            BrushPreset.G_PEN -> paint.strokeWidth=brushSize*.65f
            else -> Unit
        }

        paint.color=(brushColor and 0x00FFFFFF) or (paint.alpha shl 24)

        if(tool==EditorTool.ERASER) {
            paint.color=Color.TRANSPARENT
            paint.xfermode=PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private fun drawPoint(canvas: Canvas,x:Float,y:Float) {
        configureStroke()
        val r=max(.5f,brushSize/2f)
        if(brush==BrushPreset.PIXEL) canvas.drawRect(x-r,y-r,x+r,y+r,paint)
        else canvas.drawCircle(x,y,r,paint)
        symmetryPoint(canvas,x,y)
    }

    private fun drawLine(canvas: Canvas,x1:Float,y1:Float,x2:Float,y2:Float) {
        var tx=x2
        var ty=y2
        if(ruler==RulerMode.STRAIGHT) {
            if(abs(x2-x1)>abs(y2-y1)) ty=y1 else tx=x1
        }
        configureStroke()
        canvas.drawLine(x1,y1,tx,ty,paint)
        symmetryLine(canvas,x1,y1,tx,ty)
    }

    private fun drawShape(canvas: Canvas,x1:Float,y1:Float,x2:Float,y2:Float) {
        configureStroke()
        val rect=RectF(min(x1,x2),min(y1,y2),max(x1,x2),max(y1,y2))
        when(tool) {
            EditorTool.LINE -> canvas.drawLine(x1,y1,x2,y2,paint)
            EditorTool.RECTANGLE -> canvas.drawRect(rect,paint)
            EditorTool.ELLIPSE -> canvas.drawOval(rect,paint)
            else -> Unit
        }
        symmetryLine(canvas,x1,y1,x2,y2)
    }

    private fun symmetryPoint(canvas: Canvas,x:Float,y:Float) {
        configureStroke()
        val r=max(.5f,brushSize/2f)
        when(symmetry) {
            SymmetryMode.VERTICAL, SymmetryMode.MIRROR ->
                canvas.drawCircle(project.width-x,y,r,paint)
            SymmetryMode.HORIZONTAL, SymmetryMode.MIRROR ->
                canvas.drawCircle(x,project.height-y,r,paint)
            SymmetryMode.RADIAL_4 -> repeat(3) { i ->
                canvas.save()
                canvas.rotate((i+1)*90f,project.width/2f,project.height/2f)
                canvas.drawCircle(x,y,r,paint)
                canvas.restore()
            }
            else -> Unit
        }
    }

    private fun symmetryLine(canvas: Canvas,x1:Float,y1:Float,x2:Float,y2:Float) {
        configureStroke()
        when(symmetry) {
            SymmetryMode.VERTICAL, SymmetryMode.MIRROR ->
                canvas.drawLine(project.width-x1,y1,project.width-x2,y2,paint)
            SymmetryMode.HORIZONTAL, SymmetryMode.MIRROR ->
                canvas.drawLine(x1,project.height-y1,x2,project.height-y2,paint)
            SymmetryMode.RADIAL_4 -> repeat(3) { i ->
                canvas.save()
                canvas.rotate((i+1)*90f,project.width/2f,project.height/2f)
                canvas.drawLine(x1,y1,x2,y2,paint)
                canvas.restore()
            }
            else -> Unit
        }
    }

    private fun pickColor(x:Int,y:Int) {
        project.current().layers.asReversed().forEach { layer ->
            if(layer.visible) {
                val pixel=layer.bitmap.getPixel(x,y)
                if(Color.alpha(pixel)>0) {
                    setBrushColor(pixel)
                    return
                }
            }
        }
    }

    private fun floodFill(x:Int,y:Int) {
        val bitmap=project.layer().bitmap
        val pixels=IntArray(project.width*project.height)
        bitmap.getPixels(pixels,0,project.width,0,0,project.width,project.height)
        val old=pixels[y*project.width+x]
        if(old==brushColor) return

        val stack=ArrayDeque<Int>()
        stack.addLast(y*project.width+x)
        while(stack.isNotEmpty()) {
            val index=stack.removeLast()
            if(pixels[index]!=old) continue
            pixels[index]=brushColor
            val px=index%project.width
            val py=index/project.width
            if(px>0 && pixels[index-1]==old) stack.addLast(index-1)
            if(px<project.width-1 && pixels[index+1]==old) stack.addLast(index+1)
            if(py>0 && pixels[index-project.width]==old) stack.addLast(index-project.width)
            if(py<project.height-1 && pixels[index+project.width]==old) stack.addLast(index+project.width)
        }
        bitmap.setPixels(pixels,0,project.width,0,0,project.width,project.height)
    }

    private fun moveBitmap(bitmap:Bitmap,dx:Float,dy:Float) {
        val source=bitmap.copy(Bitmap.Config.ARGB_8888,true)
        Canvas(bitmap).drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR)
        Canvas(bitmap).drawBitmap(source,dx,dy,bitmapPaint)
        source.recycle()
    }

    private fun snapshot() {
        val layer=project.layer()
        if(layer.locked) return
        undo.addLast(layer.bitmap.copy(Bitmap.Config.ARGB_8888,true))
        while(undo.size>20) undo.removeFirst().recycle()
        while(redo.isNotEmpty()) redo.removeLast().recycle()
    }

    private fun clearHistory() {
        while(undo.isNotEmpty()) undo.removeLast().recycle()
        while(redo.isNotEmpty()) redo.removeLast().recycle()
    }

    private fun replaceLayerBitmap(layer:LayerState,bitmap:Bitmap) {
        val old=layer.bitmap
        layer.bitmap=bitmap
        if(!old.isRecycled) old.recycle()
    }

    fun addText(value:String) {
        val layer=project.layer()
        snapshot()
        val textPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color=brushColor
            textSize=56f
            typeface=android.graphics.Typeface.DEFAULT_BOLD
        }
        Canvas(layer.bitmap).drawText(value,(project.width-textPaint.measureText(value))/2f,project.height*.5f,textPaint)
        invalidate()
    }

    fun addAsset(name:String) {
        val asset=AssetCatalog.create(name,420,420)
        snapshot()
        Canvas(project.layer().bitmap).drawBitmap(asset,(project.width-asset.width)/2f,(project.height-asset.height)/2f,bitmapPaint)
        asset.recycle()
        invalidate()
    }

    fun addTexture(name:String) {
        val texture=TextureCatalog.create(name,project.width,project.height)
        snapshot()
        Canvas(project.layer().bitmap).drawBitmap(texture,0f,0f,bitmapPaint)
        texture.recycle()
        invalidate()
    }

    fun addImportedBitmap(bitmap:Bitmap) {
        val source=Bitmap.createScaledBitmap(bitmap,min(project.width,bitmap.width),min(project.height,bitmap.height),true)
        snapshot()
        Canvas(project.layer().bitmap).drawBitmap(source,(project.width-source.width)/2f,(project.height-source.height)/2f,bitmapPaint)
        source.recycle()
        invalidate()
    }

    fun applyFilter(type:Int,amount:Float) {
        val layer=project.layer()
        snapshot()
        replaceLayerBitmap(layer,ImageFilters.apply(layer.bitmap,type,amount))
        invalidate()
    }

    fun rotate90() {
        val layer=project.layer()
        snapshot()
        val matrix=Matrix().apply { postRotate(90f,project.width/2f,project.height/2f) }
        replaceLayerBitmap(layer,Bitmap.createBitmap(layer.bitmap,0,0,project.width,project.height,matrix,true))
        invalidate()
    }

    fun flipHorizontal()=transform(-1f,1f)
    fun flipVertical()=transform(1f,-1f)

    private fun transform(scaleX:Float,scaleY:Float) {
        val layer=project.layer()
        snapshot()
        val matrix=Matrix().apply { setScale(scaleX,scaleY,project.width/2f,project.height/2f) }
        replaceLayerBitmap(layer,Bitmap.createBitmap(layer.bitmap,0,0,project.width,project.height,matrix,true))
        invalidate()
    }

    fun applyTemplate(id:String) {
        stopPlayback()
        project.frames.forEach { it.dispose() }
        project.frames.clear()

        TemplateCatalog.create(id,project.width,project.height).forEach { bitmap ->
            val background=Bitmap.createBitmap(project.width,project.height,Bitmap.Config.ARGB_8888)
            background.eraseColor(Color.WHITE)
            project.frames += FrameState(
                mutableListOf(
                    LayerState("Background",background,locked=true),
                    LayerState("Artwork",bitmap)
                )
            )
        }

        project.animationMode=project.frames.size>1
        project.currentFrame=0
        project.currentLayer=1
        clearHistory()
        invalidate()
    }

    fun renderFrame(index:Int,whiteBackground:Boolean):Bitmap {
        val output=Bitmap.createBitmap(project.width,project.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(output)
        if(whiteBackground) canvas.drawColor(Color.WHITE)
        project.frames[index].layers.forEach { it.draw(canvas,bitmapPaint) }
        return output
    }

    fun renderAllFrames():List<Bitmap> = project.frames.indices.map { renderFrame(it,true) }

    fun startPlayback() {
        if(!project.animationMode || project.frames.isEmpty()) return
        stopPlayback()
        playing=true
        playIndex=0
        val delay=1000L/project.fps.coerceIn(1,30)
        playback=object:Runnable {
            override fun run() {
                if(!playing) return
                project.currentFrame=playIndex%project.frames.size
                playIndex++
                invalidate()
                postDelayed(this,delay)
            }
        }
        post(playback!!)
    }

    fun stopPlayback() {
        playing=false
        playback?.let { removeCallbacks(it) }
        playback=null
    }

    override fun onDetachedFromWindow() {
        stopPlayback()
        clearHistory()
        project.dispose()
        super.onDetachedFromWindow()
    }
}
