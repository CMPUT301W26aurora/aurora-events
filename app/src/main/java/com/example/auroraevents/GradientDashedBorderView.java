package com.example.auroraevents;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class GradientDashedBorderView extends ViewGroup {

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF borderRect = new RectF();

    public GradientDashedBorderView(Context context) {
        super(context);
        init();
    }

    public GradientDashedBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientDashedBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setPathEffect(new DashPathEffect(new float[]{20f, 12f}, 0f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // Recalculate rect and shader only when size changes
        borderRect.set(4f, 4f, w - 4f, h - 4f);

        borderPaint.setShader(new LinearGradient(
                0f, 0f,
                w, h,
                new int[]{0xFFAA66FF, 0xFF00FFAA},
                null,
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(borderRect, 40f, 40f, borderPaint);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(0, 0, r - l, b - t);
        }
    }
}
