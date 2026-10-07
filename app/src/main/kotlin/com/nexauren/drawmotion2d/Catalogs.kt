package com.nexauren.drawmotion2d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

object AssetCatalog {
    val categories = listOf(
        "Characters", "People", "Clothes", "Vehicles", "City",
        "Worlds", "Nature", "Objects", "Effects", "Backgrounds"
    )

    private val items = mapOf(
        "Characters" to listOf("Hero", "Robot", "Mage", "Ninja", "Alien", "Astronaut"),
        "People" to listOf("Person", "Runner", "Worker", "Traveler", "Teacher", "Musician"),
        "Clothes" to listOf("T-Shirt", "Hoodie", "Jacket", "Dress", "Pants", "Cap"),
        "Vehicles" to listOf("Airplane", "Car", "Bus", "Bike", "Rocket", "Boat"),
        "City" to listOf("Street", "Skyscraper", "House", "Shop", "Bridge", "Tower"),
        "Worlds" to listOf("Planet", "Moon", "Space", "Ocean", "Mountain", "Desert"),
        "Nature" to listOf("Tree", "Pine", "Flower", "Cloud", "Sun", "Rock"),
        "Objects" to listOf("Chair", "Table", "Phone", "Lamp", "Book", "Box"),
        "Effects" to listOf("Spark", "Smoke", "Impact", "Speed Lines", "Glow", "Star"),
        "Backgrounds" to listOf("Sky", "Forest", "City Night", "Sunset", "Paper", "Pattern")
    )

    fun names(category: String): List<String> = items[category].orEmpty()

    fun create(name: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = max(5f, width * 0.012f)
        }

        val cx = width * 0.5f
        val cy = height * 0.52f
        val key = name.lowercase()

        when {
            key.contains("hero") || key.contains("person") || key.contains("runner") ||
                key.contains("traveler") || key.contains("teacher") || key.contains("musician") ||
                key.contains("worker") -> {
                fill.color = Color.rgb(34, 211, 238)
                canvas.drawCircle(cx, height * 0.28f, width * 0.08f, fill)
                line.color = Color.rgb(31, 41, 55)
                canvas.drawLine(cx, height * 0.36f, cx, height * 0.64f, line)
                canvas.drawLine(cx, height * 0.42f, cx - width * 0.12f, height * 0.53f, line)
                canvas.drawLine(cx, height * 0.42f, cx + width * 0.12f, height * 0.53f, line)
                canvas.drawLine(cx, height * 0.64f, cx - width * 0.1f, height * 0.84f, line)
                canvas.drawLine(cx, height * 0.64f, cx + width * 0.1f, height * 0.84f, line)
            }
            key.contains("robot") -> {
                fill.color = Color.rgb(148, 163, 184)
                canvas.drawRoundRect(
                    RectF(cx - width*.15f, height*.3f, cx + width*.15f, height*.58f),
                    24f, 24f, fill
                )
                line.color = Color.rgb(31, 41, 55)
                canvas.drawCircle(cx - width*.06f, height*.4f, width*.02f, line)
                canvas.drawCircle(cx + width*.06f, height*.4f, width*.02f, line)
            }
            key.contains("mage") || key.contains("ninja") -> {
                fill.color = if (key.contains("mage")) Color.rgb(99,102,241) else Color.rgb(17,24,39)
                canvas.drawCircle(cx, height*.34f, width*.08f, fill)
                val body = Path().apply {
                    moveTo(cx-width*.1f, height*.4f)
                    lineTo(cx-width*.22f, height*.78f)
                    lineTo(cx+width*.22f, height*.78f)
                    lineTo(cx+width*.1f, height*.4f)
                    close()
                }
                canvas.drawPath(body, fill)
            }
            key.contains("alien") -> {
                fill.color = Color.rgb(74,222,128)
                canvas.drawOval(RectF(cx-width*.1f, height*.2f, cx+width*.1f, height*.45f), fill)
            }
            key.contains("astronaut") -> {
                fill.color = Color.WHITE
                canvas.drawCircle(cx, height*.28f, width*.09f, fill)
                line.color = Color.rgb(31,41,55)
                canvas.drawCircle(cx, height*.28f, width*.06f, line)
                canvas.drawLine(cx, height*.37f, cx, height*.68f, line)
            }
            key.contains("car") -> {
                fill.color = Color.rgb(239,68,68)
                canvas.drawRoundRect(RectF(width*.2f,height*.48f,width*.8f,height*.68f),30f,30f,fill)
                canvas.drawRect(width*.32f,height*.36f,width*.68f,height*.5f,fill)
                fill.color = Color.rgb(147,197,253)
                canvas.drawRect(width*.36f,height*.39f,width*.48f,height*.48f,fill)
                canvas.drawRect(width*.52f,height*.39f,width*.64f,height*.48f,fill)
            }
            key.contains("airplane") -> {
                fill.color = Color.rgb(148,163,184)
                val body = Path().apply {
                    moveTo(cx, height*.15f)
                    lineTo(cx+width*.07f, height*.62f)
                    lineTo(cx+width*.22f, height*.72f)
                    lineTo(cx+width*.12f, height*.75f)
                    lineTo(cx, height*.69f)
                    lineTo(cx-width*.12f, height*.75f)
                    lineTo(cx-width*.22f, height*.72f)
                    lineTo(cx-width*.07f, height*.62f)
                    close()
                }
                canvas.drawPath(body, fill)
            }
            key.contains("tree") || key.contains("pine") -> {
                fill.color = Color.rgb(120,53,15)
                canvas.drawRect(cx-width*.04f,height*.5f,cx+width*.04f,height*.82f,fill)
                fill.color = Color.rgb(34,197,94)
                repeat(3) { i ->
                    val tree = Path().apply {
                        moveTo(cx, height*(.18f+i*.14f))
                        lineTo(cx-width*(.22f-i*.03f), height*(.52f+i*.08f))
                        lineTo(cx+width*(.22f-i*.03f), height*(.52f+i*.08f))
                        close()
                    }
                    canvas.drawPath(tree, fill)
                }
            }
            key.contains("house") || key.contains("shop") -> {
                fill.color = Color.rgb(248,113,113)
                canvas.drawRect(width*.23f,height*.42f,width*.77f,height*.78f,fill)
                val roof = Path().apply {
                    moveTo(width*.18f,height*.42f); lineTo(cx,height*.2f); lineTo(width*.82f,height*.42f); close()
                }
                fill.color = Color.rgb(124,58,237)
                canvas.drawPath(roof, fill)
            }
            key.contains("planet") -> {
                fill.color = Color.rgb(59,130,246)
                canvas.drawCircle(cx,cy,width*.18f,fill)
                fill.color = Color.rgb(34,197,94)
                canvas.drawOval(RectF(cx-width*.13f,cy-width*.03f,cx+width*.13f,cy+width*.06f),fill)
            }
            key.contains("moon") -> {
                fill.color = Color.LTGRAY
                canvas.drawCircle(cx,cy,width*.18f,fill)
                fill.color = Color.rgb(190,195,205)
                canvas.drawCircle(cx-width*.06f,cy-width*.02f,width*.025f,fill)
            }
            key.contains("sun") -> {
                fill.color = Color.rgb(250,204,21)
                canvas.drawCircle(cx,cy,width*.12f,fill)
                line.color = Color.rgb(250,204,21)
                repeat(12) { i ->
                    val a = i*Math.PI/6.0
                    canvas.drawLine(
                        cx+cos(a).toFloat()*width*.15f, cy+sin(a).toFloat()*width*.15f,
                        cx+cos(a).toFloat()*width*.22f, cy+sin(a).toFloat()*width*.22f, line
                    )
                }
            }
            key.contains("flower") -> {
                fill.color = Color.rgb(236,72,153)
                repeat(6) { i ->
                    val a = i*Math.PI/3.0
                    canvas.drawCircle(cx+cos(a).toFloat()*width*.08f, cy+sin(a).toFloat()*width*.08f, width*.05f, fill)
                }
                fill.color = Color.rgb(250,204,21)
                canvas.drawCircle(cx,cy,width*.045f,fill)
            }
            key.contains("spark") || key.contains("star") || key.contains("impact") -> {
                line.color = Color.rgb(124,58,237)
                repeat(8) { i ->
                    val a = i*Math.PI/4.0
                    canvas.drawLine(
                        cx+cos(a).toFloat()*width*.05f, cy+sin(a).toFloat()*width*.05f,
                        cx+cos(a).toFloat()*width*.2f, cy+sin(a).toFloat()*width*.2f, line
                    )
                }
            }
            else -> {
                fill.color = Color.rgb(226,232,240)
                canvas.drawRoundRect(RectF(width*.22f,height*.24f,width*.78f,height*.76f),28f,28f,fill)
                fill.color = Color.rgb(124,58,237)
                canvas.drawCircle(cx,cy,width*.08f,fill)
            }
        }
        return bitmap
    }
}

object TextureCatalog {
    val names = listOf("Paper","Dots","Grid","Hatch","Crosshatch","Halftone","Wood","Fabric","Noise","Stars","Bubbles","Diagonal")

    fun create(name: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFF8FAFC.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x3347556B; strokeWidth = max(1f,width/300f) }
        val step = max(12,width/18)
        when (name.lowercase()) {
            "dots" -> for (y in 0 until height step step) for (x in 0 until width step step)
                canvas.drawCircle(x.toFloat(),y.toFloat(),max(1f,step*.11f),paint)
            "grid","fabric" -> {
                for (x in 0 until width step step) canvas.drawLine(x.toFloat(),0f,x.toFloat(),height.toFloat(),paint)
                for (y in 0 until height step step) canvas.drawLine(0f,y.toFloat(),width.toFloat(),y.toFloat(),paint)
            }
            "hatch","diagonal" -> for (x in -height until width step step)
                canvas.drawLine(x.toFloat(),0f,(x+height).toFloat(),height.toFloat(),paint)
            "crosshatch" -> {
                for (x in -height until width step step) canvas.drawLine(x.toFloat(),0f,(x+height).toFloat(),height.toFloat(),paint)
                for (x in 0..(width+height) step step) canvas.drawLine(x.toFloat(),0f,(x-height).toFloat(),height.toFloat(),paint)
            }
            "halftone","stars" -> for (y in step until height step step*2) for (x in step until width step step*2)
                canvas.drawCircle(x.toFloat(),y.toFloat(),step*.18f,paint)
            "wood" -> for (y in 0 until height step step) {
                val path = Path().apply {
                    moveTo(0f,y.toFloat())
                    cubicTo(width*.25f,y-step*.8f,width*.7f,y+step*.8f,width.toFloat(),y.toFloat())
                }
                canvas.drawPath(path,paint)
            }
            "noise" -> {
                val random = Random(7)
                val dot = Paint()
                repeat(max(1000,width*height/200)) {
                    dot.color = if(random.nextBoolean()) 0x22000000 or 0x0047556B else 0x22FFFFFF
                    canvas.drawPoint(random.nextInt(width).toFloat(),random.nextInt(height).toFloat(),dot)
                }
            }
            "bubbles" -> for (y in step until height step step*2) for (x in step until width step step*2)
                canvas.drawCircle(x.toFloat(),y.toFloat(),step*.34f,paint)
            else -> for (y in 0 until height step step*2) canvas.drawLine(0f,y.toFloat(),width.toFloat(),y.toFloat(),paint)
        }
        return bitmap
    }
}

object TemplateCatalog {
    data class Template(val id: String,val title: String,val frames: Int)

    val templates = listOf(
        Template("blank","Blank Canvas",1),
        Template("bounce","Bouncing Ball",8),
        Template("blink","Blink",6),
        Template("impact","Impact",6),
        Template("spin","Spin Loop",8),
        Template("pulse","Pulse",8),
        Template("walk","Walk Cycle",8),
        Template("jump","Jump",7),
        Template("wave","Wave",6),
        Template("run","Run Cycle",8)
    )

    fun create(id: String,width: Int,height: Int): List<Bitmap> {
        val template = templates.firstOrNull { it.id == id } ?: templates.first()
        val result = mutableListOf<Bitmap>()

        for (i in 0 until template.frames) {
            val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(124,58,237) }
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(31,41,55); style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND; strokeWidth = max(6f,width*.012f)
            }
            val p = i/template.frames.toFloat()

            when(template.id) {
                "bounce" -> canvas.drawCircle(width*(.15f+.1f*i),height*(.3f+.4f*(1f-kotlin.math.abs(2f*p-1f))),width*.07f,fill)
                "blink" -> {
                    canvas.drawCircle(width*.5f,height*.45f,width*.18f,Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(255,213,165) })
                    if(i==2 || i==3) {
                        canvas.drawLine(width*.44f,height*.44f,width*.48f,height*.44f,stroke)
                        canvas.drawLine(width*.52f,height*.44f,width*.56f,height*.44f,stroke)
                    } else {
                        fill.color = Color.rgb(31,41,55)
                        canvas.drawCircle(width*.46f,height*.44f,width*.014f,fill)
                        canvas.drawCircle(width*.54f,height*.44f,width*.014f,fill)
                    }
                }
                "impact" -> {
                    val radius = width*(.03f+i*.035f)
                    canvas.drawCircle(width/2f,height/2f,radius,stroke)
                    repeat(8) { k ->
                        val a = k*Math.PI/4.0
                        canvas.drawLine(
                            width/2f+cos(a).toFloat()*radius*1.3f,
                            height/2f+sin(a).toFloat()*radius*1.3f,
                            width/2f+cos(a).toFloat()*radius*(2f+i*.15f),
                            height/2f+sin(a).toFloat()*radius*(2f+i*.15f),stroke
                        )
                    }
                }
                "spin" -> {
                    canvas.save(); canvas.rotate(i*45f,width/2f,height/2f)
                    canvas.drawRect(width*.43f,height*.25f,width*.57f,height*.75f,fill)
                    canvas.restore()
                }
                "pulse" -> {
                    val phase = if(i<=3)i else 7-i
                    canvas.drawCircle(width/2f,height/2f,width*(.06f+phase*.025f),stroke)
                    canvas.drawCircle(width/2f,height/2f,width*.035f,fill)
                }
                else -> {
                    val x = width*(.2f+.6f*p)
                    val y = if(template.id=="jump") height*(.7f-.25f*kotlin.math.sin(Math.PI*p).toFloat()) else height*.55f
                    canvas.drawCircle(x,y,width*.06f,fill)
                    canvas.drawLine(x,y+width*.06f,x,y+width*.22f,stroke)
                    canvas.drawLine(x,y+width*.12f,x-width*.08f,y+width*.2f,stroke)
                    canvas.drawLine(x,y+width*.12f,x+width*.08f,y+width*.2f,stroke)
                    canvas.drawLine(x,y+width*.22f,x-width*.08f,y+width*.32f,stroke)
                    canvas.drawLine(x,y+width*.22f,x+width*.08f,y+width*.32f,stroke)
                }
            }
            result += bitmap
        }
        return result
    }
}
