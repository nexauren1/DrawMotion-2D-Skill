package com.nexauren.drawmotion;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.*;

public class EditorCanvasView extends View {
    public static final int BRUSH=0,LINE=1,RECT=2,ELLIPSE=3,FILL=4,PICK=5,ERASE=6,MOVE=7;
    public static final int RULER_OFF=0,RULER_STRAIGHT=1,RULER_GRID=2,RULER_PERSPECTIVE=3,RULER_RADIAL=4;
    public static final String[] BRUSHES={"Ink","Pencil","Marker","Airbrush","Neon","Pixel","Chalk","Oil","Watercolor","Calligraphy","G-Pen","Glow"};
    public static final String[] RULERS={"Off","Straight","Grid","Perspective","Radial"};

    public static class FrameModel{
        public final ArrayList<LayerModel> layers=new ArrayList<>();
        FrameModel copy(){FrameModel f=new FrameModel();for(LayerModel l:layers)f.layers.add(l.copy());return f;}
    }

    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final Paint bitmapPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final ArrayList<FrameModel> frames=new ArrayList<>();
    private final ArrayDeque<Bitmap> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private int W=720,H=720,frame=0,layer=1,tool=BRUSH,brush=0,ruler=RULER_OFF,sym=0,fps=12;
    private int color=Color.rgb(31,41,55); private float size=18,sx,sy,lx,ly,mx,my,scale=1;
    private boolean animation=false,onion=true,grid=false,playing=false;
    private String name="Untitled"; private Runnable task; private final RectF rect=new RectF();

    public EditorCanvasView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);ensure();}
    public void setAnimationMode(boolean v){animation=v;invalidate();} public boolean isAnimationMode(){return animation;}
    public void setOnionSkin(boolean v){onion=v;invalidate();} public void setGrid(boolean v){grid=v;invalidate();}
    public void setRuler(int v){ruler=Math.max(0,Math.min(4,v));invalidate();} public int getRuler(){return ruler;}
    public void setSymmetry(int v){sym=Math.max(0,Math.min(4,v));invalidate();}
    public void setBrushColor(int v){color=v;tool=BRUSH;} public void setBrushSize(float v){size=Math.max(1,Math.min(120,v));} public float getBrushSize(){return size;}
    public void setBrushPreset(int v){brush=Math.max(0,Math.min(BRUSHES.length-1,v));tool=BRUSH;invalidate();}
    public void setTool(int v){tool=v;invalidate();} public int getFrameCount(){return frames.size();} public int getCurrentFrame(){return frame;} public boolean isPlaying(){return playing;}
    public void setFps(int v){fps=Math.max(1,Math.min(30,v));} public int getFps(){return fps;} public void setProjectName(String v){if(v!=null&&!v.trim().isEmpty())name=v.trim();} public String getProjectName(){return name;}
    public List<FrameModel> getFrames(){return frames;}

    private Bitmap blank(){return Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);}
    private Bitmap white(){Bitmap b=blank();b.eraseColor(Color.WHITE);return b;}
    private void ensure(){if(frames.isEmpty()){FrameModel f=new FrameModel();LayerModel bg=new LayerModel("Background",white());bg.locked=true;f.layers.add(bg);f.layers.add(new LayerModel("Artwork",blank()));frames.add(f);}frame=Math.min(frame,frames.size()-1);layer=Math.min(layer,frames.get(frame).layers.size()-1);}
    private void rec(Bitmap b){if(b!=null&&!b.isRecycled())b.recycle();}
    private void hist(){while(!undo.isEmpty())rec(undo.pop());while(!redo.isEmpty())rec(redo.pop());}
    private void snap(){LayerModel l=frames.get(frame).layers.get(layer);if(l.locked)return;undo.push(l.bitmap.copy(Bitmap.Config.ARGB_8888,true));while(undo.size()>20)rec(undo.removeLast());while(!redo.isEmpty())rec(redo.pop());}
    public void undo(){if(undo.isEmpty())return;LayerModel l=frames.get(frame).layers.get(layer);redo.push(l.bitmap.copy(Bitmap.Config.ARGB_8888,true));rec(l.bitmap);l.bitmap=undo.pop();invalidate();}
    public void redo(){if(redo.isEmpty())return;LayerModel l=frames.get(frame).layers.get(layer);undo.push(l.bitmap.copy(Bitmap.Config.ARGB_8888,true));rec(l.bitmap);l.bitmap=redo.pop();invalidate();}

    public void addFrame(){ensure();FrameModel src=frames.get(frame),f=new FrameModel();for(LayerModel l:src.layers){LayerModel n=new LayerModel(l.name,blank());n.visible=l.visible;n.opacity=l.opacity;n.locked=l.locked;n.blendMode=l.blendMode;f.layers.add(n);}frames.add(++frame,f);layer=Math.min(layer,f.layers.size()-1);hist();invalidate();}
    public void duplicateFrame(){ensure();frames.add(++frame,frames.get(frame-1).copy());hist();invalidate();}
    public void deleteFrame(){if(!animation||frames.size()<2)return;FrameModel f=frames.remove(frame);for(LayerModel l:f.layers)rec(l.bitmap);frame=Math.min(frame,frames.size()-1);hist();invalidate();}
    public void selectFrame(int i){if(i>=0&&i<frames.size()){frame=i;layer=Math.min(layer,frames.get(i).layers.size()-1);hist();invalidate();}}
    public String[] layerNames(){ensure();String[] a=new String[frames.get(frame).layers.size()];for(int i=0;i<a.length;i++){LayerModel l=frames.get(frame).layers.get(i);a[i]=(i==layer?"● ":"")+l.name+(l.locked?" 🔒":"");}return a;}
    public void selectLayer(int i){ensure();if(i>=0&&i<frames.get(frame).layers.size()){layer=i;hist();invalidate();}}
    public void addLayer(){ensure();frames.get(frame).layers.add(layer+1,new LayerModel("Layer "+(frames.get(frame).layers.size()+1),blank()));layer++;invalidate();}
    public void duplicateLayer(){ensure();LayerModel s=frames.get(frame).layers.get(layer);if(s.locked)return;LayerModel n=s.copy();n.name+=" Copy";frames.get(frame).layers.add(layer+1,n);layer++;invalidate();}
    public void deleteLayer(){ensure();if(layer<=0||frames.get(frame).layers.size()<3)return;rec(frames.get(frame).layers.remove(layer).bitmap);layer--;invalidate();}
    public void moveLayerUp(){if(layer<frames.get(frame).layers.size()-1){LayerModel x=frames.get(frame).layers.remove(layer);frames.get(frame).layers.add(++layer,x);invalidate();}}
    public void moveLayerDown(){if(layer>1){LayerModel x=frames.get(frame).layers.remove(layer);frames.get(frame).layers.add(--layer,x);invalidate();}}
    public void toggleLayer(){frames.get(frame).layers.get(layer).visible=!frames.get(frame).layers.get(layer).visible;invalidate();}
    public void setLayerOpacity(int v){frames.get(frame).layers.get(layer).opacity=Math.max(0,Math.min(255,v));invalidate();}
    public void cycleBlend(){LayerModel l=frames.get(frame).layers.get(layer);l.blendMode=(l.blendMode+1)%5;invalidate();}
    public String currentLayerName(){return frames.get(frame).layers.get(layer).name;} public String currentBlend(){return LayerModel.blendName(frames.get(frame).layers.get(layer).blendMode);}

    @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);fit();}
    private void fit(){float p=16*getResources().getDisplayMetrics().density;scale=Math.min(Math.max(1,getWidth()-2*p)/W,Math.max(1,getHeight()-2*p)/H);float cw=W*scale,ch=H*scale;rect.set((getWidth()-cw)/2,(getHeight()-ch)/2,(getWidth()+cw)/2,(getHeight()+ch)/2);}
    @Override protected void onDraw(Canvas c){ensure();fit();c.drawColor(Color.rgb(246,248,251));c.save();c.translate(rect.left,rect.top);c.scale(scale,scale);c.drawColor(Color.WHITE);if(animation&&onion&&frame>0){bitmapPaint.setAlpha(60);c.drawBitmap(frames.get(frame-1).layers.get(frames.get(frame-1).layers.size()-1).bitmap,0,0,bitmapPaint);}if(animation&&onion&&frame+1<frames.size()){bitmapPaint.setAlpha(45);c.drawBitmap(frames.get(frame+1).layers.get(frames.get(frame+1).layers.size()-1).bitmap,0,0,bitmapPaint);}bitmapPaint.setAlpha(255);drawFrame(c,frame);if(grid||ruler==RULER_GRID)guideGrid(c);if(ruler==RULER_STRAIGHT)guideStraight(c);if(ruler==RULER_PERSPECTIVE)guidePerspective(c);if(ruler==RULER_RADIAL)guideRadial(c);c.restore();}
    private void drawFrame(Canvas c,int i){for(LayerModel l:frames.get(i).layers)l.draw(c,bitmapPaint);}
    private void guideGrid(Canvas c){Paint p=new Paint(1);p.setColor(0x22334155);for(int x=0;x<W;x+=60)c.drawLine(x,0,x,H,p);for(int y=0;y<H;y+=60)c.drawLine(0,y,W,y,p);}
    private void guideStraight(Canvas c){Paint p=new Paint(1);p.setColor(0x5594A3B8);c.drawLine(0,H/2f,W,H/2f,p);}
    private void guidePerspective(Canvas c){Paint p=new Paint(1);p.setColor(0x5594A3B8);float vx=W/2f,vy=H*.38f;for(int y=80;y<H;y+=80){c.drawLine(vx,vy,0,y,p);c.drawLine(vx,vy,W,y,p);}}
    private void guideRadial(Canvas c){Paint p=new Paint(1);p.setColor(0x5594A3B8);float cx=W/2f,cy=H/2f;for(int a=0;a<360;a+=30){double r=Math.toRadians(a);c.drawLine(cx,cy,cx+(float)Math.cos(r)*W,cy+(float)Math.sin(r)*H,p);}}

    private boolean in(float x,float y){return rect.contains(x,y);}private float px(float x){return Math.max(0,Math.min(W-1,(x-rect.left)/scale));}private float py(float y){return Math.max(0,Math.min(H-1,(y-rect.top)/scale));}
    @Override public boolean onTouchEvent(MotionEvent e){if(playing||!in(e.getX(),e.getY()))return true;ensure();float x=px(e.getX()),y=py(e.getY());LayerModel l=frames.get(frame).layers.get(layer);if(l.locked)return true;
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN){sx=lx=x;sy=ly=y;mx=x;my=y;if(tool==PICK){pick((int)x,(int)y);return true;}snap();if(tool==FILL)fill((int)x,(int)y);else if(tool==BRUSH||tool==ERASE)drawPoint(new Canvas(l.bitmap),x,y);invalidate();return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE){if(tool==BRUSH||tool==ERASE){drawLine(new Canvas(l.bitmap),lx,ly,x,y);lx=x;ly=y;invalidate();}else if(tool==MOVE){moveBitmap(l.bitmap,x-mx,y-my);mx=x;my=y;invalidate();}return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_UP){if(tool==LINE||tool==RECT||tool==ELLIPSE)shape(new Canvas(l.bitmap),sx,sy,x,y);invalidate();return true;}return true;}
    private void setup(){paint.reset();paint.setAntiAlias(brush!=5);paint.setDither(true);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(brush==5?Paint.Cap.SQUARE:Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeWidth(size);if(tool==ERASE){paint.setColor(Color.TRANSPARENT);paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));return;}int a=255;switch(brush){case 1:a=205;paint.setStrokeWidth(size*.8f);break;case 2:a=145;paint.setStrokeWidth(size*1.35f);break;case 3:a=75;paint.setStrokeWidth(size*2f);paint.setMaskFilter(new BlurMaskFilter(Math.max(1,size*.7f),BlurMaskFilter.Blur.NORMAL));break;case 4:paint.setShadowLayer(size,0,0,color);break;case 6:a=180;break;case 7:a=230;paint.setStrokeWidth(size*1.7f);break;case 8:a=90;paint.setStrokeWidth(size*1.3f);break;case 9:paint.setStrokeWidth(size*.55f);break;case 10:paint.setStrokeWidth(size*.65f);break;case 11:paint.setShadowLayer(size,0,0,color);break;}paint.setColor((color&0x00FFFFFF)|(a<<24));paint.setXfermode(null);}
    private void drawPoint(Canvas c,float x,float y){setup();float r=Math.max(.5f,size/2);if(brush==5)c.drawRect(x-r,y-r,x+r,y+r,paint);else c.drawCircle(x,y,r,paint);symPoint(c,x,y);}
    private void drawLine(Canvas c,float a,float b,float x,float y){if(ruler==RULER_STRAIGHT){if(Math.abs(x-a)>Math.abs(y-b))y=b;else x=a;}setup();c.drawLine(a,b,x,y,paint);symLine(c,a,b,x,y);}
    private void shape(Canvas c,float a,float b,float x,float y){setup();RectF r=new RectF(Math.min(a,x),Math.min(b,y),Math.max(a,x),Math.max(b,y));if(tool==LINE)c.drawLine(a,b,x,y,paint);else if(tool==RECT)c.drawRect(r,paint);else c.drawOval(r,paint);symLine(c,a,b,x,y);}
    private void symPoint(Canvas c,float x,float y){float r=Math.max(.5f,size/2);if(sym==1||sym==3)c.drawCircle(W-x,y,r,paint);if(sym==2||sym==3)c.drawCircle(x,H-y,r,paint);if(sym==4)for(int a=90;a<360;a+=90){c.save();c.rotate(a,W/2f,H/2f);c.drawCircle(x,y,r,paint);c.restore();}}
    private void symLine(Canvas c,float a,float b,float x,float y){if(sym==1||sym==3)c.drawLine(W-a,b,W-x,y,paint);if(sym==2||sym==3)c.drawLine(a,H-b,x,H-y,paint);if(sym==3)c.drawLine(W-a,H-b,W-x,H-y,paint);if(sym==4)for(int q=90;q<360;q+=90){c.save();c.rotate(q,W/2f,H/2f);c.drawLine(a,b,x,y,paint);c.restore();}}
    private void pick(int x,int y){for(LayerModel l:frames.get(frame).layers)if(l.visible){int p=l.bitmap.getPixel(x,y);if(Color.alpha(p)>0){color=p;tool=BRUSH;invalidate();return;}}}
    private void fill(int sx,int sy){Bitmap b=frames.get(frame).layers.get(layer).bitmap;int[] p=new int[W*H];b.getPixels(p,0,W,0,0,W,H);int old=p[sy*W+sx];if(old==color)return;int[] s=new int[p.length];int top=0;s[top++]=sy*W+sx;while(top>0){int i=s[--top];if(p[i]!=old)continue;p[i]=color;int x=i%W,y=i/W;if(x>0&&p[i-1]==old)s[top++]=i-1;if(x+1<W&&p[i+1]==old)s[top++]=i+1;if(y>0&&p[i-W]==old)s[top++]=i-W;if(y+1<H&&p[i+W]==old)s[top++]=i+W;if(top>s.length-8)break;}b.setPixels(p,0,W,0,0,W,H);}
    private void moveBitmap(Bitmap b,float dx,float dy){Bitmap c=b.copy(Bitmap.Config.ARGB_8888,true);Canvas x=new Canvas(b);x.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);x.drawBitmap(c,dx,dy,bitmapPaint);rec(c);}
    public void addText(String s){Canvas c=new Canvas(frames.get(frame).layers.get(layer).bitmap);Paint p=new Paint(1);p.setColor(color);p.setTextSize(56);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(s,(W-p.measureText(s))/2,H*.5f,p);invalidate();}
    public void addAsset(String s){Bitmap b=AssetLibrary.create(s,420,420);Canvas c=new Canvas(frames.get(frame).layers.get(layer).bitmap);c.drawBitmap(b,(W-b.getWidth())/2f,(H-b.getHeight())/2f,bitmapPaint);rec(b);invalidate();}
    public void addTexture(String s){Bitmap b=TextureFactory.create(s,W,H,0xFFF8FAFC,0x3347556B);new Canvas(frames.get(frame).layers.get(layer).bitmap).drawBitmap(b,0,0,bitmapPaint);rec(b);invalidate();}
    public void addImportedBitmap(Bitmap b){if(b==null)return;snap();Bitmap s=Bitmap.createScaledBitmap(b,Math.min(W,b.getWidth()),Math.min(H,b.getHeight()),true);new Canvas(frames.get(frame).layers.get(layer).bitmap).drawBitmap(s,(W-s.getWidth())/2f,(H-s.getHeight())/2f,bitmapPaint);rec(s);invalidate();}
    public void applyFilter(int type,float amount){LayerModel l=frames.get(frame).layers.get(layer);snap();Bitmap o=ImageFilters.apply(l.bitmap,type,amount);rec(l.bitmap);l.bitmap=o;invalidate();}
    public void rotateLayer90(){LayerModel l=frames.get(frame).layers.get(layer);snap();Matrix m=new Matrix();m.postRotate(90,W/2f,H/2f);Bitmap o=Bitmap.createBitmap(l.bitmap,0,0,W,H,m,true);rec(l.bitmap);l.bitmap=o;invalidate();}
    public void transformHorizontal(){transform(true,false);}public void transformVertical(){transform(false,true);}private void transform(boolean hx,boolean vy){LayerModel l=frames.get(frame).layers.get(layer);snap();Matrix m=new Matrix();m.setScale(hx?-1:1,vy?-1:1,W/2f,H/2f);Bitmap o=Bitmap.createBitmap(l.bitmap,0,0,W,H,m,true);rec(l.bitmap);l.bitmap=o;invalidate();}
    public void applyTemplate(String id){stopPlayback();for(FrameModel f:frames)for(LayerModel l:f.layers)rec(l.bitmap);frames.clear();int n="blank".equals(id)?1:("walk".equals(id)||"run".equals(id)?8:6);for(int i=0;i<n;i++){FrameModel f=new FrameModel();LayerModel bg=new LayerModel("Background",white());bg.locked=true;f.layers.add(bg);LayerModel a=new LayerModel("Artwork",blank());Paint p=new Paint(1);p.setColor("impact".equals(id)?Color.rgb(239,68,68):Color.rgb(124,58,237));Canvas c=new Canvas(a.bitmap);float xx=W*(.2f+.6f*i/(float)Math.max(1,n-1));if("bounce".equals(id))c.drawCircle(xx,H*(.25f+.42f*(float)Math.sin(Math.PI*i/(n-1))),70,p);else if("spin".equals(id)){c.save();c.rotate(i*45,W/2f,H/2f);c.drawRect(W*.43f,H*.25f,W*.57f,H*.75f,p);c.restore();}else c.drawCircle(xx,H*.5f,60,p);f.layers.add(a);frames.add(f);}frame=0;layer=1;animation=n>1;invalidate();}
    public Bitmap renderFrameBitmap(int i,boolean white){Bitmap b=blank();Canvas c=new Canvas(b);if(white)c.drawColor(Color.WHITE);drawFrame(c,i);return b;}
    public ArrayList<Bitmap> renderedFrames(){ArrayList<Bitmap> r=new ArrayList<>();for(int i=0;i<frames.size();i++)r.add(renderFrameBitmap(i,true));return r;}
    public void exportFrameSequence(){for(int i=0;i<frames.size();i++){Bitmap b=renderFrameBitmap(i,true);android.content.ContentValues v=new android.content.ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,String.format(Locale.US,"%s_Frame_%03d.png",safe(name),i+1));v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");v.put(MediaStore.Images.Media.RELATIVE_PATH,android.os.Environment.DIRECTORY_PICTURES+"/DrawMotion/Frames");Uri u=getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);try(OutputStream o=getContext().getContentResolver().openOutputStream(u)){if(o!=null)b.compress(Bitmap.CompressFormat.PNG,100,o);}catch(Exception ignored){}rec(b);}toast("Frame sequence exported");}
    public boolean exportProject(Context c,Uri u){return ProjectIO.save(c,u,frames,name,fps);} public boolean importProject(Context c,Uri u){ProjectIO.Loaded p=ProjectIO.load(c,u,W,H);if(p==null)return false;for(FrameModel f:frames)for(LayerModel l:f.layers)rec(l.bitmap);frames.clear();frames.addAll(p.frames);name=p.name;fps=p.fps;frame=0;layer=1;animation=frames.size()>1;invalidate();return true;}
    public void startPlayback(){stopPlayback();playing=true;long d=1000L/Math.max(1,fps);task=new Runnable(){int i=0;public void run(){if(!playing)return;frame=i%frames.size();invalidate();i++;postDelayed(this,d);}};post(task);}public void stopPlayback(){playing=false;if(task!=null)removeCallbacks(task);task=null;}
    private String safe(String s){String x=s==null?"DrawMotion":s.replaceAll("[^a-zA-Z0-9._-]+","_");return x.isEmpty()?"DrawMotion":x;}private void toast(String s){Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();}
    @Override protected void onDetachedFromWindow(){stopPlayback();hist();for(FrameModel f:frames)for(LayerModel l:f.layers)rec(l.bitmap);frames.clear();super.onDetachedFromWindow();}
}