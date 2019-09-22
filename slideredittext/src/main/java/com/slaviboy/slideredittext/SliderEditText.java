package com.slaviboy.slideredittext;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Free SliderEditText (Java)
 *
 * Copyright (c) 2019 Stanislav Georgiev. (MIT License)
 * https://github.com/slaviboy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 *
 * Class that creates EditText view which values can be changed using the keyboard input method or
 * by using the view as slider. You can specify range for the values and can set color and shader
 * style for the background, foreground and text.
 */
public class SliderEditText extends EditText {

    public SliderEditText(Context context) {
        super(context);
        init(context, null);
    }

    public SliderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SliderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private Range range;                          // range values
    private Paint paint;                          // global paint object
    private Path backgroundPath;                  // path object for the background
    private Path foregroundPath;                  // path object for the foreground
    private Path clipPath;                        // used to cut excess part from the foreground
    private CornerRadius backgroundCornerRadius;  // corner radius for both paths
    private CornerRadius foregroundCornerRadius;  // corner radius for both paths
    private float width;                          // canvas width
    private float height;                         // canvas height
    private String suffix;                        // text suffix
    private String prefix;                        // text prefix
    private RectF textPadding;                    // padding for the text
    private RectF backgroundPadding;              // padding for the background
    private RectF foregroundPadding;              // padding for the foreground
    private int backgroundColor;                  // background color
    private int foregroundColor;                  // foreground color
    private Shader backgroundShader;              // background shader
    private Shader foregroundShader;              // foreground shader
    private int roundDecimalPlaces;               // to how many decimal places the double rounding should be done
    private boolean isMoved;                      // if finger is moved before touchup
    private boolean isTextEditMode;               // if view is in edit mode after touchdown->touchup events
    private boolean isInit;                       // if all values are initialized before redraw
    private float fingerX;                        // last finger x coordinate
    private float maxForegroundWidth;             // max allowed foreground width
    private boolean showForeground;               // if foreground should be drawn
    private boolean isRequestedFocus;             // if focus request is made inside(from this class)
    private double defaultValue;                  // default text value

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderEditText);

        // xml color
        backgroundColor = typedArray.getColor(R.styleable.SliderEditText_background_color, Color.parseColor("#333333"));
        foregroundColor = typedArray.getColor(R.styleable.SliderEditText_foreground_color, Color.parseColor("#666666"));

        // xml range
        float rangeLower = typedArray.getFloat(R.styleable.SliderEditText_range_lower, 0);
        float rangeUpper = typedArray.getFloat(R.styleable.SliderEditText_range_upper, 100);
        range = new Range(rangeLower, rangeUpper);

        // xml
        suffix = typedArray.getString(R.styleable.SliderEditText_suffix);
        prefix = typedArray.getString(R.styleable.SliderEditText_prefix);
        if (suffix == null) suffix = "";
        if (prefix == null) prefix = "";

        // background padding
        int backgroundPaddingAll = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_background_padding, 0);
        int backgroundPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_background_padding_left, backgroundPaddingAll);
        int backgroundPaddingTop = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_background_padding_top, backgroundPaddingAll);
        int backgroundPaddingRight = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_background_padding_right, backgroundPaddingAll);
        int backgroundPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_background_padding_bottom, backgroundPaddingAll);
        backgroundPadding = new RectF(
                backgroundPaddingLeft,
                backgroundPaddingTop,
                backgroundPaddingRight,
                backgroundPaddingBottom);

        // foreground padding
        int foregroundPaddingAll = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_foreground_padding, 0);
        int foregroundPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_foreground_padding_left, foregroundPaddingAll);
        int foregroundPaddingTop = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_foreground_padding_top, foregroundPaddingAll);
        int foregroundPaddingRight = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_foreground_padding_right, foregroundPaddingAll);
        int foregroundPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_foreground_padding_bottom, foregroundPaddingAll);
        foregroundPadding = new RectF(
                foregroundPaddingLeft,
                foregroundPaddingTop,
                foregroundPaddingRight,
                foregroundPaddingBottom);

        // text padding
        int textPaddingAll = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_text_padding, 0);
        int textPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_text_padding_left, textPaddingAll);
        int textPaddingTop = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_text_padding_top, textPaddingAll);
        int textPaddingRight = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_text_padding_right, textPaddingAll);
        int textPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_text_padding_bottom, textPaddingAll);
        textPadding = new RectF(
                textPaddingLeft,
                textPaddingTop,
                textPaddingRight,
                textPaddingBottom);
        setPadding(textPaddingLeft, textPaddingTop, textPaddingRight, textPaddingBottom);


        // xml corner radius attribute
        int cornerRadiusAll = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_corner_radius, 0);
        int cornerRadiusUpperLeft = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_corner_radius_upper_left, cornerRadiusAll);
        int cornerRadiusUpperRight = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_corner_radius_upper_right, cornerRadiusAll);
        int cornerRadiusLowerLeft = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_corner_radius_lower_left, cornerRadiusAll);
        int cornerRadiusLowerRight = typedArray.getDimensionPixelSize(R.styleable.SliderEditText_corner_radius_lower_right, cornerRadiusAll);
        backgroundCornerRadius = new CornerRadius(
                cornerRadiusUpperLeft,
                cornerRadiusUpperRight,
                cornerRadiusLowerLeft,
                cornerRadiusLowerRight);

        // same as background corner radius minus the minimum corner padding
        float cornerRadiusUpperLeftAdd = cornerRadiusUpperLeft > 0 ? Math.min(foregroundPaddingLeft, foregroundPaddingTop) : 0;
        float cornerRadiusUpperRightAdd = cornerRadiusUpperRight > 0 ? Math.min(foregroundPaddingRight, foregroundPaddingTop) : 0;
        float cornerRadiusLowerLeftAdd = cornerRadiusLowerLeft > 0 ? Math.min(foregroundPaddingLeft, foregroundPaddingBottom) : 0;
        float cornerRadiusLowerRightAdd = cornerRadiusLowerRight > 0 ? Math.min(foregroundPaddingRight, foregroundPaddingBottom) : 0;
        foregroundCornerRadius = new CornerRadius(
                cornerRadiusUpperLeft - cornerRadiusUpperLeftAdd,
                cornerRadiusUpperRight - cornerRadiusUpperRightAdd,
                cornerRadiusLowerLeft - cornerRadiusLowerLeftAdd,
                cornerRadiusLowerRight - cornerRadiusLowerRightAdd);

        defaultValue = typedArray.getFloat(R.styleable.SliderEditText_default_value, 0);
        range.setCurrent(defaultValue);

        roundDecimalPlaces = typedArray.getInteger(R.styleable.SliderEditText_round_decimal_places, 0);
        typedArray.recycle();

        init();
    }

    private void init() {

        // init paint
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        setText(round(range.getCurrent()) + "");
        setSingleLine(true); // allowed only single lines for the edit text
        setBackground(null); // remove bottom highlights
        postOnPreDraw(new Runnable() {
            @Override
            public void run() {
                 _onPreDraw();
            }
        }, this);
    }

    private void _onPreDraw() {

        width = getWidth();
        height = getHeight();

        maxForegroundWidth = width -
                (backgroundPadding.left + backgroundPadding.right +
                        foregroundPadding.left + foregroundPadding.right);

        backgroundPath = roundRect(
                backgroundPadding.left,
                backgroundPadding.top,
                width - (backgroundPadding.left + backgroundPadding.right),
                height - (backgroundPadding.top + backgroundPadding.bottom),
                backgroundCornerRadius);

        // clip path to cut the foreground
        clipPath = roundRect(
                backgroundPadding.left + foregroundPadding.left,
                backgroundPadding.top + foregroundPadding.top,
                width - 1 - backgroundPadding.left - backgroundPadding.right
                        - foregroundPadding.left - foregroundPadding.right,
                height - 1 - backgroundPadding.top - backgroundPadding.bottom
                        - foregroundPadding.top - foregroundPadding.bottom,
                foregroundCornerRadius);

        deactivateEditMode();
        isInit = true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {

        // if view is unfocused
        if (!focused) {
            deactivateEditMode();
            isMoved = false;
        } else {
            if (!isRequestedFocus) {
                activateEditMode();
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        // allow only numbers and comma
        String temp = text.toString().replaceAll("[^.0-9]", "");

        // fit in range
        if (temp.length() > 0) {
            double value = Double.parseDouble(temp);
            range.setCurrent(value);
        }
        super.onTextChanged(temp, start, lengthBefore, lengthAfter);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setAntiAlias(true);

        // draw background
        if (backgroundShader != null) {
            paint.setShader(backgroundShader);
        }
        paint.setColor(backgroundColor);
        canvas.drawPath(backgroundPath, paint);
        paint.setShader(null);

        if (showForeground) {

            // use path intersection instead of clipping, because clipping produce ruff edges
            foregroundPath.op(clipPath, Path.Op.INTERSECT);

            // draw foreground
            if (foregroundShader != null) {
                paint.setShader(foregroundShader);
            }
            paint.setColor(foregroundColor);
            canvas.drawPath(foregroundPath, paint);
            paint.setShader(null);
        }

        // draw text
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {

                if (!isTextEditMode) {
                    setLongClickable(false);
                    setCursorVisible(false);
                }
                fingerX = x;
            }
            break;

            case MotionEvent.ACTION_UP: {

                boolean isMovedPrevious = isMoved;
                isMoved = false;

                if (!isMovedPrevious) {
                    if (!isTextEditMode) {
                        // if text will activate edit mode
                        activateEditMode();
                    } else {
                        // can be used to exit edit mode if view was previously in edit mode
                    }
                } else {
                    // hide keyboard if finger was MOVED before UP
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
                    return false;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {

                if (isTextEditMode) {
                    deactivateEditMode();
                }

                if (!isMoved) {
                    isMoved = true;

                    // request focus and hide caret
                    isRequestedFocus = true;
                    requestFocus();
                    isRequestedFocus = false;

                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
                }

                // set foreground path and width
                update(x, y);

                // change text
                String value = round(range.getCurrent());
                setText(prefix + value + suffix);

                // call listener
                if (onChangeListener != null) {
                    onChangeListener.onChange(this);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Generate path that creates rectangles with rounded corners
     *
     * @param x      - rectangle left position
     * @param y      - rectangle top position
     * @param width  -rectangle width
     * @param height -rectangle height
     * @param radius - corner radius object
     * @return
     */
    private Path roundRect(float x, float y, float width, float height, CornerRadius radius) {

        // make sure corner radius is in range
        float min = Math.min(width, height);
        float upperLeft = radius.getUpperLeft();
        float upperRight = radius.getUpperRight();
        float lowerLeft = radius.getLowerLeft();
        float lowerRight = radius.getLowerRight();
        if (min < 2 * upperLeft) upperLeft = min / 2;
        if (min < 2 * upperRight) upperRight = min / 2;
        if (min < 2 * lowerLeft) lowerLeft = min / 2;
        if (min < 2 * lowerRight) lowerRight = min / 2;

        Path path = new Path();
        path.moveTo(x + upperLeft, y);
        path.lineTo(x + width - upperRight, y);
        path.quadTo(x + width, y, x + width, y + upperRight);
        path.lineTo(x + width, y + height - lowerRight);
        path.quadTo(x + width, y + height, x + width - lowerRight, y + height);
        path.lineTo(x + lowerLeft, y + height);
        path.quadTo(x, y + height, x, y + height - lowerLeft);
        path.lineTo(x, y + upperLeft);
        path.quadTo(x, y, x + upperLeft, y);
        path.close();

        return path;
    }


    /**
     * Deactivate edit mode, that means text will receive prefix and suffix,
     * foreground will be shown, caret will ne hidden and call the listener
     * method.
     */
    private void deactivateEditMode() {
        isTextEditMode = false;

        // set text with prefix and suffix
        String value = round(range.getCurrent());
        setText(prefix + value + suffix);

        // show foreground
        showForeground = true;

        // hide caret and selection
        setLongClickable(false);
        setCursorVisible(false);

        // set foreground path and width
        update(fingerX, 0);

        // call listener
        if (onChangeListener != null) {
            onChangeListener.onChange(this);
        }
    }

    /**
     * Activate edit mode, that means text will be displayed without prefix
     * and suffix. Foreground will be hidden and caret will be shown.
     */
    private void activateEditMode() {
        isTextEditMode = true;

        // set text without prefix and suffix
        setText(round(range.getCurrent()) + "");

        // hide foreground
        showForeground = false;

        // show caret and selection
        setLongClickable(true);
        setCursorVisible(true);
    }

    /**
     * Round double value to given decimal places, if decimal places is 0, the
     * the return string is rounded integer else rounded double.
     *
     * @param value double value to be rounded
     * @return
     */
    private String round(double value) {

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(roundDecimalPlaces, RoundingMode.HALF_UP);

        if (roundDecimalPlaces == 0) {
            return "" + (int) bd.doubleValue(); // return rounded int
        } else {
            return "" + bd.doubleValue();       // return rounded double
        }
    }

    /**
     * Update foreground width, change current range value, and create new
     * foreground rounded rectangle path. And finally redraw view.
     *
     * @param x
     * @param y
     */
    private void update(float x, float y) {

        float diffX = x - fingerX;
        float foregroundWidth = (float) (
                -maxForegroundWidth * (range.getCurrent() - range.getLower()) / (range.getLower() - range.getUpper()));

        // make sure foreground width is in range
        float newForegroundWidth = foregroundWidth + diffX;
        if (newForegroundWidth < 0) {
            newForegroundWidth = 0;
        } else if (newForegroundWidth >= maxForegroundWidth) {
            newForegroundWidth = maxForegroundWidth;
        }

        // set current range value
        range.setCurrent(
                maxForegroundWidth,
                newForegroundWidth);

        // set new foreground path using the new foreground width
        foregroundPath = roundRect(
                backgroundPadding.left + foregroundPadding.left,
                backgroundPadding.top + foregroundPadding.top,
                newForegroundWidth,
                height - 1 - backgroundPadding.top - backgroundPadding.bottom
                        - foregroundPadding.top - foregroundPadding.bottom, foregroundCornerRadius);

        // update values
        fingerX = x;

        invalidate();
    }

    /**
     * Set runnable, used to observe and call method run() after final
     * measurement is made, and view is about to be drawn
     *
     * @param runnable
     * @param view
     */
    private void postOnPreDraw(final Runnable runnable, final View view) {

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                try {
                    runnable.run();
                    return true;
                } finally {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                }
            }
        });
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        range.setCurrent(defaultValue);
        this.range = range;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public RectF getTextPadding() {
        return textPadding;
    }

    public void setTextPadding(RectF textPadding) {
        this.textPadding = textPadding;
        setPadding((int) textPadding.left, (int) textPadding.top, (int) textPadding.right, (int) textPadding.bottom);
    }

    public RectF getBackgroundPadding() {
        return backgroundPadding;
    }

    public void setBackgroundPadding(RectF backgroundPadding) {
        this.backgroundPadding = backgroundPadding;
    }

    public RectF getForegroundPadding() {
        return foregroundPadding;
    }

    public void setForegroundPadding(RectF foregroundPadding) {
        this.foregroundPadding = foregroundPadding;
    }

    public Shader getBackgroundShader() {
        return backgroundShader;
    }

    public void setBackgroundShader(Shader backgroundShader) {
        this.backgroundShader = backgroundShader;
    }

    public Shader getForegroundShader() {
        return foregroundShader;
    }

    public void setForegroundShader(Shader foregroundShader) {
        this.foregroundShader = foregroundShader;
    }

    public int getRoundDecimalPlaces() {
        return roundDecimalPlaces;
    }

    public void setRoundDecimalPlaces(int roundDecimalPlaces) {
        this.roundDecimalPlaces = roundDecimalPlaces;
    }

    public CornerRadius getCornerRadius() {
        return backgroundCornerRadius;
    }

    public void setCornerRadius(CornerRadius cornerRadius) {
        this.backgroundCornerRadius = cornerRadius;

        // update foreground corner radius
        float cornerRadiusUpperLeftAdd = cornerRadius.getUpperLeft() > 0 ? Math.min(foregroundPadding.left, foregroundPadding.top) : 0;
        float cornerRadiusUpperRightAdd = cornerRadius.getUpperRight() > 0 ? Math.min(foregroundPadding.right, foregroundPadding.top) : 0;
        float cornerRadiusLowerLeftAdd = cornerRadius.getLowerLeft() > 0 ? Math.min(foregroundPadding.left, foregroundPadding.bottom) : 0;
        float cornerRadiusLowerRightAdd = cornerRadius.getLowerRight() > 0 ? Math.min(foregroundPadding.right, foregroundPadding.bottom) : 0;
        foregroundCornerRadius = new CornerRadius(
                cornerRadius.getUpperLeft() - cornerRadiusUpperLeftAdd,
                cornerRadius.getUpperRight() - cornerRadiusUpperRightAdd,
                cornerRadius.getLowerLeft() - cornerRadiusLowerLeftAdd,
                cornerRadius.getLowerRight() - cornerRadiusLowerRightAdd);

        // update clip path
        clipPath = roundRect(
                backgroundPadding.left + foregroundPadding.left,
                backgroundPadding.top + foregroundPadding.top,
                width - 1 - backgroundPadding.left - backgroundPadding.right
                        - foregroundPadding.left - foregroundPadding.right,
                height - 1 - backgroundPadding.top - backgroundPadding.bottom
                        - foregroundPadding.top - foregroundPadding.bottom,
                foregroundCornerRadius);
    }

    public double getValue() {
        return range.getCurrent();
    }

    public void setValue(double value) {

        if (!isInit) {
            return;
        }

        // change
        range.setCurrent(value);
        String valueString = round(range.getCurrent());
        setText(prefix + valueString + suffix);
        update(fingerX, 0);
    }

    public void setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        range.setCurrent(defaultValue);
    }

    private OnChangeListener onChangeListener;

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onChange(SliderEditText slider);
    }
}
