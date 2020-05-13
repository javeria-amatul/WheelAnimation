package com.javeria.wheelanimation

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircleSeekBar : View {

    private var mUnactiveWheelPaint: Paint? = null
    private var mActiveWheelPaint: Paint? = null
    private var mBlobPaint: Paint? = null
    private var mBlobRadius: Float = 0.toFloat()
    private var mWheelStrokeWidth: Int = 0
    private val mWheelRectangle = RectF()
    private var mTranslationOffset: Float = 0.toFloat()
    private var mColorWheelRadius: Float = 0.toFloat()
    private var mAngle: Float = 0.toFloat()
    var maxValue = 100
    private var active_wheel_color: Int = 0
    private var unactive_wheel_color: Int = 0
    private var blob_fill_color: Int = 0
    private var position = -1
    private var arc = 360
    private var start_arc = 270

    private var blobPosition: FloatArray? = null
    private var end_wheel: Int = 0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, defStyle, 0)
        initAttributes(typedArray)
        typedArray.recycle()

        mUnactiveWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mUnactiveWheelPaint!!.color = unactive_wheel_color
        mUnactiveWheelPaint!!.style = Style.STROKE
        mUnactiveWheelPaint!!.strokeWidth = mWheelStrokeWidth.toFloat()

        mBlobPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBlobPaint!!.color = blob_fill_color
        mBlobPaint!!.strokeWidth = mBlobRadius + 10

        mActiveWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mActiveWheelPaint!!.color = active_wheel_color
        mActiveWheelPaint!!.style = Style.STROKE
        mActiveWheelPaint!!.strokeWidth = mWheelStrokeWidth.toFloat()

        if (arc > end_wheel)
            arc = end_wheel

        mAngle = calculateAngleFromRadians(0)
        invalidate()
    }

    private fun initAttributes(a: TypedArray) {
        mWheelStrokeWidth = a.getInteger(R.styleable.CircleSeekBar_wheel_size, COLOR_WHEEL_STROKE_WIDTH_VALUE)
        mBlobRadius = a.getDimension(R.styleable.CircleSeekBar_blob_size, BLOB_RADIUS_DEF_VALUE)
        maxValue = a.getInteger(R.styleable.CircleSeekBar_max, MAX_VALUE)

        val wheel_active_color_attr = a.getString(R.styleable.CircleSeekBar_wheel_active_color)
        val wheel_unactive_color_attr = a.getString(R.styleable.CircleSeekBar_wheel_unactive_color)
        val blob_fill_color_attr = a.getString(R.styleable.CircleSeekBar_blob_fill_color)

        position = a.getInteger(R.styleable.CircleSeekBar_init_position, 0)

        start_arc = a.getInteger(R.styleable.CircleSeekBar_start_angle, START_ANGLE_DEF_VALUE)
        end_wheel = a.getInteger(R.styleable.CircleSeekBar_end_angle, END_WHEEL_DEFAULT_VALUE)

        if (wheel_active_color_attr != null) {
            try {
                active_wheel_color = Color.parseColor(wheel_active_color_attr)
            } catch (e: IllegalArgumentException) {
                active_wheel_color = Color.GREEN
            }
        } else {
            active_wheel_color = Color.GREEN
        }

        if (wheel_unactive_color_attr != null) {
            try {
                unactive_wheel_color = Color.parseColor(wheel_unactive_color_attr)
            } catch (e: IllegalArgumentException) {
                unactive_wheel_color = Color.GRAY
            }
        } else {
            unactive_wheel_color = Color.GRAY
        }

        if (blob_fill_color_attr != null) {
            try {
                blob_fill_color = Color.parseColor(blob_fill_color_attr)
            } catch (e: IllegalArgumentException) {
                blob_fill_color = Color.YELLOW
            }
        } else {
            blob_fill_color = Color.YELLOW
        }
    }

    override fun onDraw(canvas: Canvas) {

        canvas.translate(mTranslationOffset, mTranslationOffset)

        // Draw the wheel
        canvas.drawArc(
            mWheelRectangle,
            (start_arc + 270).toFloat(),
            (end_wheel - start_arc).toFloat(),
            false,
            mUnactiveWheelPaint!!
        )

        // Draw progress wheel.
        canvas.drawArc(
            mWheelRectangle, (start_arc + 270).toFloat(),
            (if (arc > end_wheel)
                end_wheel - start_arc
            else
                arc - start_arc).toFloat(), false, mActiveWheelPaint!!
        )

        // Draw blob
        canvas.drawCircle(
            blobPosition!![0], blobPosition!![1],
            mBlobRadius, mBlobPaint!!
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = Math.min(width, height)
        setMeasuredDimension(min, min)
        mTranslationOffset = min * 0.5f
        mColorWheelRadius = mTranslationOffset - mBlobRadius
        mWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius, mColorWheelRadius)
        updateBlobPosition()
    }


    private fun calculateRadiansFromAngle(angle: Float): Int {
        var unit = (angle / (2 * Math.PI)).toFloat()
        if (unit < 0) {
            unit += 1f
        }
        var radians = (unit * 360 - 360 / 4 * 3).toInt()
        if (radians < 0)
            radians += 360
        return radians
    }

    private fun calculateAngleFromRadians(radians: Int): Float {
        return ((radians + 270) * (2 * Math.PI) / 360).toFloat()
    }

    private fun updateBlobPosition() {
        blobPosition = calculateBlobPosition(mAngle)
    }

    /**
     * Calculate the blob's coordinates on the color wheel using the supplied
     * angle.
     */
    private fun calculateBlobPosition(angle: Float): FloatArray {
        val x = (mColorWheelRadius * Math.cos(angle.toDouble())).toFloat()
        val y = (mColorWheelRadius * Math.sin(angle.toDouble())).toFloat()
        return floatArrayOf(x, y)
    }

    fun setPosition(pos: Int) {
        position = pos
        mAngle = calculateAngleFromRadians(position)
        arc = calculateRadiansFromAngle(mAngle)
        updateBlobPosition()
        invalidate()
    }

    fun getAngle(): Float {
        return mAngle
    }

    companion object {
        val START_ANGLE_DEF_VALUE = 0
        val END_WHEEL_DEFAULT_VALUE = 360
        val COLOR_WHEEL_STROKE_WIDTH_VALUE = 16
        val BLOB_RADIUS_DEF_VALUE = 8f
        val MAX_VALUE = 100
    }
}