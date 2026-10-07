package com.nexauren.drawmotion;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private DrawCanvasView canvas;
    private LinearLayout timeline;
    private boolean animationMode;
    private Button onionButton;
    private Button playButton;
    private int fps = 12;

    private final int BG = Color.rgb(11, 13, 18);
    private final int PANEL = Color.rgb(20, 23, 31);
    private final int PANEL_2 = Color.rgb(27, 31, 42);
    private final int TEXT = Color.rgb(248, 250, 252);
    private final int MUTED = Color.rgb(148, 163, 184);
    private final int PURPLE = Color.rgb(139, 92, 246);
    private final int CYAN = Color.rgb(34, 211, 238);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(Color.rgb(8, 10, 15));
        showHome();
    }

    private void showHome() {
        animationMode = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(24), dp(22), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

        TextView brand = text("DRAWMOTION", 13, PURPLE, Typeface.BOLD);
        brand.setLetterSpacing(0.18f);
        content.addView(brand, lp(-1, -2, 0, 0, 0, 8));

        TextView title = text("2D Skill", 38, TEXT, Typeface.BOLD);
        content.addView(title, lp(-1, -2, 0, 0, 0, 2));

        TextView tagline = text("Draw. Animate. Create.", 18, CYAN, Typeface.BOLD);
        content.addView(tagline, lp(-1, -2, 0, 0, 0, 26));

        content.addView(homeCard("New Drawing", "Create illustrations and game-ready artwork.",
                v -> startEditor(false)), lp(-1, -2, 0, 0, 0, 12));
        content.addView(homeCard("New Animation", "Draw frame by frame with onion skin and FPS control.",
                v -> startEditor(true)), lp(-1, -2, 0, 0, 0, 12));

        TextView gameTitle = text("GAME ASSETS", 12, MUTED, Typeface.BOLD);
        gameTitle.setLetterSpacing(0.16f);
        content.addView(gameTitle, lp(-1, -2, 0, 18, 0, 8));

        TextView gameInfo = text(
                "Export transparent PNG artwork, sprite sheets and frame sequences for 2D games.",
                15, TEXT, Typeface.NORMAL
        );
        gameInfo.setBackground(round(PANEL, 18));
        gameInfo.setPadding(dp(18), dp(18), dp(18), dp(18));
        content.addView(gameInfo, lp(-1, -2, 0, 0, 0, 18));

        TextView free = text("Free at launch • No premium tier", 13, MUTED, Typeface.NORMAL);
        content.addView(free, lp(-1, -2, 0, 4, 0, 0));

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
    }

    private LinearLayout homeCard(String title, String subtitle, View.OnClickListener listener) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(17), dp(18), dp(17));
        box.setBackground(round(PANEL, 20));
        box.setOnClickListener(listener);

        TextView t = text(title, 20, TEXT, Typeface.BOLD);
        TextView s = text(subtitle, 14, MUTED, Typeface.NORMAL);
        box.addView(t);
        box.addView(s, lp(-1, -2, 0, 5, 0, 0));
        return box;
    }

    private void startEditor(boolean animated) {
        animationMode = animated;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(8), dp(8), dp(6));

        Button back = toolButton("‹");
        back.setOnClickListener(v -> {
            if (canvas != null) canvas.stopPlayback();
            showHome();
        });
        top.addView(back, lp(dp(44), dp(44), 0, 0, 4, 0));

        TextView title = text(animated ? "Animation" : "Drawing", 18, TEXT, Typeface.BOLD);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1f));

        Button undo = toolButton("Undo");
        undo.setOnClickListener(v -> canvas.undo());
        top.addView(undo, lp(-2, dp(44), 4, 0, 2, 0));

        Button redo = toolButton("Redo");
        redo.setOnClickListener(v -> canvas.redo());
        top.addView(redo, lp(-2, dp(44), 2, 0, 2, 0));

        Button export = toolButton("Export");
        export.setOnClickListener(v -> showExportDialog());
        top.addView(export, lp(-2, dp(44), 2, 0, 0, 0));

        root.addView(top, new LinearLayout.LayoutParams(-1, dp(58)));

        canvas = new DrawCanvasView(this);
        canvas.setAnimationMode(animated);
        root.addView(canvas, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout tools = new LinearLayout(this);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.setPadding(dp(8), dp(7), dp(8), dp(7));
        tools.setBackgroundColor(PANEL);

        Button brush = toolButton("Brush");
        brush.setOnClickListener(v -> {
            canvas.setEraser(false);
            brush.setText("Brush ✓");
        });
        tools.addView(brush, equalLp());

        Button eraser = toolButton("Erase");
        eraser.setOnClickListener(v -> {
            canvas.setEraser(true);
            brush.setText("Brush");
        });
        tools.addView(eraser, equalLp());

        Button color = toolButton("Color");
        color.setOnClickListener(v -> showColorDialog());
        tools.addView(color, equalLp());

        Button size = toolButton("Size");
        size.setOnClickListener(v -> showSizeDialog());
        tools.addView(size, equalLp());

        if (animated) {
            onionButton = toolButton("Onion ✓");
            onionButton.setOnClickListener(v -> {
                canvas.setOnionSkin(!canvas.isOnionSkin());
                onionButton.setText(canvas.isOnionSkin() ? "Onion ✓" : "Onion");
            });
            tools.addView(onionButton, equalLp());
        }

        root.addView(tools, new LinearLayout.LayoutParams(-1, dp(55)));

        if (animated) buildTimeline();

        setContentView(root);
        canvas.post(this::refreshTimeline);
    }

    private void buildTimeline() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.rgb(14, 17, 24));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(dp(8), dp(6), dp(8), dp(4));

        Button add = toolButton("+ Frame");
        add.setOnClickListener(v -> { canvas.addFrame(); refreshTimeline(); });
        controls.addView(add, equalLp());

        Button duplicate = toolButton("Duplicate");
        duplicate.setOnClickListener(v -> { canvas.duplicateFrame(); refreshTimeline(); });
        controls.addView(duplicate, equalLp());

        Button delete = toolButton("Delete");
        delete.setOnClickListener(v -> { canvas.deleteFrame(); refreshTimeline(); });
        controls.addView(delete, equalLp());

        playButton = toolButton("Play ▶");
        playButton.setOnClickListener(v -> {
            if (canvas.isPlaying()) {
                canvas.stopPlayback();
                playButton.setText("Play ▶");
            } else {
                canvas.startPlayback(fps, () -> playButton.setText("Play ▶"));
                playButton.setText("Stop ■");
            }
        });
        controls.addView(playButton, equalLp());

        Button fpsButton = toolButton("FPS " + fps);
        fpsButton.setOnClickListener(v -> showFpsDialog(fpsButton));
        controls.addView(fpsButton, equalLp());

        panel.addView(controls);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        timeline = new LinearLayout(this);
        timeline.setPadding(dp(8), dp(3), dp(8), dp(8));
        timeline.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(timeline, new HorizontalScrollView.LayoutParams(-2, dp(54)));
        panel.addView(scroll);

        root.addView(panel, new LinearLayout.LayoutParams(-1, dp(112)));
    }

    private void refreshTimeline() {
        if (timeline == null || canvas == null) return;
        timeline.removeAllViews();
        for (int i = 0; i < canvas.getFrameCount(); i++) {
            final int index = i;
            TextView frame = text(String.format(Locale.US, "%02d", i + 1), 13,
                    i == canvas.getCurrentFrame() ? Color.WHITE : MUTED, Typeface.BOLD);
            frame.setGravity(Gravity.CENTER);
            frame.setBackground(round(i == canvas.getCurrentFrame() ? PURPLE : PANEL_2, 12));
            frame.setOnClickListener(v -> { canvas.selectFrame(index); refreshTimeline(); });
            timeline.addView(frame, lp(dp(48), dp(42), 4, 0, 4, 0));
        }
    }

    private void showColorDialog() {
        final int[] colors = {
                Color.WHITE, Color.BLACK, Color.RED, Color.rgb(255, 120, 0),
                Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE,
                Color.rgb(139, 92, 246), Color.MAGENTA, Color.rgb(255, 105, 180)
        };
        LinearLayout grid = new LinearLayout(this);
        grid.setPadding(dp(14), dp(14), dp(14), dp(14));
        grid.setOrientation(LinearLayout.HORIZONTAL);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Brush color")
                .setView(grid)
                .create();

        for (int c : colors) {
            TextView swatch = new TextView(this);
            swatch.setBackground(round(c, 100));
            swatch.setOnClickListener(v -> { canvas.setBrushColor(c); dialog.dismiss(); });
            grid.addView(swatch, lp(dp(42), dp(42), 4, 4, 4, 4));
        }
        dialog.show();
    }

    private void showSizeDialog() {
        final SeekBar seek = new SeekBar(this);
        seek.setMax(72);
        seek.setProgress(Math.max(0, Math.min(72, (int) canvas.getBrushSize() - 1)));
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(18), dp(4), dp(18), dp(8));
        wrap.addView(seek, new LinearLayout.LayoutParams(-1, dp(48)));

        new AlertDialog.Builder(this)
                .setTitle("Brush size")
                .setView(wrap)
                .setMessage("1–73 px")
                .setPositiveButton("Apply", (d, w) -> canvas.setBrushSize(seek.getProgress() + 1))
                .show();
    }

    private void showFpsDialog(Button button) {
        final SeekBar seek = new SeekBar(this);
        seek.setMax(29);
        seek.setProgress(fps - 1);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(18), 0, dp(18), 0);
        wrap.addView(seek, new LinearLayout.LayoutParams(-1, dp(48)));

        new AlertDialog.Builder(this)
                .setTitle("Animation FPS")
                .setView(wrap)
                .setMessage("1–30 FPS")
                .setPositiveButton("Apply", (d, w) -> {
                    fps = seek.getProgress() + 1;
                    button.setText("FPS " + fps);
                })
                .show();
    }

    private void showExportDialog() {
        String[] options = animationMode
                ? new String[]{"Current frame PNG", "Sprite sheet PNG", "PNG frame sequence"}
                : new String[]{"Current drawing PNG"};

        new AlertDialog.Builder(this)
                .setTitle("Export")
                .setItems(options, (d, which) -> {
                    if (which == 0) canvas.exportCurrentPng();
                    else if (which == 1) canvas.exportSpriteSheet();
                    else canvas.exportFrameSequence();
                })
                .show();
    }

    private Button toolButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(TEXT);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setPadding(dp(5), 0, dp(5), 0);
        b.setBackground(round(PANEL_2, 12));
        return b;
    }

    private TextView text(String value, float size, int color, int style) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        return t;
    }

    private GradientDrawable round(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    private LinearLayout.LayoutParams equalLp() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(42), 1f);
        p.setMargins(dp(3), 0, dp(3), 0);
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
