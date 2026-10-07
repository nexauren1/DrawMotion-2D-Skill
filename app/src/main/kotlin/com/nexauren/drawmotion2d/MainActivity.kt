package com.nexauren.drawmotion2d

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    companion object {
        private const val REQUEST_SAVE = 10
        private const val REQUEST_OPEN = 11
        private const val REQUEST_IMAGE = 12
        private const val REQUEST_VIDEO = 13
    }

    private lateinit var root: LinearLayout
    private var editor: DrawCanvasView? = null
    private var timeline: LinearLayout? = null

    private val page = Color.rgb(248,250,252)
    private val text = Color.rgb(15,23,42)
    private val muted = Color.rgb(100,116,139)
    private val purple = Color.rgb(124,58,237)
    private val cyan = Color.rgb(8,145,178)
    private val border = Color.rgb(226,232,240)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=page
        window.navigationBarColor=page
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        showHome()
    }

    private fun showHome() {
        editor=null
        root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setBackgroundColor(page)
        }

        val content=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(20),dp(22),dp(20),dp(24))
        }
        val scroll=ScrollView(this)
        scroll.addView(content)
        root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))

        content.addView(label("DRAWMOTION",13f,purple,Typeface.BOLD,.18f))
        content.addView(label("Kotlin Edition",36f,text,Typeface.BOLD))
        content.addView(label("Draw • Animate • Build",18f,cyan,Typeface.BOLD))
        content.addView(space(18))

        content.addView(actionCard("New Drawing","Native canvas with layers, brushes, effects and export.") {
            openEditor(false,null)
        },margins(0,0,0,10))

        content.addView(actionCard("New Animation","Frame timeline, onion skin, rulers, symmetry and MP4.") {
            openEditor(true,null)
        },margins(0,0,0,16))

        content.addView(label("READY PROJECTS",12f,muted,Typeface.BOLD))
        content.addView(space(6))

        TemplateCatalog.templates.forEach { template ->
            content.addView(
                smallCard(template.title,template.frames.toString()+" frames") {
                    openEditor(template.frames>1,template.id)
                },
                margins(0,0,0,8)
            )
        }

        content.addView(space(16))
        content.addView(label(
            "Kotlin-first rebuild • Java application code removed • Android Canvas engine",
            12f,muted,Typeface.NORMAL
        ))

        setContentView(root)
    }

    private fun openEditor(animation:Boolean,templateId:String?) {
        val newEditor=DrawCanvasView(this).also {
            it.setAnimation(animation)
            if(templateId!=null) it.applyTemplate(templateId)
        }
        editor=newEditor

        root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(241,245,249))
        }

        buildTopBar()
        buildToolStrip()
        root.addView(newEditor,LinearLayout.LayoutParams(-1,0,1f))
        if(animation) buildTimeline()
        setContentView(root)
        refreshTimeline()
    }

    private fun buildTopBar() {
        val bar=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(5),dp(5),dp(5),dp(5))
            setBackgroundColor(Color.WHITE)
        }

        bar.addView(toolButton("‹") { showHome() },fixed(42))
        bar.addView(label("DrawMotion",15f,text,Typeface.BOLD),LinearLayout.LayoutParams(0,48.dp(),1f))
        bar.addView(toolButton("Undo") { editor?.undo() },fixed(64))
        bar.addView(toolButton("Redo") { editor?.redo() },fixed(64))
        bar.addView(toolButton("Project") { projectDialog() },fixed(76))
        bar.addView(toolButton("Export") { exportDialog() },fixed(70))

        root.addView(bar,LinearLayout.LayoutParams(-1,dp(58)))
    }

    private fun buildToolStrip() {
        val scroll=HorizontalScrollView(this).apply { setBackgroundColor(Color.WHITE) }
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            setPadding(dp(5),dp(4),dp(5),dp(4))
        }

        fun add(name:String,click:()->Unit) {
            row.addView(toolButton(name,click),fixed(76))
        }

        add("Brush"){ brushDialog() }
        add("Tools"){ toolsDialog() }
        add("Color"){ colorDialog() }
        add("Size"){ sizeDialog() }
        add("Ruler"){ rulerDialog() }
        add("Symmetry"){ symmetryDialog() }
        add("Grid"){
            val active=editor?.ruler==RulerMode.GRID
            editor?.setGrid(!active)
        }
        add("Layers"){ layersDialog() }
        add("Resources"){ resourcesDialog() }
        add("Filters"){ filtersDialog() }
        add("Import"){ importImage() }

        scroll.addView(row)
        root.addView(scroll,LinearLayout.LayoutParams(-1,dp(50)))
    }

    private fun buildTimeline() {
        val panel=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val controls=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(4),dp(3),dp(4),dp(3))
        }

        controls.addView(toolButton("+ Frame") {
            editor?.addFrame()
            refreshTimeline()
        },equal())
        controls.addView(toolButton("Duplicate") {
            editor?.duplicateFrame()
            refreshTimeline()
        },equal())
        controls.addView(toolButton("Delete") {
            editor?.deleteFrame()
            refreshTimeline()
        },equal())
        controls.addView(toolButton("Play") {
            val view=editor
            if(view!=null) {
                if(view.playing()) view.stopPlayback() else view.startPlayback()
                refreshTimeline()
            }
        },equal())
        controls.addView(toolButton("FPS " + (editor?.project?.fps ?: 12)) {
            fpsDialog()
        },equal())

        panel.addView(controls)

        val hs=HorizontalScrollView(this)
        timeline=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            setPadding(dp(6),0,dp(6),dp(6))
        }
        hs.addView(timeline)
        panel.addView(hs,LinearLayout.LayoutParams(-1,dp(52)))
        root.addView(panel,LinearLayout.LayoutParams(-1,dp(98)))
    }

    private fun refreshTimeline() {
        val strip=timeline ?: return
        val view=editor ?: return
        strip.removeAllViews()
        repeat(view.frameCount()) { index ->
            val active=index==view.currentFrame()
            val item=TextView(this).apply {
                text=String.format(Locale.US,"%02d",index+1)
                textSize=12f
                gravity=Gravity.CENTER
                setTextColor(if(active) Color.WHITE else muted)
                background=rounded(if(active) purple else Color.WHITE,12)
                setOnClickListener {
                    view.selectFrame(index)
                    refreshTimeline()
                }
            }
            strip.addView(item,marginsSize(46,40,3,0,3,0))
        }
    }

    private fun projectDialog() {
        val items=arrayOf("Rename","Save .drawmotion","Open .drawmotion","Add Layer","Layers")
        AlertDialog.Builder(this).setTitle("Project").setItems(items) { _,which ->
            when(which) {
                0 -> renameProject()
                1 -> saveProject()
                2 -> openProject()
                3 -> editor?.addLayer()
                4 -> layersDialog()
            }
        }.show()
    }

    private fun renameProject() {
        val input=EditText(this).apply {
            setText(editor?.project?.name ?: "Untitled")
            hint="Project name"
        }
        AlertDialog.Builder(this).setTitle("Project name").setView(input)
            .setPositiveButton("Save") { _,_ ->
                editor?.project?.name=input.text.toString().trim().ifEmpty { "Untitled" }
            }
            .setNegativeButton("Cancel",null).show()
    }

    private fun toolsDialog() {
        val names=arrayOf("Brush","Line","Rectangle","Ellipse","Fill","Color Picker","Eraser","Move","Text")
        AlertDialog.Builder(this).setTitle("Tools").setItems(names) { _,which ->
            when(which) {
                0 -> editor?.setTool(EditorTool.BRUSH)
                1 -> editor?.setTool(EditorTool.LINE)
                2 -> editor?.setTool(EditorTool.RECTANGLE)
                3 -> editor?.setTool(EditorTool.ELLIPSE)
                4 -> editor?.setTool(EditorTool.FILL)
                5 -> editor?.setTool(EditorTool.PICKER)
                6 -> editor?.setTool(EditorTool.ERASER)
                7 -> editor?.setTool(EditorTool.MOVE)
                8 -> textDialog()
            }
        }.show()
    }

    private fun brushDialog() {
        val names=BrushPreset.values().map { it.name.replace('_',' ') }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Brush presets").setItems(names) { _,which ->
            editor?.setBrushPreset(BrushPreset.values()[which])
        }.show()
    }

    private fun colorDialog() {
        val colors=listOf(
            Color.BLACK,Color.WHITE,Color.RED,Color.rgb(255,120,0),
            Color.YELLOW,Color.GREEN,Color.CYAN,Color.BLUE,
            Color.rgb(124,58,237),Color.MAGENTA,Color.rgb(255,105,180)
        )
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            setPadding(dp(10),dp(10),dp(10),dp(10))
        }
        colors.forEach { value ->
            row.addView(TextView(this).apply {
                background=rounded(value,100)
                setOnClickListener { editor?.setBrushColor(value) }
            },marginsSize(42,42,4,4,4,4))
        }
        AlertDialog.Builder(this).setTitle("Color palette").setView(row)
            .setPositiveButton("Close",null).show()
    }

    private fun sizeDialog() {
        val seek=SeekBar(this).apply {
            max=119
            progress=((editor?.brushSize ?: 18f).toInt()-1).coerceIn(0,119)
        }
        AlertDialog.Builder(this).setTitle("Brush size").setMessage("1–120 px").setView(seek)
            .setPositiveButton("Apply"){ _,_ -> editor?.setBrushSize((seek.progress+1).toFloat()) }
            .setNegativeButton("Cancel",null).show()
    }

    private fun rulerDialog() {
        val values=RulerMode.values()
        AlertDialog.Builder(this).setTitle("Ruler")
            .setItems(values.map { it.name }.toTypedArray()){ _,which -> editor?.setRuler(values[which]) }.show()
    }

    private fun symmetryDialog() {
        val values=SymmetryMode.values()
        AlertDialog.Builder(this).setTitle("Symmetry")
            .setItems(values.map { it.name.replace('_',' ') }.toTypedArray()){ _,which -> editor?.setSymmetry(values[which]) }.show()
    }

    private fun layersDialog() {
        val view=editor ?: return
        AlertDialog.Builder(this)
            .setTitle("Layers • " + view.currentLayerTitle())
            .setItems(view.layerNames().toTypedArray()){ _,which ->
                view.selectLayer(which)
                layerActions()
            }
            .setPositiveButton("Add"){ _,_ -> view.addLayer() }
            .setNeutralButton("Done",null).show()
    }

    private fun layerActions() {
        val items=arrayOf(
            "Duplicate","Delete","Move Up","Move Down","Hide / Show",
            "Blend Mode","Opacity","Flip Horizontal","Flip Vertical","Rotate 90°"
        )
        AlertDialog.Builder(this).setTitle(editor?.currentLayerTitle() ?: "Layer")
            .setItems(items){ _,which ->
                val view=editor ?: return@setItems
                when(which) {
                    0 -> view.duplicateLayer()
                    1 -> view.deleteLayer()
                    2 -> view.moveLayerUp()
                    3 -> view.moveLayerDown()
                    4 -> view.toggleLayerVisibility()
                    5 -> view.cycleBlendMode()
                    6 -> opacityDialog()
                    7 -> view.flipHorizontal()
                    8 -> view.flipVertical()
                    9 -> view.rotate90()
                }
            }.show()
    }

    private fun opacityDialog() {
        val seek=SeekBar(this).apply {
            max=255
            progress=editor?.project?.layer()?.opacity ?: 255
        }
        AlertDialog.Builder(this).setTitle("Layer opacity").setView(seek)
            .setPositiveButton("Apply"){ _,_ -> editor?.setLayerOpacity(seek.progress) }
            .setNegativeButton("Cancel",null).show()
    }

    private fun textDialog() {
        val input=EditText(this).apply { hint="Type text" }
        AlertDialog.Builder(this).setTitle("Add text").setView(input)
            .setPositiveButton("Add"){ _,_ ->
                val value=input.text.toString().trim()
                if(value.isNotEmpty()) editor?.addText(value)
            }
            .setNegativeButton("Cancel",null).show()
    }

    private fun fpsDialog() {
        val seek=SeekBar(this).apply {
            max=29
            progress=(editor?.project?.fps ?: 12)-1
        }
        AlertDialog.Builder(this).setTitle("Animation FPS").setMessage("1–30 FPS").setView(seek)
            .setPositiveButton("Apply"){ _,_ -> editor?.setFps(seek.progress+1); refreshTimeline() }
            .setNegativeButton("Cancel",null).show()
    }

    private fun resourcesDialog() {
        val items=arrayOf("Assets","Textures","Templates")
        AlertDialog.Builder(this).setTitle("Creative resources").setItems(items){ _,which ->
            when(which) {
                0 -> assetCategories()
                1 -> textureDialog()
                2 -> templateDialog()
            }
        }.show()
    }

    private fun assetCategories() {
        val categories=AssetCatalog.categories.toTypedArray()
        AlertDialog.Builder(this).setTitle("Assets").setItems(categories){ _,which ->
            val items=AssetCatalog.names(categories[which]).toTypedArray()
            AlertDialog.Builder(this).setTitle(categories[which]).setItems(items){ _,index ->
                editor?.addAsset(items[index])
            }.show()
        }.show()
    }

    private fun textureDialog() {
        val names=TextureCatalog.names.toTypedArray()
        AlertDialog.Builder(this).setTitle("Textures & patterns").setItems(names){ _,which ->
            editor?.addTexture(names[which])
        }.show()
    }

    private fun templateDialog() {
        val templates=TemplateCatalog.templates
        AlertDialog.Builder(this).setTitle("Ready projects")
            .setItems(templates.map { it.title }.toTypedArray()){ _,which ->
                editor?.applyTemplate(templates[which].id)
                refreshTimeline()
            }.show()
    }

    private fun filtersDialog() {
        val names=arrayOf("Brightness +","Brightness -","Contrast +","Grayscale","Invert","Sepia")
        AlertDialog.Builder(this).setTitle("Filters").setItems(names){ _,which ->
            when(which) {
                0 -> editor?.applyFilter(ImageFilters.BRIGHTNESS,35f)
                1 -> editor?.applyFilter(ImageFilters.BRIGHTNESS,-35f)
                2 -> editor?.applyFilter(ImageFilters.CONTRAST,55f)
                3 -> editor?.applyFilter(ImageFilters.GRAYSCALE,0f)
                4 -> editor?.applyFilter(ImageFilters.INVERT,0f)
                5 -> editor?.applyFilter(ImageFilters.SEPIA,0f)
            }
        }.show()
    }

    private fun exportDialog() {
        val items=arrayOf("PNG on White","Transparent PNG","Sprite Sheet","Frame Sequence","MP4 Video")
        AlertDialog.Builder(this).setTitle("Export").setItems(items){ _,which ->
            when(which) {
                0 -> exportPng(false)
                1 -> exportPng(true)
                2 -> exportSpriteSheet()
                3 -> exportFrames()
                4 -> exportVideo()
            }
        }.show()
    }

    private fun saveProject() {
        val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type="application/octet-stream"
            putExtra(Intent.EXTRA_TITLE,safeName(editor?.project?.name ?: "DrawMotion")+".drawmotion")
        }
        startActivityForResult(intent,REQUEST_SAVE)
    }

    private fun openProject() {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type="application/octet-stream"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent,REQUEST_OPEN)
    }

    private fun importImage() {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type="image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent,REQUEST_IMAGE)
    }

    private fun exportPng(transparent:Boolean) {
        val view=editor ?: return
        val bitmap=view.renderFrame(view.currentFrame(),!transparent)
        val values=android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,safeName(view.project.name)+"_"+System.currentTimeMillis()+".png")
            put(MediaStore.Images.Media.MIME_TYPE,"image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH,android.os.Environment.DIRECTORY_PICTURES+"/DrawMotion")
        }
        val uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        try {
            if(uri!=null) {
                contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                toast(if(transparent) "Transparent PNG exported" else "PNG exported")
            } else toast("Export failed")
        } catch(_:Exception) {
            toast("Export failed")
        } finally {
            bitmap.recycle()
        }
    }

    private fun exportSpriteSheet() {
        val view=editor ?: return
        val frames=view.renderAllFrames()
        if(frames.isEmpty()) return
        try {
            val cols=minOf(4,frames.size)
            val rows=(frames.size+cols-1)/cols
            val sheet=Bitmap.createBitmap(
                frames.first().width*cols,
                frames.first().height*rows,
                Bitmap.Config.ARGB_8888
            )
            val canvas=android.graphics.Canvas(sheet)
            canvas.drawColor(Color.WHITE)
            frames.forEachIndexed { index,bitmap ->
                canvas.drawBitmap(bitmap,
                    (index%cols*bitmap.width).toFloat(),
                    (index/cols*bitmap.height).toFloat(),null)
                bitmap.recycle()
            }
            val values=android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME,safeName(view.project.name)+"_sprites.png")
                put(MediaStore.Images.Media.MIME_TYPE,"image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH,android.os.Environment.DIRECTORY_PICTURES+"/DrawMotion")
            }
            val uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
            if(uri!=null) {
                contentResolver.openOutputStream(uri)?.use { sheet.compress(Bitmap.CompressFormat.PNG,100,it) }
                toast("Sprite sheet exported")
            }
            sheet.recycle()
        } catch(_:OutOfMemoryError) {
            toast("Sprite sheet is too large for this device")
        }
    }

    private fun exportFrames() {
        val view=editor ?: return
        view.renderAllFrames().forEachIndexed { index,bitmap ->
            val values=android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME,
                    safeName(view.project.name)+"_Frame_"+String.format(Locale.US,"%03d",index+1)+".png")
                put(MediaStore.Images.Media.MIME_TYPE,"image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH,android.os.Environment.DIRECTORY_PICTURES+"/DrawMotion/Frames")
            }
            val uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
            if(uri!=null) contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
            bitmap.recycle()
        }
        toast("Frame sequence exported")
    }

    private fun exportVideo() {
        val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type="video/mp4"
            putExtra(Intent.EXTRA_TITLE,safeName(editor?.project?.name ?: "DrawMotion")+".mp4")
        }
        startActivityForResult(intent,REQUEST_VIDEO)
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK) return
        val uri=data?.data ?: return

        when(requestCode) {
            REQUEST_SAVE -> {
                val ok=editor?.let { ProjectStore.save(this,uri,it.project) } == true
                toast(if(ok) "Project saved" else "Project save failed")
            }
            REQUEST_OPEN -> openLoadedProject(uri)
            REQUEST_IMAGE -> {
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val bitmap=BitmapFactory.decodeStream(input)
                        if(bitmap!=null) {
                            editor?.addImportedBitmap(bitmap)
                            bitmap.recycle()
                        }
                    }
                } catch(_:Exception) {
                    toast("Image import failed")
                }
            }
            REQUEST_VIDEO -> {
                val view=editor ?: return
                Thread {
                    val frames=view.renderAllFrames()
                    val ok=VideoExporter.export(this,uri,frames,view.project.fps)
                    frames.forEach { if(!it.isRecycled) it.recycle() }
                    runOnUiThread { toast(if(ok) "MP4 exported" else "Video export failed") }
                }.start()
            }
        }
    }

    private fun openLoadedProject(uri:Uri) {
        val source=ProjectStore.load(this,uri)
        if(source==null) {
            toast("Invalid project file")
            return
        }

        editor?.stopPlayback()
        editor?.project?.dispose()

        val newEditor=DrawCanvasView(this)
        newEditor.project.dispose()
        newEditor.project.width=source.width
        newEditor.project.height=source.height
        newEditor.project.name=source.name
        newEditor.project.fps=source.fps
        newEditor.project.animationMode=source.animationMode
        source.frames.forEach { newEditor.project.frames += it }
        newEditor.project.currentFrame=0
        newEditor.project.currentLayer=1.coerceAtMost(newEditor.project.current().layers.lastIndex)
        editor=newEditor

        root.removeAllViews()
        buildTopBar()
        buildToolStrip()
        root.addView(newEditor,LinearLayout.LayoutParams(-1,0,1f))
        if(source.animationMode) buildTimeline()
        setContentView(root)
        refreshTimeline()
        source.frames.clear()
    }

    override fun onBackPressed() {
        if(editor!=null) {
            editor?.stopPlayback()
            editor=null
            showHome()
        } else super.onBackPressed()
    }

    private fun actionCard(title:String,subtitle:String,click:()->Unit):View =
        LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(18),dp(18),dp(18),dp(18))
            background=rounded(Color.WHITE,20)
            setOnClickListener { click() }
            addView(label(title,18f,text,Typeface.BOLD))
            addView(space(4))
            addView(label(subtitle,13f,muted,Typeface.NORMAL))
        }

    private fun smallCard(title:String,subtitle:String,click:()->Unit):View =
        LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(14),dp(12),dp(14),dp(12))
            background=rounded(Color.WHITE,14)
            setOnClickListener { click() }
            val copy=LinearLayout(this@MainActivity).apply {
                orientation=LinearLayout.VERTICAL
                addView(label(title,14f,text,Typeface.BOLD))
                addView(label(subtitle,11f,muted,Typeface.NORMAL))
            }
            addView(copy,LinearLayout.LayoutParams(0,-2,1f))
            addView(label("›",24f,purple,Typeface.BOLD))
        }

    private fun toolButton(title:String,click:()->Unit)=
        Button(this).apply {
            text=title
            textSize=11f
            isAllCaps=false
            setTextColor(this@MainActivity.text)
            background=rounded(Color.WHITE,11)
            setOnClickListener { click() }
            minHeight=dp(40)
            setPadding(dp(5),0,dp(5),0)
        }

    private fun label(value:String,size:Float,color:Int,style:Int,spacing:Float=0f)=
        TextView(this).apply {
            text=value
            textSize=size
            setTextColor(color)
            typeface=Typeface.create(Typeface.DEFAULT,style)
            letterSpacing=spacing
        }

    private fun rounded(colorValue:Int,radius:Int)=
        GradientDrawable().apply {
            setColor(colorValue)
            cornerRadius=dp(radius).toFloat()
            setStroke(dp(1),border)
        }

    private fun space(height:Int)=Space(this).apply { minimumHeight=dp(height) }

    private fun fixed(width:Int)=LinearLayout.LayoutParams(dp(width),dp(42)).apply {
        setMargins(dp(2),dp(2),dp(2),dp(2))
    }

    private fun equal()=LinearLayout.LayoutParams(0,dp(38),1f).apply {
        setMargins(dp(2),0,dp(2),0)
    }

    private fun margins(l:Int,t:Int,r:Int,b:Int)=LinearLayout.LayoutParams(-1,-2).apply {
        setMargins(dp(l),dp(t),dp(r),dp(b))
    }

    private fun marginsSize(w:Int,h:Int,l:Int,t:Int,r:Int,b:Int)=LinearLayout.LayoutParams(dp(w),dp(h)).apply {
        setMargins(dp(l),dp(t),dp(r),dp(b))
    }

    private fun Int.dp():Int=dp(this)

    private fun dp(value:Int):Int=(value*resources.displayMetrics.density+.5f).toInt()

    private fun safeName(value:String):String {
        val cleaned=value.replace(Regex("[^A-Za-z0-9._-]+"),"_")
        return cleaned.ifEmpty { "DrawMotion" }
    }

    private fun toast(message:String)=Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}
