package com.example.notesappusingfirebase.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.notesappusingfirebase.R;

//Extending from View parent class
public class VerticalSeekBar extends View {

    //Creating obj of Paint for drawing different part of Vertical progress bar
    private Paint barBackgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;

    //Seek Bar properties (The desired width of the vertical bar in pixels)
    private RectF barRect;
    private float barWidth;
    //The radius for rounding the corner of the bar
    private float barCornerRadius;

    //Thumb properties (The drawable used for the draggable thumb)
    private Drawable thumbDrawable;
    //The width of the thumb drawable
    private float thumbWidth;
    //The Height of the thumb drawable
    private float thumbHeight;

    //The X-coordinate (Center) of the thumb
    private float thumbX;
    //The X-coordinate (Center) of the thumb
    private float thumbY;

    //Seek Bar Configuration
    //(The maximum value the seek bar can reach)
    private int maxProgress = 100;
    //The current progress value. Default is 0
    private int currentProgress = 0;
    //Flag to indicate, if the thumb is currently being dragged by the user
    private boolean isDragging = false;
    //Determines if progress fills form bottom (true) or top (false). Default is true
    private boolean progressFromBottom = true;
    //Flag to enable/disable the display of progress text. Default is false
    private boolean textEnabled = false;
    //Margin for the progress text (though its usage for positioning is not direct in current drawProgressText)
    private float textMargin;
    //suffix to append to the progress text (e.g "%","vol")
    private String textSuffix = "";


 //   OnSeekBarChangeListener listener;
 private OnSeekBarChangeListener mListener;

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.mListener = listener;
    }



    //Overriding methods of parent class
    //In all constructor, call the init(); method to perform common initialization
    public VerticalSeekBar(Context context) {
        super(context);
        init(context,null);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }


    //Initialization Method (for all the declared variable)
    private void init(Context context, AttributeSet attrs) {

        //Initialize Paint obj (with anti-aliasing for smooth drawing)
        barBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barRect = new RectF();

        //Set default value (For colors and dimensions)
        int barBackgroundColor = Color.GRAY;
        int progressColor = Color.RED;

        //Convert dp to pixels
        barWidth = dbToPx(20);
        thumbWidth = dbToPx(20);
        thumbHeight = dbToPx(20);

        int textColor = Color.BLACK;
        float textSize = dbToPx(14);
        textMargin = dbToPx(12);

        //Process Custom Attributes from XML if they exist
        if (attrs != null) {

            //Obtain a TypedArray (To read attributes defined in R.styleable.VerticalSeekBar)
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar);

            //Read colors, dimensions and flag form attributes
            barBackgroundColor = ta.getColor(R.styleable.VerticalSeekBar_vsb_barBackgroundColor, barBackgroundColor);
            progressColor = ta.getColor(R.styleable.VerticalSeekBar_vsb_progressColor, progressColor);
            barWidth = ta.getDimension(R.styleable.VerticalSeekBar_vsb_barWidth, barWidth);
            maxProgress = ta.getInt(R.styleable.VerticalSeekBar_vsb_max, 100);
            currentProgress = ta.getInt(R.styleable.VerticalSeekBar_vsb_progress, 0);

            //Check if a custom thumb drawable is provide
            if (ta.hasValue(R.styleable.VerticalSeekBar_vsb_thumb)){
                thumbDrawable = ta.getDrawable(R.styleable.VerticalSeekBar_vsb_thumb);
            }
            thumbWidth = ta.getDimension(R.styleable.VerticalSeekBar_vsb_thumbWidth,thumbWidth);
            thumbHeight = ta.getDimension(R.styleable.VerticalSeekBar_vsb_thumbHeight,thumbHeight);
            progressFromBottom = ta.getBoolean(R.styleable.VerticalSeekBar_vsb_progress_from_bottom,true);

            textEnabled = ta.getBoolean(R.styleable.VerticalSeekBar_vsb_textEnabled,false);
            textColor = ta.getColor(R.styleable.VerticalSeekBar_vsb_textColor,textColor);
            textSize = ta.getDimension(R.styleable.VerticalSeekBar_vsb_textSize,textSize);
            textMargin = ta.getDimension(R.styleable.VerticalSeekBar_vsb_textMargin,textMargin);
            textSuffix = ta.getString(R.styleable.VerticalSeekBar_vsb_textSuffix);

            //Ensure textSuffix is not null
            if (textSuffix == null) textSuffix = "";
        }

        //Apply the determined colors and styles to the Paint obj
        barBackgroundPaint.setColor(barBackgroundColor);
        progressPaint.setColor(progressColor);

        //Set corner radius to half bar width for perfect round
        barCornerRadius = barWidth / 2;
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextSize(40f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        //Bug / Potential Issue
        if (thumbDrawable == null){
            //This line will always overwrite 'thumbDrawable' with 'R.drawable.thumb'. If 'thumbDrawable' was not null (meaning it was set via XML or was initially null and then somehow set)
            thumbDrawable = ContextCompat.getDrawable(context,R.drawable.ic_launcher_background);
        }
    }

    private float dbToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    //Measurement & Layout

    //Calculate the desired, need to accommodate padding, thumb width and bar width. Take the maximum of thumb width and bar width to ensure enough space
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = (int) Math.max(getPaddingLeft() + getPaddingRight() + thumbWidth, barWidth);
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float barLeft = (w - barWidth) / 2;
        float barRight = (barLeft + barWidth);
        float thumbHalfHeight = thumbHeight / 2;
        float barTop = getPaddingTop() + thumbHalfHeight;
        float barBottom = h - getPaddingBottom() - thumbHalfHeight;

        barRect.set(barLeft, barTop, barRight, barBottom);

        thumbX = w / 2f;

        updateThumbPosition();
    }

    private void updateThumbPosition() {

        thumbY = getProgressY();
        invalidate();

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);
        drawBarBackground(canvas);
        drawThumb(canvas);
        drawProgressBar(canvas);


        if (textEnabled){
            drawProgressText(canvas);
        }
    }

    private void drawProgressText(Canvas canvas) {

            if (isDragging) {
                canvas.drawText(currentProgress+"", thumbX , thumbY , textPaint);
            }


        canvas.drawText(currentProgress+"", thumbX , thumbY , textPaint);



        String textToDraw = currentProgress + textSuffix;
        Rect textBounds = new Rect();
        textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), textBounds);

        textPaint.setTextAlign(Paint.Align.CENTER);

        float x = barRect.centerX();
        float y = ((textPaint.descent() + textPaint.ascent() / 2f));

        canvas.drawText(textToDraw, x, y, textPaint);
    }

    private void drawThumb(Canvas canvas) {

        if (thumbDrawable != null){
            int thumbHalfWidth = (int) (thumbWidth/2);
            int thumbHalfHeight = (int) (thumbHeight/2);

            thumbDrawable.setBounds(
                    (int)(thumbX - thumbHalfWidth),
                    (int)(thumbY - thumbHalfHeight),
                    (int)(thumbX + thumbHalfWidth),
                    (int)(thumbY + thumbHalfHeight));

            thumbDrawable.draw(canvas);
        }
    }

    private void drawProgressBar(Canvas canvas) {

        float progressY = getProgressY();

        if (progressFromBottom){
            canvas.drawRoundRect(barRect.left, progressY, barRect.right, barRect.bottom, barCornerRadius, barCornerRadius, progressPaint);
        }else {
            canvas.drawRoundRect(barRect.left, barRect.top, barRect.right, barRect.bottom, barCornerRadius, barCornerRadius, progressPaint);
        }
    }

    private float getProgressY() {

        float barDrawableHeight = barRect.height();
        float progressRatio = (float) currentProgress / maxProgress;

        if (progressFromBottom){
            return barRect.top + barDrawableHeight * (1 - progressRatio);
        }

        return barRect.top + barDrawableHeight * progressRatio;
    }

    private void drawBarBackground(Canvas canvas) {

        canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barBackgroundPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) return false;
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (isTouchOnThumb(y)){
                    isDragging = true;

                    if (mListener != null) mListener.onStartTrackingTouch(this);

                    updateProgressFromTouch(y);
                    invalidate();

                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                isDragging =  true;

                if (isDragging){
                    updateProgressFromTouch(y);
                    if (mListener != null) {
                        mListener.onProgressChanged(this, currentProgress, true);
                    }
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:

                if (isDragging){
                    getParent().requestDisallowInterceptTouchEvent(false);
                    isDragging = false;

                    if (mListener != null) mListener.onStopTrackingTouch(this);
                    return true;
                }
                break;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            isDragging = true;
            if (mListener != null) mListener.onStartTrackingTouch(this);
            updateProgressFromTouch(y);
            invalidate();
            if (mListener != null) mListener.onStopTrackingTouch(this);
            isDragging = false;
            return true;
        }

        return super.onTouchEvent(event);


    }

    private void updateProgressFromTouch(float y) {

        float clampedY = Math.max(barRect.top, Math.min(y, barRect.bottom));
        float progressRatio;

        if (progressFromBottom){
            progressRatio = 1 - ((clampedY - barRect.top) / barRect.height());
        }else {
            progressRatio = (clampedY - barRect.top) / barRect.height();
        }

        setProgress((int)(maxProgress * progressRatio));
        invalidate();
    }

    private void setProgress(int progress) {

        progress = Math.max(0,Math.min(progress, maxProgress));

        if (this.currentProgress != progress){
            this.currentProgress = progress;
            updateThumbPosition();

            if (mListener != null){
                mListener.onProgressChanged(this,currentProgress, isDragging);
            }
        }
    }

    public int getProgress(){
        return currentProgress;
    }

    public int getMax(){
        return maxProgress;
    }

    public void setMax(int max){

        if (max > 0){
            this.maxProgress = max;
            setProgress(Math.min(currentProgress, maxProgress));
        }
    }

    public void setTextEnabled(boolean textEnabled) {
        this.textEnabled = textEnabled;
        invalidate();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public void setTextSize(float size) {
        this.setTextSize(size);
        invalidate();
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    public void setBarBackgroundColor(int color) {
        barBackgroundPaint.setColor(color);
        invalidate();
    }

    public void setBarWidth(int size) {
        this.barWidth = size;
        this.barCornerRadius = size / 2;
        requestLayout();
        invalidate();
    }

    public void setThumb(Drawable drawable) {
        this.thumbDrawable = drawable;
        invalidate();
    }

    public void setThumb(int drawableResId) {
        this.thumbDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
    }

    public void setThumbSize(float width, float height) {
        this.thumbWidth = width;
        this.thumbHeight = height;
        requestLayout();
        invalidate();
    }

    private boolean isTouchOnThumb(float y) {
        float thumbTouchRadius = thumbHeight;
        return Math.abs(y - thumbY) <= thumbTouchRadius;
    }


//    public interface OnSeekBarChangeListener{
//       void onProgressChange(VerticalSeekBar verticalSeekBar, int progress, boolean fromUser);
//       void onStartTrackingTouch(VerticalSeekBar verticalSeekBar);
//       void onStopTrackingTouch(VerticalSeekBar verticalSeekBar);
//
//    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(VerticalSeekBar seekBar);
        void onStopTrackingTouch(VerticalSeekBar seekBar);
    }

}
