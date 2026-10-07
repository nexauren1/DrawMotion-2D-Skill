package com.nexauren.drawmotion;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;

public class DrawCanvasView extends View {
    public interface PlaybackCallback {
        void onFinished();
    }

    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final ArrayList<Bitmap> frames = new ArrayList<>();
    private final ArrayDeque<Bitmap> undoStack = new ArrayDeque<>();
    private final ArrayDeque<Bitmap> redoStack = new ArrayDeque<>();

    private int currentFrame = 0;
    private int brushColor = Color.WHITE;
    private float brushSize = 18f;
    private boolean eraser = false;
    private boolean animationMode = false;
    private boolean onionSkin = true;
    private boolean playing = false;
    private float lastX;
    private float lastY;
    private final android.os.Handler handler = new android.os.Handler();
    private Runnable playbackTask;

    public DrawCanvasView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        setFocusable(true);
    }

    public void setAnimationMode(boolean value) { animationMode = value; }
    public boolean isOnionSkin() { return onionSkin; }

    public void setOnionSkin(boolean enabled) {
        onionSkin = enabled;
        invalidate();
    }

    public void setBrushColor(int color) {
        brushColor = color;
        eraser = false;
    }

    public void setBrushSize(float size) {
        brushSize = Math.max(1f, size);
    }

    public float getBrushSize() { return brushSize; }
    public void setEraser(boolean value) { eraser = value; }
    public int getFrameCount() { return frames.size(); }
    public int getCurrentFrame() { return currentFrame; }
    public boolean isPlaying() { return playing; }

    public void addFrame() {
        ensureFrame();
        Bitmap source = frames.get(currentFrame);
        Bitmap next = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        frames.add(next);
        currentFrame = frames.size() - 1;
        clearHistory();
        invalidate();
    }

    public void duplicateFrame() {
        ensureFrame();
        frames.add(frames.get(currentFrame).copy(Bitmap.Config.ARGB_8888, true));
        currentFrame = frames.size() - 1;
        clearHistory();
        invalidate();
    }

    public void deleteFrame() {
        if (!animationMode || frames.size() <= 1) return;
        Bitmap removed = frames.remove(currentFrame);
        if (!removed.isRecycled()) removed.recycle();
        currentFrame = Math.min(currentFrame, frames.size() - 1);
        clearHistory();
        invalidate();
    }

    public void selectFrame(int index) {
        if (index < 0 || index >= frames.size()) return;
        currentFrame = index;
        clearHistory();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && frames.isEmpty()) {
            frames.add(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888));
        }
    }

    private void ensureFrame() {
        if (frames.isEmpty() && getWidth() > 0 && getHeight() > 0) {
            frames.add(Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888));
            currentFrame = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCheckerboard(canvas);
        ensureFrame();
        if (frames.isEmpty()) return;

        if (animationMode && onionSkin && currentFrame > 0) {
            bitmapPaint.setAlpha(70);
            canvas.drawBitmap(frames.get(currentFrame - 1), 0, 0, bitmapPaint);
        }

        bitmapPaint.setAlpha(255);
        canvas.drawBitmap(frames.get(currentFrame), 0, 0, bitmapPaint);
    }

    private void drawCheckerboard(Canvas canvas) {
        int size = 32;
        Paint p = new Paint();
        for (int y = 0; y < getHeight(); y += size) {
            for (int x = 0; x < getWidth(); x += size) {
                p.setColor(((x / size + y / size) & 1) == 0
                        ? Color.rgb(32, 36, 46)
                        : Color.rgb(24, 28, 36));
                canvas.drawRect(x, y, x + size, y + size, p);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (playing) return true;
        ensureFrame();
        if (frames.isEmpty()) return true;

        Canvas target = new Canvas(frames.get(currentFrame));

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                snapshotForUndo();
                lastX = event.getX();
                lastY = event.getY();
                drawPoint(target, lastX, lastY);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                drawLine(target, lastX, lastY, x, y);
                lastX = x;
                lastY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                invalidate();
                return true;
            default:
                return true;
        }
    }

    private void drawPoint(Canvas canvas, float x, float y) {
        configureStroke();
        canvas.drawCircle(x, y, Math.max(0.5f, brushSize / 2f), strokePaint);
    }

    private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2) {
        configureStroke();
        canvas.drawLine(x1, y1, x2, y2, strokePaint);
    }

    private void configureStroke() {
        strokePaint.setStrokeWidth(brushSize);
        if (eraser) {
            strokePaint.setColor(Color.TRANSPARENT);
            strokePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            strokePaint.setColor(brushColor);
            strokePaint.setXfermode(null);
        }
    }

    private void snapshotForUndo() {
        ensureFrame();
        Bitmap snapshot = frames.get(currentFrame).copy(Bitmap.Config.ARGB_8888, true);
        undoStack.push(snapshot);
        while (undoStack.size() > 5) {
            Bitmap old = undoStack.removeLast();
            old.recycle();
        }
        while (!redoStack.isEmpty()) redoStack.pop().recycle();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(frames.get(currentFrame).copy(Bitmap.Config.ARGB_8888, true));
        frames.set(currentFrame, undoStack.pop());
        invalidate();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.push(frames.get(currentFrame).copy(Bitmap.Config.ARGB_8888, true));
        frames.set(currentFrame, redoStack.pop());
        invalidate();
    }

    private void clearHistory() {
        while (!undoStack.isEmpty()) undoStack.pop().recycle();
        while (!redoStack.isEmpty()) redoStack.pop().recycle();
    }

    public void startPlayback(int fps, PlaybackCallback callback) {
        if (!animationMode || frames.isEmpty()) return;
        stopPlayback();
        playing = true;
        long delay = Math.max(33, 1000L / Math.max(1, fps));

        playbackTask = new Runnable() {
            int frame = 0;
            @Override
            public void run() {
                if (!playing) {
                    callback.onFinished();
                    return;
                }
                currentFrame = frame;
                invalidate();
                frame = (frame + 1) % frames.size();
                handler.postDelayed(this, delay);
            }
        };
        handler.post(playbackTask);
    }

    public void stopPlayback() {
        playing = false;
        if (playbackTask != null) handler.removeCallbacks(playbackTask);
        playbackTask = null;
    }

    public void exportCurrentPng() {
        ensureFrame();
        if (frames.isEmpty()) return;
        String name = String.format(Locale.US, "DrawMotion_%d.png", System.currentTimeMillis());
        Uri uri = saveImage(frames.get(currentFrame), name, "DrawMotion");
        notifySaved(uri != null ? "PNG exported to Pictures/DrawMotion" : "Export failed");
    }

    public void exportSpriteSheet() {
        if (!animationMode || frames.isEmpty()) return;

        int count = frames.size();
        int columns = Math.min(4, count);
        int rows = (int) Math.ceil(count / (double) columns);
        int fw = frames.get(0).getWidth();
        int fh = frames.get(0).getHeight();

        Bitmap sheet;
        try {
            sheet = Bitmap.createBitmap(fw * columns, fh * rows, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            notifySaved("Sprite sheet is too large for this device");
            return;
        }

        Canvas c = new Canvas(sheet);
        for (int i = 0; i < count; i++) {
            int x = (i % columns) * fw;
            int y = (i / columns) * fh;
            c.drawBitmap(frames.get(i), x, y, bitmapPaint);
        }

        String name = String.format(Locale.US, "DrawMotion_SpriteSheet_%d.png", System.currentTimeMillis());
        Uri uri = saveImage(sheet, name, "DrawMotion");
        sheet.recycle();
        notifySaved(uri != null ? "Sprite sheet exported to Pictures/DrawMotion" : "Export failed");
    }

    public void exportFrameSequence() {
        if (!animationMode || frames.isEmpty()) return;

        int saved = 0;
        for (int i = 0; i < frames.size(); i++) {
            String name = String.format(Locale.US, "DrawMotion_Frame_%03d_%d.png",
                    i + 1, System.currentTimeMillis());
            if (saveImage(frames.get(i), name, "DrawMotion/Frames") != null) saved++;
        }
        notifySaved(saved + " frames exported");
    }

    private Uri saveImage(Bitmap bitmap, String displayName, String subfolder) {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/" + subfolder);

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return null;

        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new Exception("No output stream");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return uri;
        } catch (Exception e) {
            resolver.delete(uri, null, null);
            return null;
        }
    }

    private void notifySaved(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPlayback();
        clearHistory();
        for (Bitmap b : frames) {
            if (b != null && !b.isRecycled()) b.recycle();
        }
        frames.clear();
        super.onDetachedFromWindow();
    }
}
