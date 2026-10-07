package com.nexauren.drawmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class ProjectIO {
    static final class Loaded { final ArrayList<EditorCanvasView.FrameModel> frames=new ArrayList<>(); String name="Imported Project"; int fps=12; }
    private ProjectIO(){}

    static boolean save(Context c,Uri uri,java.util.List<EditorCanvasView.FrameModel> frames,String name,int fps){
        try{
            OutputStream out=c.getContentResolver().openOutputStream(uri);if(out==null)return false;ZipOutputStream z=new ZipOutputStream(out);
            JSONObject meta=new JSONObject();meta.put("format","drawmotion");meta.put("version",2);meta.put("name",name);meta.put("fps",fps);meta.put("frames",frames.size());
            JSONArray fa=new JSONArray();
            for(EditorCanvasView.FrameModel f:frames){JSONArray la=new JSONArray();for(LayerModel l:f.layers){JSONObject q=new JSONObject();q.put("name",l.name);q.put("opacity",l.opacity);q.put("visible",l.visible);q.put("locked",l.locked);q.put("blend",l.blendMode);la.put(q);}JSONObject fo=new JSONObject();fo.put("layers",la);fa.put(fo);}
            meta.put("frameData",fa);z.putNextEntry(new ZipEntry("project.json"));z.write(meta.toString().getBytes(StandardCharsets.UTF_8));z.closeEntry();
            for(int fi=0;fi<frames.size();fi++){EditorCanvasView.FrameModel f=frames.get(fi);for(int li=0;li<f.layers.size();li++){z.putNextEntry(new ZipEntry(String.format(Locale.US,"frames/f%03d/l%03d.png",fi,li)));f.layers.get(li).bitmap.compress(Bitmap.CompressFormat.PNG,100,z);z.closeEntry();}}
            z.close();return true;
        }catch(Exception e){return false;}
    }

    static Loaded load(Context c,Uri uri,int w,int h){
        try{
            InputStream input=c.getContentResolver().openInputStream(uri);if(input==null)return null;ZipInputStream z=new ZipInputStream(input);Loaded out=new Loaded();JSONObject meta=null;ArrayList<byte[]> images=new ArrayList<>();ArrayList<String> names=new ArrayList<>();ZipEntry e;
            while((e=z.getNextEntry())!=null){ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=z.read(buf))>0)b.write(buf,0,n);byte[] data=b.toByteArray();if(e.getName().equals("project.json"))meta=new JSONObject(new String(data,StandardCharsets.UTF_8));else if(e.getName().startsWith("frames/")){images.add(data);names.add(e.getName());}}
            z.close();if(meta==null)return null;out.name=meta.optString("name","Imported Project");out.fps=meta.optInt("fps",12);JSONArray fa=meta.optJSONArray("frameData");int frameCount=Math.max(1,meta.optInt("frames",1));
            while(out.frames.size()<frameCount)out.frames.add(new EditorCanvasView.FrameModel());
            if(fa!=null){for(int fi=0;fi<fa.length()&&fi<out.frames.size();fi++){JSONArray la=fa.getJSONObject(fi).optJSONArray("layers");if(la!=null)for(int li=0;li<la.length();li++){JSONObject q=la.getJSONObject(li);LayerModel lm=new LayerModel(q.optString("name","Layer "+(li+1)),Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888));lm.opacity=q.optInt("opacity",255);lm.visible=q.optBoolean("visible",true);lm.locked=q.optBoolean("locked",false);lm.blendMode=q.optInt("blend",0);out.frames.get(fi).layers.add(lm);}}}
            for(int i=0;i<images.size();i++){String n=names.get(i),p[]=n.split("/");if(p.length<3)continue;int fi=Integer.parseInt(p[1].substring(1)),li=Integer.parseInt(p[2].substring(1,4));Bitmap raw=BitmapFactory.decodeByteArray(images.get(i),0,images.get(i).length);if(raw==null)continue;if(raw.getWidth()!=w||raw.getHeight()!=h){Bitmap s=Bitmap.createScaledBitmap(raw,w,h,true);raw.recycle();raw=s;}while(out.frames.get(fi).layers.size()<=li)out.frames.get(fi).layers.add(new LayerModel("Layer "+(out.frames.get(fi).layers.size()+1),Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)));LayerModel lm=out.frames.get(fi).layers.get(li);lm.bitmap.recycle();lm.bitmap=raw;}
            return out;
        }catch(Exception e){return null;}
    }
}