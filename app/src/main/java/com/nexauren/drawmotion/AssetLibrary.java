package com.nexauren.drawmotion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Locale;

public final class AssetLibrary {
 public static final String[] CATEGORIES={"Characters","People","Clothes","Vehicles","City","Worlds","Nature","Objects","Effects","Backgrounds"};
 private AssetLibrary(){}
 public static String[] names(String cat){
  String k=cat==null?"":cat.toLowerCase(Locale.US);
  if(k.contains("character"))return new String[]{"Hero","Robot","Mage","Ninja","Alien","Astronaut"};
  if(k.contains("people"))return new String[]{"Person","Runner","Worker","Traveler","Teacher","Musician"};
  if(k.contains("clothes"))return new String[]{"T-Shirt","Hoodie","Jacket","Dress","Pants","Cap"};
  if(k.contains("vehicle"))return new String[]{"Airplane","Car","Bus","Bike","Rocket","Boat"};
  if(k.contains("city"))return new String[]{"Street","Skyscraper","House","Shop","Bridge","Tower"};
  if(k.contains("world"))return new String[]{"Planet","Moon","Space","Ocean","Mountain","Desert"};
  if(k.contains("nature"))return new String[]{"Tree","Pine","Flower","Cloud","Sun","Rock"};
  if(k.contains("object"))return new String[]{"Chair","Table","Phone","Lamp","Book","Box"};
  if(k.contains("effect"))return new String[]{"Spark","Smoke","Impact","Speed Lines","Glow","Star"};
  return new String[]{"Sky","Forest","City Night","Sunset","Paper","Pattern"};
 }
 public static Bitmap create(String name,int w,int h){
  Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
  Paint f=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG),p=new Paint(Paint.ANTI_ALIAS_FLAG);
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(Math.max(5,w*.012f));
  float cx=w*.5f,cy=h*.53f;String k=name.toLowerCase(Locale.US);
  if(k.contains("hero")||k.contains("person")||k.contains("runner")||k.contains("traveler")||k.contains("teacher")||k.contains("musician")||k.contains("worker")){
   f.setColor(Color.rgb(34,211,238));c.drawCircle(cx,h*.28f,w*.08f,f);p.setColor(Color.rgb(31,41,55));c.drawLine(cx,h*.36f,cx,h*.64f,p);c.drawLine(cx,h*.42f,cx-w*.12f,h*.53f,p);c.drawLine(cx,h*.42f,cx+w*.12f,h*.53f,p);c.drawLine(cx,h*.64f,cx-w*.10f,h*.84f,p);c.drawLine(cx,h*.64f,cx+w*.10f,h*.84f,p);f.setColor(Color.rgb(124,58,237));c.drawCircle(cx+w*.03f,h*.26f,w*.02f,f);
  }else if(k.contains("robot")){
   f.setColor(Color.rgb(148,163,184));c.drawRoundRect(new RectF(cx-w*.15f,h*.30f,cx+w*.15f,h*.58f),24,24,f);p.setColor(Color.rgb(31,41,55));c.drawCircle(cx-w*.06f,h*.40f,w*.025f,p);c.drawCircle(cx+w*.06f,h*.40f,w*.025f,p);c.drawLine(cx,h*.30f,cx,h*.20f,p);c.drawCircle(cx,h*.18f,w*.018f,p);c.drawLine(cx-w*.15f,h*.49f,cx-w*.24f,h*.58f,p);c.drawLine(cx+w*.15f,h*.49f,cx+w*.24f,h*.58f,p);
  }else if(k.contains("mage")||k.contains("ninja")){
   f.setColor(k.contains("mage")?Color.rgb(99,102,241):Color.rgb(17,24,39));c.drawCircle(cx,h*.34f,w*.08f,f);Path q=new Path();q.moveTo(cx-w*.1f,h*.40f);q.lineTo(cx-w*.22f,h*.78f);q.lineTo(cx+w*.22f,h*.78f);q.lineTo(cx+w*.1f,h*.40f);q.close();c.drawPath(q,f);
  }else if(k.contains("alien")){
   f.setColor(Color.rgb(74,222,128));c.drawOval(new RectF(cx-w*.10f,h*.20f,cx+w*.10f,h*.45f),f);p.setColor(Color.rgb(31,41,55));c.drawOval(new RectF(cx-w*.07f,h*.27f,cx-w*.01f,h*.36f),p);c.drawOval(new RectF(cx+w*.01f,h*.27f,cx+w*.07f,h*.36f),p);
  }else if(k.contains("astronaut")){
   f.setColor(Color.WHITE);c.drawCircle(cx,h*.28f,w*.09f,f);p.setColor(Color.rgb(31,41,55));c.drawCircle(cx,h*.28f,w*.06f,p);p.setStyle(Paint.Style.STROKE);c.drawLine(cx,h*.37f,cx,h*.68f,p);c.drawLine(cx,h*.46f,cx-w*.14f,h*.56f,p);c.drawLine(cx,h*.46f,cx+w*.14f,h*.56f,p);c.drawLine(cx,h*.68f,cx-w*.10f,h*.83f,p);c.drawLine(cx,h*.68f,cx+w*.10f,h*.83f,p);
  }else if(k.contains("airplane")){
   f.setColor(Color.rgb(148,163,184));Path q=new Path();q.moveTo(cx,h*.15f);q.lineTo(cx+w*.07f,h*.62f);q.lineTo(cx+w*.22f,h*.72f);q.lineTo(cx+w*.12f,h*.75f);q.lineTo(cx+w*.04f,h*.69f);q.lineTo(cx,h*.90f);q.lineTo(cx-w*.04f,h*.69f);q.lineTo(cx-w*.12f,h*.75f);q.lineTo(cx-w*.22f,h*.72f);q.lineTo(cx-w*.07f,h*.62f);q.close();c.drawPath(q,f);
  }else if(k.contains("car")){
   f.setColor(Color.rgb(239,68,68));c.drawRoundRect(new RectF(w*.20f,h*.48f,w*.80f,h*.68f),30,30,f);c.drawRect(w*.32f,h*.36f,w*.68f,h*.50f,f);f.setColor(Color.rgb(147,197,253));c.drawRect(w*.36f,h*.39f,w*.48f,h*.48f,f);c.drawRect(w*.52f,h*.39f,w*.64f,h*.48f,f);f.setColor(Color.DKGRAY);c.drawCircle(w*.32f,h*.70f,w*.06f,f);c.drawCircle(w*.68f,h*.70f,w*.06f,f);
  }else if(k.contains("bus")){
   f.setColor(Color.rgb(59,130,246));c.drawRoundRect(new RectF(w*.18f,h*.28f,w*.82f,h*.72f),32,32,f);f.setColor(Color.rgb(191,219,254));c.drawRect(w*.25f,h*.36f,w*.75f,h*.50f,f);f.setColor(Color.DKGRAY);c.drawCircle(w*.30f,h*.74f,w*.055f,f);c.drawCircle(w*.70f,h*.74f,w*.055f,f);
  }else if(k.contains("bike")){
   p.setColor(Color.rgb(31,41,55));c.drawCircle(w*.28f,h*.67f,w*.10f,p);c.drawCircle(w*.72f,h*.67f,w*.10f,p);c.drawLine(w*.28f,h*.67f,w*.45f,h*.49f,p);c.drawLine(w*.45f,h*.49f,w*.72f,h*.67f,p);c.drawLine(w*.45f,h*.49f,w*.58f,h*.49f,p);
  }else if(k.contains("rocket")){
   f.setColor(Color.rgb(226,232,240));c.drawOval(new RectF(cx-w*.09f,h*.16f,cx+w*.09f,h*.63f),f);f.setColor(Color.rgb(239,68,68));c.drawCircle(cx,h*.36f,w*.03f,f);Path q=new Path();q.moveTo(cx-w*.04f,h*.60f);q.lineTo(cx,h*.84f);q.lineTo(cx+w*.04f,h*.60f);q.close();f.setColor(Color.rgb(251,146,60));c.drawPath(q,f);
  }else if(k.contains("boat")){
   f.setColor(Color.rgb(180,83,9));Path q=new Path();q.moveTo(w*.22f,h*.58f);q.lineTo(w*.78f,h*.58f);q.lineTo(w*.65f,h*.75f);q.lineTo(w*.35f,h*.75f);q.close();c.drawPath(q,f);p.setColor(Color.rgb(31,41,55));c.drawLine(cx,h*.22f,cx,h*.58f,p);Path sail=new Path();sail.moveTo(cx,h*.24f);sail.lineTo(cx,h*.50f);sail.lineTo(w*.60f,h*.50f);sail.close();f.setColor(Color.WHITE);c.drawPath(sail,f);
  }else if(k.contains("tree")||k.contains("pine")){
   f.setColor(Color.rgb(120,53,15));c.drawRect(cx-w*.04f,h*.50f,cx+w*.04f,h*.82f,f);f.setColor(Color.rgb(34,197,94));for(int i=0;i<3;i++){Path q=new Path();q.moveTo(cx,h*(.18f+i*.14f));q.lineTo(cx-w*(.22f-i*.03f),h*(.52f+i*.08f));q.lineTo(cx+w*(.22f-i*.03f),h*(.52f+i*.08f));q.close();c.drawPath(q,f);}
  }else if(k.contains("flower")){
   f.setColor(Color.rgb(236,72,153));for(int i=0;i<6;i++){double a=i*Math.PI/3;c.drawCircle(cx+(float)Math.cos(a)*w*.08f,cy+(float)Math.sin(a)*w*.08f,w*.05f,f);}f.setColor(Color.rgb(250,204,21));c.drawCircle(cx,cy,w*.045f,f);
  }else if(k.contains("house")||k.contains("shop")){
   f.setColor(Color.rgb(248,113,113));c.drawRect(w*.23f,h*.42f,w*.77f,h*.78f,f);Path q=new Path();q.moveTo(w*.18f,h*.42f);q.lineTo(cx,h*.20f);q.lineTo(w*.82f,h*.42f);q.close();f.setColor(Color.rgb(124,58,237));c.drawPath(q,f);f.setColor(Color.WHITE);c.drawRect(w*.45f,h*.58f,w*.55f,h*.78f,f);c.drawRect(w*.30f,h*.50f,w*.40f,h*.60f,f);c.drawRect(w*.60f,h*.50f,w*.70f,h*.60f,f);
  }else if(k.contains("skyscraper")||k.contains("tower")){
   f.setColor(Color.rgb(71,85,105));c.drawRect(w*.30f,h*.12f,w*.70f,h*.86f,f);f.setColor(Color.rgb(125,211,252));for(int y=0;y<5;y++)for(int x=0;x<3;x++)c.drawRect(w*(.35f+x*.11f),h*(.20f+y*.12f),w*(.41f+x*.11f),h*(.27f+y*.12f),f);
  }else if(k.contains("street")||k.contains("bridge")){
   f.setColor(Color.rgb(71,85,105));c.drawRect(0,h*.68f,w,h*.82f,f);f.setColor(Color.WHITE);for(int x=0;x<w;x+=Math.max(1,w/6))c.drawRect(x,h*.74f,x+w/12,h*.77f,f);
  }else if(k.contains("planet")){f.setColor(Color.rgb(59,130,246));c.drawCircle(cx,cy,w*.18f,f);f.setColor(Color.rgb(34,197,94));c.drawOval(new RectF(cx-w*.13f,cy-w*.03f,cx+w*.13f,cy+w*.06f),f);}
  else if(k.contains("moon")){f.setColor(Color.LTGRAY);c.drawCircle(cx,cy,w*.18f,f);f.setColor(Color.WHITE);c.drawCircle(cx+w*.06f,cy-w*.04f,w*.16f,f);}
  else if(k.contains("sun")){f.setColor(Color.rgb(250,204,21));c.drawCircle(cx,cy,w*.12f,f);p.setColor(Color.rgb(250,204,21));for(int i=0;i<12;i++){double a=i*Math.PI/6;c.drawLine(cx+(float)Math.cos(a)*w*.15f,cy+(float)Math.sin(a)*w*.15f,cx+(float)Math.cos(a)*w*.22f,cy+(float)Math.sin(a)*w*.22f,p);}}
  else if(k.contains("mountain")){Path q=new Path();q.moveTo(w*.12f,h*.82f);q.lineTo(w*.45f,h*.28f);q.lineTo(w*.55f,h*.46f);q.lineTo(w*.70f,h*.22f);q.lineTo(w*.90f,h*.82f);q.close();f.setColor(Color.rgb(100,116,139));c.drawPath(q,f);}
  else if(k.contains("ocean")){f.setColor(Color.rgb(14,165,233));c.drawRect(0,h*.55f,w,h*.82f,f);}
  else if(k.contains("clothes")||k.contains("t-shirt")||k.contains("hoodie")||k.contains("jacket")||k.contains("dress")||k.contains("pants")||k.contains("cap")){
   f.setColor(Color.rgb(124,58,237));if(k.contains("cap")){c.drawOval(new RectF(w*.33f,h*.25f,w*.67f,h*.42f),f);c.drawRect(w*.48f,h*.33f,w*.72f,h*.39f,f);}else if(k.contains("dress")){Path q=new Path();q.moveTo(cx-w*.08f,h*.25f);q.lineTo(cx+w*.08f,h*.25f);q.lineTo(cx+w*.20f,h*.72f);q.lineTo(cx-w*.20f,h*.72f);q.close();c.drawPath(q,f);}else c.drawRoundRect(new RectF(w*.34f,h*.25f,w*.66f,h*.72f),24,24,f);
  }else{f.setColor(Color.rgb(226,232,240));c.drawRoundRect(new RectF(w*.24f,h*.28f,w*.76f,h*.72f),28,28,f);p.setColor(Color.rgb(124,58,237));c.drawCircle(cx,cy,w*.08f,p);}
  return b;
 }
}