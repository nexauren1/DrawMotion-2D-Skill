package com.nexauren.drawmotion;

import android.graphics.Bitmap;
import android.graphics.Color;

public final class ImageFilters {
    private ImageFilters(){}
    public static Bitmap apply(Bitmap src,int type,float amount){
        Bitmap out=src.copy(Bitmap.Config.ARGB_8888,true); int w=out.getWidth(),h=out.getHeight(); int[] px=new int[w*h];
        out.getPixels(px,0,w,0,0,w,h); float a=amount;
        for(int i=0;i<px.length;i++){
            int c=px[i],r=Color.red(c),g=Color.green(c),b=Color.blue(c),al=Color.alpha(c);
            if(type==0){r=clamp((int)(r+a));g=clamp((int)(g+a));b=clamp((int)(b+a));}
            else if(type==1){float f=(259*(a+255))/(255*(259-a));r=clamp((int)(f*(r-128)+128));g=clamp((int)(f*(g-128)+128));b=clamp((int)(f*(b-128)+128));}
            else if(type==2){int y=(int)(.299*r+.587*g+.114*b);r=g=b=y;}
            else if(type==3){r=255-r;g=255-g;b=255-b;}
            else if(type==4){r=clamp((int)(.393*r+.769*g+.189*b));g=clamp((int)(.349*r+.686*g+.168*b));b=clamp((int)(.272*r+.534*g+.131*b));}
            px[i]=Color.argb(al,r,g,b);
        }
        out.setPixels(px,0,w,0,0,w,h);return out;
    }
    private static int clamp(int x){return Math.max(0,Math.min(255,x));}
}