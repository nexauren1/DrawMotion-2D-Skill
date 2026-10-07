package com.nexauren.drawmotion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public final class TemplateFactory {
    private TemplateFactory() {}

    public static List<Bitmap> create(String id, int w, int h) {
        ArrayList<Bitmap> frames = new ArrayList<>();
        String key = id == null ? "blank" : id;
        switch (key) {
            case "bounce":
                for (int i = 0; i < 8; i++) frames.add(ball(w, h, i));
                break;
            case "walk":
                for (int i = 0; i < 8; i++) frames.add(person(w, h, i, false, false));
                break;
            case "run":
                for (int i = 0; i < 6; i++) frames.add(person(w, h, i, true, false));
                break;
            case "jump":
                for (int i = 0; i < 7; i++) frames.add(person(w, h, i, false, true));
                break;
            case "blink":
                for (int i = 0; i < 6; i++) frames.add(face(w, h, i));
                break;
            case "wave":
                for (int i = 0; i < 6; i++) frames.add(person(w, h, i, false, false, true));
                break;
            case "impact":
                for (int i = 0; i < 6; i++) frames.add(impact(w, h, i));
                break;
            case "fall":
                for (int i = 0; i < 8; i++) frames.add(falling(w, h, i));
                break;
            case "spin":
                for (int i = 0; i < 8; i++) frames.add(spin(w, h, i));
                break;
            case "pulse":
                for (int i = 0; i < 8; i++) frames.add(pulse(w, h, i));
                break;
            default:
                frames.add(blank(w, h));
        }
        return frames;
    }

    private static Bitmap blank(int w, int h) {
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    private static Paint paint(int color, float width, Paint.Style style) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        p.setColor(color);
        p.setStrokeWidth(width);
        p.setStyle(style);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        return p;
    }

    private static Bitmap ball(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        float[] ys = {0.68f, 0.58f, 0.48f, 0.40f, 0.40f, 0.48f, 0.58f, 0.68f};
        float x = w * (0.18f + i * 0.09f);
        float y = h * ys[i];
        Paint p = paint(Color.rgb(139, 92, 246), Math.max(6, w * 0.008f), Paint.Style.FILL);
        c.drawCircle(x, y, w * 0.07f, p);
        Paint shadow = paint(0x55334155, 1, Paint.Style.FILL);
        c.drawOval(x - w * 0.10f, h * 0.82f, x + w * 0.10f, h * 0.85f, shadow);
        return b;
    }

    private static Bitmap person(int w, int h, int i, boolean running, boolean jumping) {
        return person(w, h, i, running, jumping, false);
    }

    private static Bitmap person(int w, int h, int i, boolean running, boolean jumping, boolean waving) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        float cx = w * (0.50f + (running ? (i - 2.5f) * 0.035f : 0));
        float ground = jumping ? h * (0.74f - Math.abs(3 - i) * 0.055f) : h * 0.78f;
        float head = w * 0.055f;
        Paint line = paint(Color.rgb(31, 41, 55), Math.max(8, w * 0.012f), Paint.Style.STROKE);
        Paint fill = paint(Color.rgb(34, 211, 238), 1, Paint.Style.FILL);
        c.drawCircle(cx, ground - h * 0.22f, head, fill);

        float shoulderY = ground - h * 0.15f;
        float hipY = ground + h * 0.04f;
        c.drawLine(cx, shoulderY, cx, hipY, line);

        float phase = (float) (Math.sin(i * Math.PI / 4.0) * 0.12);
        float leftLegX = cx - w * (running ? 0.16f * (0.8f + phase) : 0.10f);
        float rightLegX = cx + w * (running ? 0.16f * (0.8f - phase) : 0.10f);
        c.drawLine(cx, hipY, leftLegX, ground, line);
        c.drawLine(cx, hipY, rightLegX, ground, line);

        float armDelta = running ? -phase : phase;
        if (waving) {
            Path arm = new Path();
            arm.moveTo(cx, shoulderY);
            arm.lineTo(cx + w * 0.12f, shoulderY - h * 0.06f);
            arm.lineTo(cx + w * (0.15f + 0.03f * (i % 2)), shoulderY - h * 0.18f);
            c.drawPath(arm, line);
            c.drawLine(cx, shoulderY, cx - w * 0.12f, shoulderY + h * 0.07f, line);
        } else {
            c.drawLine(cx, shoulderY, cx - w * (0.12f + armDelta), shoulderY + h * 0.10f, line);
            c.drawLine(cx, shoulderY, cx + w * (0.12f - armDelta), shoulderY + h * 0.10f, line);
        }

        Paint accent = paint(Color.rgb(139, 92, 246), 1, Paint.Style.FILL);
        c.drawCircle(cx + head * 0.35f, ground - h * 0.23f, head * 0.16f, accent);
        return b;
    }

    private static Bitmap face(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        float cx = w * 0.5f;
        float cy = h * 0.48f;
        Paint face = paint(Color.rgb(255, 213, 165), 1, Paint.Style.FILL);
        Paint line = paint(Color.rgb(31, 41, 55), Math.max(7, w * 0.010f), Paint.Style.STROKE);
        c.drawCircle(cx, cy, w * 0.18f, face);
        boolean closed = i == 2 || i == 3;
        if (closed) {
            c.drawLine(cx - w * 0.09f, cy - h * 0.02f, cx - w * 0.03f, cy - h * 0.02f, line);
            c.drawLine(cx + w * 0.03f, cy - h * 0.02f, cx + w * 0.09f, cy - h * 0.02f, line);
        } else {
            Paint dot = paint(Color.rgb(31, 41, 55), 1, Paint.Style.FILL);
            c.drawCircle(cx - w * 0.06f, cy - h * 0.02f, w * 0.014f, dot);
            c.drawCircle(cx + w * 0.06f, cy - h * 0.02f, w * 0.014f, dot);
        }
        c.drawArc(cx - w * 0.055f, cy + h * 0.04f, cx + w * 0.055f, cy + h * 0.12f, 0, 180, false, line);
        return b;
    }

    private static Bitmap impact(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        float cx = w * 0.5f, cy = h * 0.5f;
        Paint p = paint(Color.rgb(139, 92, 246), Math.max(7, w * 0.012f), Paint.Style.STROKE);
        float r = w * (0.035f + i * 0.035f);
        c.drawCircle(cx, cy, r, p);
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4.0;
            float inner = r * 1.4f;
            float outer = r * (2.1f + i * 0.15f);
            c.drawLine(cx + (float)Math.cos(a) * inner, cy + (float)Math.sin(a) * inner,
                    cx + (float)Math.cos(a) * outer, cy + (float)Math.sin(a) * outer, p);
        }
        return b;
    }

    private static Bitmap falling(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        Paint p = paint(Color.rgb(34, 211, 238), 1, Paint.Style.FILL);
        float y = h * (0.16f + i * 0.08f);
        c.drawRoundRect(w * 0.42f, y, w * 0.58f, y + h * 0.10f, 22, 22, p);
        Paint trail = paint(0x664E5D78, Math.max(5, w * 0.009f), Paint.Style.STROKE);
        for (int t = 1; t < 4; t++) {
            float ty = y - t * h * 0.045f;
            c.drawLine(w * 0.5f, ty, w * 0.5f, ty - h * 0.02f, trail);
        }
        return b;
    }

    private static Bitmap spin(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        c.save();
        c.rotate(i * 45f, w / 2f, h / 2f);
        Paint p = paint(Color.rgb(139, 92, 246), 1, Paint.Style.FILL);
        Path star = new Path();
        for (int k = 0; k < 10; k++) {
            double a = -Math.PI / 2 + k * Math.PI / 5;
            float r = (k % 2 == 0) ? w * 0.13f : w * 0.055f;
            float x = w / 2f + (float)Math.cos(a) * r;
            float y = h / 2f + (float)Math.sin(a) * r;
            if (k == 0) star.moveTo(x, y); else star.lineTo(x, y);
        }
        star.close();
        c.drawPath(star, p);
        c.restore();
        return b;
    }

    private static Bitmap pulse(int w, int h, int i) {
        Bitmap b = blank(w, h);
        Canvas c = new Canvas(b);
        float phase = i <= 3 ? i : 7 - i;
        Paint p = paint(0xAA22D3EE, Math.max(6, w * 0.010f), Paint.Style.STROKE);
        c.drawCircle(w / 2f, h / 2f, w * (0.06f + phase * 0.025f), p);
        Paint dot = paint(Color.rgb(139, 92, 246), 1, Paint.Style.FILL);
        c.drawCircle(w / 2f, h / 2f, w * 0.035f, dot);
        return b;
    }
}
