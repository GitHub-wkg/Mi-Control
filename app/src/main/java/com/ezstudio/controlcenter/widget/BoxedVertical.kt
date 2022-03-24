package com.ezstudio.controlcenter.widget

import android.R.attr.path
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ezstudio.controlcenter.R


class BoxedVertical : View {
    /**
     * The min value of progress value.
     */
    private var mMin = MIN

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private var mMax = MAX

    /**
     * The increment/decrement value for each movement of progress.
     */
    var step = 10

    /**
     * The corner radius of the view.
     */
    private var mCornerRadius = 48

    /**
     * Text size in SP.
     */
    private var mTextSize = 26f

    /**
     * Text bottom padding in pixel.
     */
    private var mtextBottomPadding = 20
    private var mPoints = 0
    private var mEnabled = true

    /**
     * Enable or disable text .
     */
    private var mtextEnabled = true

    /**
     * Enable or disable image .
     */
    var isImageEnabled = false

    /**
     * mTouchDisabled touches will not move the slider
     * only swipe motion will activate it
     */
    private var mTouchDisabled = true
    private var mProgressSweep = 0f
    private var mProgressPaint: Paint? = null
    private var mTextPaint: Paint? = null
    private var scrWidth = 0
    private var scrHeight = 0
    private var mOnValuesChangeListener: OnValuesChangeListener? = null
    private var backgroundColors = 0
    private var mDefaultValue = 0
    private var mDefaultImage: Bitmap? = null
    private var mMinImage: Bitmap? = null
    private var mMaxImage: Bitmap? = null
    private var progressColor = 0
    private val dRect = Rect()
    private var firstRun = true
    private var  mPath  =  Path()
    private var paint = Paint()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        println("INIT")
        val density = resources.displayMetrics.density

        // Defaults, may need to link this into theme settings
        progressColor = ContextCompat.getColor(context, R.color.color_progress)
        backgroundColors = ContextCompat.getColor(context, R.color.color_background)
        backgroundColors = ContextCompat.getColor(context, R.color.color_background)
        var textColor = ContextCompat.getColor(context, R.color.color_text)
        mTextSize = (mTextSize * density)
        mDefaultValue = mMax / 2
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.BoxedVertical, 0, 0
            )
            mPoints = a.getInteger(R.styleable.BoxedVertical_points, mPoints)
            mMax = a.getInteger(R.styleable.BoxedVertical_max, mMax)
            mMin = a.getInteger(R.styleable.BoxedVertical_min, mMin)
            step = a.getInteger(R.styleable.BoxedVertical_step, step)
            mDefaultValue = a.getInteger(R.styleable.BoxedVertical_defaultValues, mDefaultValue)
            mCornerRadius = a.getInteger(R.styleable.BoxedVertical_libCornerRadius, mCornerRadius)
            mtextBottomPadding =
                a.getInteger(R.styleable.BoxedVertical_textBottomPadding, mtextBottomPadding)
            //Images
            isImageEnabled = a.getBoolean(R.styleable.BoxedVertical_imageEnabled, isImageEnabled)
            if (isImageEnabled) {
                val drawableDefault = a.getDrawable(R.styleable.BoxedVertical_defaultImage)
                //
                val bitmapDefault = Bitmap.createBitmap(
                    drawableDefault!!.intrinsicWidth,
                    drawableDefault.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                //
//                val canvas = Canvas(bitmapDefault)
//                drawableDefault.setBounds(0, 0, canvas.width, canvas.height)
//                drawableDefault.draw(canvas)
                mDefaultImage = bitmapDefault
                //
                val drawableMin = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_brightness
                )
                //
                val bitmapMin = Bitmap.createBitmap(
                    drawableMin!!.intrinsicWidth,
                    drawableMin.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                //
//                val canvasMin = Canvas(bitmapMin)
//                drawableMin.setBounds(0, 0, canvas.width, canvas.height)
//                drawableMin.draw(canvasMin)
                mMinImage = bitmapMin

                //
                val drawableMax = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_brightness
                )
                //
                val bitmapMax = Bitmap.createBitmap(
                    drawableMax!!.intrinsicWidth,
                    drawableMax.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                //
//                val canvasMax = Canvas(bitmapMax)
//                drawableMax.setBounds(0, 0, canvas.width, canvas.height)
//                drawableMax.draw(canvasMax)
                mMaxImage = bitmapMax

            }
            progressColor = a.getColor(R.styleable.BoxedVertical_progressColor, progressColor)
            backgroundColors =
                a.getColor(R.styleable.BoxedVertical_backgroundColor, backgroundColors)
            mTextSize = a.getDimension(R.styleable.BoxedVertical_textSize, mTextSize)
            textColor = a.getColor(R.styleable.BoxedVertical_textColor, textColor)
            mEnabled = a.getBoolean(R.styleable.BoxedVertical_enabled, mEnabled)
            mTouchDisabled = a.getBoolean(R.styleable.BoxedVertical_touchDisabled, mTouchDisabled)
            mtextEnabled = a.getBoolean(R.styleable.BoxedVertical_textEnabled, mtextEnabled)
            mPoints = mDefaultValue
            a.recycle()
        }

        // range check
        mPoints = if (mPoints > mMax) mMax else mPoints
        mPoints = if (mPoints < mMin) mMin else mPoints
        mProgressPaint = Paint()
        mProgressPaint!!.color = progressColor
        mProgressPaint!!.isAntiAlias = true
        mProgressPaint!!.style = Paint.Style.STROKE
        mTextPaint = Paint()
        mTextPaint!!.color = textColor
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.style = Paint.Style.FILL
        mTextPaint!!.textSize = mTextSize
        scrHeight = context.resources.displayMetrics.heightPixels
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        scrWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        scrHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        mProgressPaint!!.strokeWidth = scrWidth.toFloat()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        paint.alpha = 255
        canvas.translate(0f, 0f)
        mPath.addRoundRect(
            RectF(0F, 0F, scrWidth.toFloat(), scrHeight.toFloat()),
            mCornerRadius.toFloat(),
            mCornerRadius.toFloat(),
            Path.Direction.CCW
        )
        canvas.clipPath(mPath, Region.Op.INTERSECT)
        paint.color = backgroundColors
        paint.isAntiAlias = true
        canvas.drawRect(0f, 0f, scrWidth.toFloat(), scrHeight.toFloat(), paint)
        canvas.drawLine(
            (canvas.width / 2).toFloat(),
            canvas.height.toFloat(),
            (canvas.width / 2).toFloat(),
            mProgressSweep,
            mProgressPaint!!
        )
        if (isImageEnabled && mDefaultImage != null && mMinImage != null && mMaxImage != null) {
            //If image is enabled, text will not be shown
            if (mPoints == mMax) {
                drawIcon(mMaxImage!!, canvas)
            } else if (mPoints == mMin) {
                drawIcon(mMinImage!!, canvas)
            } else {
                drawIcon(mDefaultImage!!, canvas)
            }
        } else {
            //If image is disabled and text is enabled show text
            if (mtextEnabled) {
                val strPoint = mPoints.toString()
                drawText(canvas, mTextPaint, strPoint)
            }
        }
        if (firstRun) {
            firstRun = false
            value = mPoints
        }
    }

    private fun drawText(canvas: Canvas, paint: Paint?, text: String) {
        canvas.getClipBounds(dRect)
        val cWidth = dRect.width()
        paint!!.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, dRect)
        val x = cWidth / 2f - dRect.width() / 2f - dRect.left
        canvas.drawText(text, x, (canvas.height - mtextBottomPadding).toFloat(), paint)
    }

    private fun drawIcon(bitmap: Bitmap, canvas: Canvas) {
        var bitmap = bitmap
        bitmap = getResizedBitmap(bitmap, (canvas.width / 2) + 10, canvas.width / 2)
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(
                (canvas.width / 2 - bitmap.width / 2).toFloat() + 2F,
                (canvas.height - bitmap.height).toFloat(),
                (canvas.width / 3 + bitmap.width).toFloat(),
                canvas.height.toFloat() + 10
            ),
            null
        )
    }

    private fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        //Thanks Piyush
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // create a matrix for the manipulation
        val matrix = Matrix()
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight)
        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {
            this.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mOnValuesChangeListener != null) mOnValuesChangeListener!!.onStartTrackingTouch(
                        this
                    )
                    if (!mTouchDisabled) updateOnTouch(event)
                }
                MotionEvent.ACTION_MOVE -> updateOnTouch(event)
                MotionEvent.ACTION_UP -> {
                    if (mOnValuesChangeListener != null) mOnValuesChangeListener!!.onStopTrackingTouch(
                        this
                    )
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (mOnValuesChangeListener != null) mOnValuesChangeListener!!.onStopTrackingTouch(
                        this
                    )
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
        return false
    }

    /**
     * Update the UI components on touch events.
     *
     * @param event MotionEvent
     */
    private fun updateOnTouch(event: MotionEvent) {
        isPressed = true
        val mTouch = convertTouchEventPoint(event.y)
        val progress = Math.round(mTouch).toInt()
        updateProgress(progress)
    }

    private fun convertTouchEventPoint(yPos: Float): Double {
        val wReturn: Float
        if (yPos > scrHeight * 2) {
            wReturn = (scrHeight * 2).toFloat()
            return wReturn.toDouble()
        } else if (yPos < 0) {
            wReturn = 0f
        } else {
            wReturn = yPos
        }
        return wReturn.toDouble()
    }

    private fun updateProgress(progress: Int) {
        var progress = progress
        mProgressSweep = progress.toFloat()
        progress = if (progress > scrHeight) scrHeight else progress
        progress = if (progress < 0) 0 else progress

        //convert progress to min-max range
        mPoints = progress * (mMax - mMin) / scrHeight + mMin
        //reverse value because progress is descending
        mPoints = mMax + mMin - mPoints
        //if value is not max or min, apply step
        if (mPoints != mMax && mPoints != mMin) {
            mPoints = mPoints - mPoints % step + mMin % step
        }
        if (mOnValuesChangeListener != null) {
            mOnValuesChangeListener!!
                .onPointsChanged(this, mPoints)
        }
        invalidate()
    }

    /**
     * Gets a value, converts it to progress for the seekBar and updates it.
     *
     * @param value The value given
     */
    private fun updateProgressByValue(value: Int) {
        mPoints = value
        mPoints = if (mPoints > mMax) mMax else mPoints
        mPoints = if (mPoints < mMin) mMin else mPoints

        //convert min-max range to progress
        mProgressSweep = ((mPoints - mMin) * scrHeight / (mMax - mMin)).toFloat()
        //reverse value because progress is descending
        mProgressSweep = scrHeight - mProgressSweep
        if (mOnValuesChangeListener != null) {
            mOnValuesChangeListener!!
                .onPointsChanged(this, mPoints)
        }
        invalidate()
    }

    interface OnValuesChangeListener {
        /**
         * Notification that the point value has changed.
         *
         * @param boxedPoints The SwagPoints view whose value has changed
         * @param points      The current point value.
         */
        fun onPointsChanged(boxedPoints: BoxedVertical?, points: Int)
        fun onStartTrackingTouch(boxedPoints: BoxedVertical?)
        fun onStopTrackingTouch(boxedPoints: BoxedVertical?)
    }

    var value: Int
        get() = mPoints
        set(points) {
            var points = points
            points = if (points > mMax) mMax else points
            points = if (points < mMin) mMin else points
            updateProgressByValue(points)
        }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    var max: Int
        get() = mMax
        set(mMax) {
            require(mMax > mMin) { "Max should not be less than zero" }
            this.mMax = mMax
        }
    var cornerRadius: Int
        get() = mCornerRadius
        set(mRadius) {
            mCornerRadius = mRadius
            invalidate()
        }
    var defaultValue: Int
        get() = mDefaultValue
        set(mDefaultValue) {
            require(mDefaultValue <= mMax) { "Default value should not be bigger than max value." }
            this.mDefaultValue = mDefaultValue
        }


    var setProgressColor: Int
        get() = progressColor
        set(progressColor) {
            this.progressColor = progressColor
            mProgressPaint!!.color = progressColor
            invalidate()
        }

    var setBackgroundColorDimmer: Int
        get() = backgroundColors
        set(backgroundColors) {
            this.backgroundColors = backgroundColors
            invalidate()
        }

    fun setOnBoxedPointsChangeListener(onValuesChangeListener: OnValuesChangeListener?) {
        mOnValuesChangeListener = onValuesChangeListener
    }

    companion object {
        private val TAG = BoxedVertical::class.java.simpleName
        private const val MAX = 100
        private const val MIN = 0
    }
}