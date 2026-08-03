package com.example.mycalendar2026sar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageEditorActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private DrawingView drawingView;
    private Bitmap baseBitmap;
    private Uri sourceUri;
    private boolean isDrawingMode = false;
    private boolean isCropMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        imagePreview = findViewById(R.id.imagePreview);
        drawingView = new DrawingView(this);
        ((android.widget.FrameLayout) findViewById(R.id.drawingOverlay).getParent()).addView(drawingView);
        findViewById(R.id.drawingOverlay).setVisibility(View.GONE);

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            sourceUri = Uri.parse(uriString);
            loadBitmap(sourceUri);
        }

        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        findViewById(R.id.btnCrop).setOnClickListener(v -> {
            isCropMode = !isCropMode;
            isDrawingMode = false;
            Toast.makeText(this, isCropMode ? "Crop Mode: Drag on image" : "Crop Mode Off", Toast.LENGTH_SHORT).show();
            drawingView.invalidate();
        });

        findViewById(R.id.btnDraw).setOnClickListener(v -> {
            isDrawingMode = !isDrawingMode;
            isCropMode = false;
            Toast.makeText(this, isDrawingMode ? "Draw Mode: On" : "Draw Mode Off", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnText).setOnClickListener(v -> showTextDialog());

        findViewById(R.id.btnOk).setOnClickListener(v -> saveResult());
    }

    private void loadBitmap(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            baseBitmap = BitmapFactory.decodeStream(is);
            imagePreview.setImageBitmap(baseBitmap);
            if (is != null) is.close();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Text");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                drawingView.addText(text);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveResult() {
        try {
            Bitmap result = drawingView.getResultBitmap();
            File file = new File(getFilesDir(), "edited_secure_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            getIntent().putExtra("resultPath", file.getAbsolutePath());
            setResult(RESULT_OK, getIntent());
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private class DrawingView extends View {
        private final Paint paint;
        private final Path path;
        private Canvas canvas;
        private Bitmap drawBitmap;
        private RectF cropRect;

        public DrawingView(Context context) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5f);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            path = new Path();
        }

        public void addText(String text) {
            if (drawBitmap == null) return;
            Canvas c = new Canvas(drawBitmap);
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(50f);
            c.drawText(text, 100, 100, textPaint);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(drawBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (baseBitmap != null) {
                Rect destRect = getBitmapRect();
                canvas.drawBitmap(baseBitmap, null, destRect, null);
            }
            canvas.drawBitmap(drawBitmap, 0, 0, null);
            if (isCropMode && cropRect != null) {
                Paint cropPaint = new Paint();
                cropPaint.setColor(Color.WHITE);
                cropPaint.setStyle(Paint.Style.STROKE);
                cropPaint.setStrokeWidth(3f);
                canvas.drawRect(cropRect, cropPaint);
            }
        }

        private Rect getBitmapRect() {
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            float bitmapWidth = baseBitmap.getWidth();
            float bitmapHeight = baseBitmap.getHeight();

            float scale = Math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight);
            float dx = (viewWidth - bitmapWidth * scale) / 2;
            float dy = (viewHeight - bitmapHeight * scale) / 2;

            return new Rect((int) dx, (int) dy, (int) (dx + bitmapWidth * scale), (int) (dy + bitmapHeight * scale));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (isDrawingMode) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        path.moveTo(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        path.lineTo(x, y);
                        canvas.drawPath(path, paint);
                        break;
                }
                invalidate();
                return true;
            } else if (isCropMode) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cropRect = new RectF(x, y, x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cropRect.right = x;
                        cropRect.bottom = y;
                        break;
                }
                invalidate();
                return true;
            }
            return super.onTouchEvent(event);
        }

        public Bitmap getResultBitmap() {
            if (isCropMode && cropRect != null) {
                Rect bRect = getBitmapRect();
                float scale = (float) baseBitmap.getWidth() / bRect.width();
                
                int left = (int) ((Math.max(cropRect.left, bRect.left) - bRect.left) * scale);
                int top = (int) ((Math.max(cropRect.top, bRect.top) - bRect.top) * scale);
                int right = (int) ((Math.min(cropRect.right, bRect.right) - bRect.left) * scale);
                int bottom = (int) ((Math.min(cropRect.bottom, bRect.bottom) - bRect.top) * scale);

                if (right > left && bottom > top) {
                   return Bitmap.createBitmap(baseBitmap, left, top, right - left, bottom - top);
                }
            }
            
            Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(result);
            draw(c);
            return result;
        }
    }
}
