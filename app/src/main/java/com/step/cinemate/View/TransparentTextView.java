package com.step.cinemate.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class TransparentTextView extends AppCompatTextView {

    private Paint textPaint;

    public TransparentTextView(Context context) {
        super(context);
        init();
    }

    public TransparentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransparentTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.set(getPaint());
        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Заливка белым цветом
        canvas.drawColor(0xFFFFFFFF);

        // Вызов оригинального текста, чтобы он вырезался
        super.onDraw(canvas);

        // Вырезание текста
        canvas.drawText(getText().toString(), getPaddingLeft(), getBaseline(), textPaint);
    }
}
