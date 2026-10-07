package com.nexauren.drawmotion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class LayerModel {
    public static final int NORMAL=0, MULTIPLY=1, SCREEN=2, ADD=3, OVERLAY=4;
    public String name;
    public Bitmap bitmap;
    public int opacity=255;
    public boolean visible=true;
    public boolean locked=false;
    public int blendMode=NORMAL;

    public LayerModel(String name, Bitmap bitmap){ this.name=name; this.bitmap=bitmap; }

    public LayerModel copy(){
        LayerModel x=new LayerModel(name,bitmap.copy(Bitmap.Config.ARGB_8888,true));
        x.opacity=opacity; x.visible=visible; x.locked=locked; x.blendMode=blendMode; return x;
    }

    public void draw(Canvas canvas, Paint paint){
        if(!visible||bitmap==null)return;
        int oldAlpha=paint.getAlpha();
        PorterDuffXfermode old=(PorterDuffXfermode)paint.getXfermode();
        paint.setAlpha(opacity);
        switch(blendMode){
            case MULTIPLY: paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)); break;
            case SCREEN: paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN)); break;
            case ADD: paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD)); break;
            case OVERLAY: paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY)); break;
            default: paint.setXfermode(null);
        }
        canvas.drawBitmap(bitmap,0,0,paint);
        paint.setAlpha(oldAlpha); paint.setXfermode(old);
    }

    public static String blendName(int mode){
        switch(mode){case MULTIPLY:return "Multiply";case SCREEN:return "Screen";case ADD:return "Add";case OVERLAY:return "Overlay";default:return "Normal";}
    }
}