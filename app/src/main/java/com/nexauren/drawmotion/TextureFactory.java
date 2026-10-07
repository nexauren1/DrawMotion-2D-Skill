package com.nexauren.drawmotion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Locale;

public final class TextureFactory {
    public static final String[] NAMES={"Paper","Dots","Grid","Hatch","Crosshatch","Halftone","Wood","Fabric","Noise","Stars","Bubbles","Diagonal"};
    private TextureFactory(){}
    public static Bitmap create(String id,int w,int h,int base,int accent){
        Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(base);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(accent);p.setStrokeWidth(Math.max(1,w/300f));int step=Math.max(12,w/18);String key=id==null?"paper":id.toLowerCase(Locale.US);
        if(key.equals("dots"))for(int y=0;y<h;y+=step)for(int x=0;x<w;x+=step)c.drawCircle(x,y,Math.max(1,step*.11f),p);
        else if(key.equals("grid")){for(int x=0;x<w;x+=step)c.drawLine(x,0,x,h,p);for(int y=0;y<h;y+=step)c.drawLine(0,y,w,y,p);}
        else if(key.equals("hatch"))for(int x=-h;x<w;x+=step)c.drawLine(x,0,x+h,h,p);
        else if(key.equals("crosshatch")){for(int x=-h;x<w;x+=step)c.drawLine(x,0,x+h,h,p);for(int x=0;x<w+h;x+=step)c.drawLine(x,0,x-h,h,p);}
        else if(key.equals("halftone"))for(int y=0;y<h;y+=step)for(int x=0;x<w;x+=step)c.drawCircle(x,y,Math.max(1,step*.18f),p);
        else if(key.equals("wood"))for(int y=0;y<h;y+=step){Path q=new Path();q.moveTo(0,y);q.cubicTo(w*.25f,y-step*.8f,w*.70f,y+step*.8f,w,y);c.drawPath(q,p);}
        else if(key.equals("fabric")){for(int x=0;x<w;x+=step)c.drawLine(x,0,x,h,p);for(int y=0;y<h;y+=step)c.drawLine(0,y,w,y,p);}
        else if(key.equals("noise")){java.util.Random r=new java.util.Random(7);Paint d=new Paint();for(int i=0;i<Math.max(1000,w*h/200);i++){d.setColor((r.nextBoolean()?0x22FFFFFF:0x22000000)|(accent&0x00FFFFFF));c.drawPoint(r.nextInt(w),r.nextInt(h),d);}}
        else if(key.equals("stars"))for(int y=step/2;y<h;y+=step*2)for(int x=step/2;x<w;x+=step*2)c.drawCircle(x,y,Math.max(1,step*.14f),p);
        else if(key.equals("bubbles"))for(int y=step;y<h;y+=step*2)for(int x=step;x<w;x+=step*2)c.drawCircle(x,y,step*.34f,p);
        else if(key.equals("diagonal"))for(int x=-h;x<w;x+=step)c.drawLine(x,h,x+h,0,p);
        else for(int y=0;y<h;y+=step*2)c.drawLine(0,y,w,y,p);
        return b;
    }
}