package com.nexauren.drawmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.Surface;
import java.util.List;

public final class VideoExporter {
    private VideoExporter(){}

    public static boolean export(Context context,Uri uri,List<Bitmap> frames,int fps){
        if(frames==null||frames.isEmpty()||uri==null)return false;
        MediaRecorder recorder=null;ParcelFileDescriptor pfd=null;Surface surface=null;
        try{
            Bitmap first=frames.get(0);pfd=context.getContentResolver().openFileDescriptor(uri,"w");if(pfd==null)return false;
            recorder=new MediaRecorder();recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoSize(Math.max(2,first.getWidth()&~1),Math.max(2,first.getHeight()&~1));
            recorder.setVideoFrameRate(Math.max(1,Math.min(30,fps)));recorder.setVideoEncodingBitRate(Math.max(1000000,first.getWidth()*first.getHeight()*5));
            recorder.setOutputFile(pfd.getFileDescriptor());recorder.prepare();recorder.start();surface=recorder.getSurface();
            long delay=1000L/Math.max(1,Math.min(30,fps));
            for(Bitmap frame:frames){CanvasCompat.draw(surface,frame);try{Thread.sleep(delay);}catch(InterruptedException e){Thread.currentThread().interrupt();break;}}
            recorder.stop();return true;
        }catch(Exception e){try{if(recorder!=null)recorder.reset();}catch(Exception ignored){}return false;}
        finally{if(surface!=null)surface.release();if(recorder!=null)recorder.release();if(pfd!=null)try{pfd.close();}catch(Exception ignored){}}
    }

    static final class CanvasCompat {
        static void draw(Surface surface,Bitmap frame){
            android.graphics.Canvas c=surface.lockCanvas(null);
            try{c.drawColor(android.graphics.Color.WHITE);c.drawBitmap(frame,null,new android.graphics.RectF(0,0,c.getWidth(),c.getHeight()),null);}
            finally{surface.unlockCanvasAndPost(c);}
        }
    }
}