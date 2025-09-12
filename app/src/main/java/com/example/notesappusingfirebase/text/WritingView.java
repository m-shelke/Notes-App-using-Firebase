package com.example.notesappusingfirebase.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

//Canvas class
public class WritingView extends View {

    //Declaring Paint class
    Paint paint;

    //Storing lines in ArrayList
    private Path path;

    public ArrayList<Stroke> drawnStrokes = new ArrayList<>();
    public ArrayList<Stroke> undoneStrokes = new ArrayList<>();

    private ShapeType currentShape = ShapeType.FREE_DRAW;
    private float startX, startY, endX, endY;
    private Path tempShapePath;

    private boolean isMoveMode = false;
    private Stroke selectedStroke = null;
    private float lastTouchX, lastTouchY;


    public void setShapeType(ShapeType shapeType) {
        this.currentShape = shapeType;
    }

    public void setMoveMode(boolean enabled) {
        isMoveMode = enabled;
    }

    //Constructor of View class that this class extend
    public WritingView(Context context) {
        super(context);
        //calling init(); method
        init();
    }

    public WritingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //calling init(); method
        init();
    }

    //Method for initiating Paint class
    public void init(){
        
        //Here we init Paint class
        paint = new Paint();

        //Properties of Paint class
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(0f);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    //All thing will be Drawn here
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        //For-each loop for drawing lines
        for (Stroke stroke : drawnStrokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        if (tempShapePath != null && currentShape != ShapeType.FREE_DRAW) {
            canvas.drawPath(tempShapePath, paint);
        }
    }

    //This method for Touch Event
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (isMoveMode) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    selectedStroke = findTouchedStroke(x, y);
                    lastTouchX = x;
                    lastTouchY = y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (selectedStroke != null) {
                        float dx = x - lastTouchX;
                        float dy = y - lastTouchY;

                        // Move the selected shape
                        selectedStroke.path.offset(dx, dy);

                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    selectedStroke = null;
                    break;
            }
            return true;
        }

        // ---- DRAWING MODE ----
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            startX = x;
            startY = y;

            if (currentShape == ShapeType.FREE_DRAW) {
                path = new Path();
                path.moveTo(x, y);
                Paint newPaint = new Paint(paint);
                drawnStrokes.add(new Stroke(path, newPaint));
                undoneStrokes.clear();
            } else {
                tempShapePath = new Path();
            }

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

            if (currentShape == ShapeType.FREE_DRAW) {
                path.lineTo(x, y);
            } else {
                endX = x;
                endY = y;
                tempShapePath.reset();
                createShapePath(tempShapePath);
            }
            invalidate();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (currentShape != ShapeType.FREE_DRAW) {
                endX = x;
                endY = y;
                Path finalPath = new Path();
                createShapePath(finalPath);
                Paint newPaint = new Paint(paint);
                drawnStrokes.add(new Stroke(finalPath, newPaint));
                undoneStrokes.clear();
            }

            invalidate();
        }

        return true;
    }

    public Bitmap getDrawingBitmap() {

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    // ---- Undo ----
    public void undo() {
        if (!drawnStrokes.isEmpty()) {
            undoneStrokes.add(drawnStrokes.remove(drawnStrokes.size() - 1));

            if (tempShapePath != null){
                tempShapePath.reset();
            }
            invalidate();
        }
    }

    // ---- Redo ----
    public void redo() {
        if (!undoneStrokes.isEmpty()) {
            drawnStrokes.add(undoneStrokes.remove(undoneStrokes.size() - 1));

            if (tempShapePath != null){
                tempShapePath.reset();
            }
            invalidate();
        }
    }

    public void clearCanvas() {
        drawnStrokes.clear();
        undoneStrokes.clear();

        if (tempShapePath != null){
            tempShapePath.reset();
        }
        invalidate();
    }

    // Change color
    public void setLineColor(int color) {

        if (tempShapePath != null){
            tempShapePath.reset();
        }
        paint.setColor(color);
    }

    // Change stroke width
    public void setLineWidth(float width) {
        paint.setStrokeWidth(width);

        if (tempShapePath != null){
            tempShapePath.reset();
        }
        invalidate();

    }

    private static class Stroke {
        Path path;
        Paint paint;

        Stroke(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    public enum ShapeType {
        FREE_DRAW,
        RECTANGLE,
        CIRCLE,
        LINE,
        TRIANGLE
    }

    private void createShapePath(Path path) {
        switch (currentShape) {
            case RECTANGLE:
                path.addRect(startX, startY, endX, endY, Path.Direction.CW);
                break;

            case CIRCLE:
                float radius = (float) Math.hypot(endX - startX, endY - startY) / 2;
                float centerX = (startX + endX) / 2;
                float centerY = (startY + endY) / 2;
                path.addCircle(centerX, centerY, radius, Path.Direction.CW);
                break;

            case LINE:
                path.moveTo(startX, startY);
                path.lineTo(endX, endY);
                break;

            case TRIANGLE:
                float midX = (startX + endX) / 2;
                path.moveTo(midX, startY);
                path.lineTo(endX, endY);
                path.lineTo(startX, endY);
                path.close();
                break;
        }
    }

    @Nullable
    private Stroke findTouchedStroke(float x, float y) {
        for (int i = drawnStrokes.size() - 1; i >= 0; i--) {
            Stroke stroke = drawnStrokes.get(i);
            // Check if touch is near the stroke
            android.graphics.RectF bounds = new android.graphics.RectF();
            stroke.path.computeBounds(bounds, true);

            // Expand bounds a bit for easier touch
            bounds.inset(-30, -30);

            if (bounds.contains(x, y)) {
                return stroke;
            }
        }
        return null;
    }

}
