package zyz.hero.record_kit.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import zyz.hero.record_kit.R
import zyz.hero.record_kit.RecordState
import zyz.hero.record_kit.dp
import zyz.hero.record_kit.sp
import kotlin.math.min


/**
 * @author yongzhen_zou@163.com
 * @date 2022/4/2 11:52 下午
 */
class TimeProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    //录音状态进度条起始颜色
    var startColor = Color.parseColor("#4972F2")

    //录音状态进度条结束颜色
    var endColor = Color.parseColor("#95AFFF")

    //取消状态进度条起始颜色
    var cancelStartColor = Color.parseColor("#F34848")

    //取消状态进度条结束颜色
    var cancelEndColor = Color.parseColor("#F34848")

    //取消状态 进度条背景颜色
    var cancelCircleColor = Color.parseColor("#33F34848")

    //取消状态按钮中间图片资源ID
    var centerImg: Int = R.drawable.trash_can

    //录音状态按钮中间drawable ID
    var recordBgRes: Int = R.drawable.shape_time_progress_record_bg

    //取消状态按钮中间drawable ID
    var cancelBgRes: Int = R.drawable.shape_time_progress_cancel_bg

    //录音状态倒计时文本颜色
    var textColor: Int = Color.parseColor("#4872F2")

    //录音状态倒计时文本大小
    var textSize: Float = 17.sp

    //最大录音时长
    var maxValue: Int = 60
    private var startAngle: Float = -90f

    //progress 绘制宽度
    var lineWidth: Float = 2f
    private var currentValue: Float = 0f
    private var centerImageView: ImageView? = null
    private var centerTextView: TextView? = null
    private var paint: Paint? = null
    private var textPaint: Paint? = null
    private var cancelCirclePaint: Paint? = null
    private var recordingGradient: SweepGradient? = null
    private var cancelGradient: SweepGradient? = null
    private var drawAnimator: ValueAnimator? = null
    private var countDownAnimator: ValueAnimator? = null
    private var rectF: RectF = RectF()
    var onRecordEnd:(()->Unit)? = null
    var recordState: Int = RecordState.RECORDING
        set(value) {
            field = value
            when (value) {
                RecordState.START -> {
                    currentValue = 0f
                    setBackgroundResource(recordBgRes)
                    paint?.shader = recordingGradient
                    centerTextView?.visibility = View.VISIBLE
                    centerImageView?.visibility = View.GONE
                    startRecord()
                    postInvalidate()
                }
                RecordState.RECORDING -> {
                    centerTextView?.visibility = View.VISIBLE
                    centerImageView?.visibility = View.GONE
                    setBackgroundResource(recordBgRes)
                    paint?.shader = recordingGradient
                    postInvalidate()
                }
                RecordState.CANCEL -> {
                    centerTextView?.visibility = View.GONE
                    centerImageView?.visibility = View.VISIBLE
                    setBackgroundResource(cancelBgRes)
                    paint?.shader = cancelGradient
                    postInvalidate()
                }
                RecordState.RELEASE -> {
                    drawAnimator?.cancel()
                    countDownAnimator?.cancel()
                }
            }
        }

    private fun startRecord() {
        try {
            if (recordState != RecordState.RELEASE) {
                drawAnimator =
                    ValueAnimator.ofFloat(0f, 360f).apply {
                        duration = maxValue * 1000L
                        repeatCount = 0
                        interpolator = LinearInterpolator()
                        addUpdateListener {
                            currentValue = ((it.animatedValue as? Float) ?: 0f)
                            postInvalidate()
                        }
                        start()
                    }
                countDownAnimator = ValueAnimator.ofInt(0, maxValue).apply {
                    duration = maxValue * 1000L
                    repeatCount = 0
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        centerTextView?.text = formatText(it.animatedValue as? Int ?: 0)
                        if (it.animatedValue as Int == maxValue){
                            onRecordEnd?.invoke()
                        }
                    }
                    start()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatText(second: Int): String {
        var result = "0:00"
        try {
            var minutesResult: String
            var secondResult: String
            var minutes = second / 60
            minutesResult = minutes.toString()
            var seconds = second % 60
            if (seconds < 10) {
                secondResult = "0$seconds"
            } else {
                secondResult = "$seconds"
            }
            result = "$minutesResult:$secondResult"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_time_progress_view, this)
        centerImageView = findViewById(R.id.centerImg)
        centerTextView = findViewById(R.id.centerText)
        var typeArray = context.obtainStyledAttributes(attrs, R.styleable.TimeProgressView)
        startColor = typeArray.getColor(
            R.styleable.TimeProgressView_time_progress_start_color,
            Color.parseColor("#4972F2")
        )
        endColor = typeArray.getColor(
            R.styleable.TimeProgressView_time_progress_end_color,
            Color.parseColor("#95AFFF")
        )
        cancelStartColor =
            typeArray.getColor(
                R.styleable.TimeProgressView_time_progress_cancel_start_color,
                Color.parseColor("#fff34848")
            )
        cancelEndColor =
            typeArray.getColor(
                R.styleable.TimeProgressView_time_progress_cancel_end_color,
                Color.parseColor("#F34848")
            )
        cancelCircleColor =
            typeArray.getColor(
                R.styleable.TimeProgressView_time_progress_cancel_circle_color,
                Color.parseColor("#33F34848")
            )

        cancelBgRes =
            typeArray.getResourceId(
                R.styleable.TimeProgressView_time_progress_cancel_bg_res,
                R.drawable.shape_time_progress_cancel_bg
            )
        centerImg = typeArray.getResourceId(
            R.styleable.TimeProgressView_time_progress_center_img,
            R.drawable.trash_can
        )
        centerImageView?.setImageResource(centerImg)
        recordBgRes =
            typeArray.getResourceId(
                R.styleable.TimeProgressView_time_progress_record_bg_res,
                R.drawable.shape_time_progress_record_bg
            )
        textColor =
            typeArray.getColor(
                R.styleable.TimeProgressView_time_progress_text_color,
                Color.parseColor("#4872F2")
            )
        maxValue = typeArray.getInt(R.styleable.TimeProgressView_time_progress_max_value, 60)
        textSize =
            typeArray.getDimension(R.styleable.TimeProgressView_time_progress_text_size, 17.sp)
        centerTextView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        centerTextView?.setTextColor(textColor)
        lineWidth = typeArray.getFloat(R.styleable.TimeProgressView_time_progress_line_width, 2.dp)
        typeArray.recycle()
        paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = lineWidth
            strokeCap = Paint.Cap.ROUND
        }
        textPaint = Paint().apply {
            isAntiAlias = true
            color = textColor
            textSize = this@TimeProgressView.textSize
            textAlign = Paint.Align.CENTER
        }
        cancelCirclePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = lineWidth
            color = cancelCircleColor
        }
//        paint?.maskFilter = BlurMaskFilter(15.dp, BlurMaskFilter.Blur.SOLID)//高斯模糊阴影
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var minSize =
            min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(minSize, minSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(
            0f + lineWidth / 2,
            0f + lineWidth / 2,
            w.toFloat() - lineWidth / 2,
            h.toFloat() - lineWidth / 2
        )
        var recordQuarterColor = quarterColor(startColor, endColor)
        var cancelQuarterColor = quarterColor(cancelStartColor, cancelEndColor)
        recordingGradient = SweepGradient(
            rectF?.centerX(), rectF?.centerY(),
            intArrayOf(recordQuarterColor, endColor, startColor, recordQuarterColor),
            floatArrayOf(0f, 0.74f, 0.75f, 1f)
        )
        cancelGradient =
            SweepGradient(
                rectF?.centerX(), rectF?.centerY(),
                intArrayOf(
                    cancelQuarterColor,
                    cancelEndColor,
                    cancelStartColor,
                    cancelQuarterColor
                ),
                floatArrayOf(0f, 0.74f, 0.75f, 1f)
            )
        paint?.shader = if (recordState == RecordState.CANCEL) cancelGradient else recordingGradient
    }

    fun quarterColor(startColor: Int, endColor: Int): Int {
        return Color.rgb(
            (startColor and 0xff0000 shr 16) * 1.25.toInt() - (endColor and 0xff0000 shr 16) * 0.25.toInt(),
            (startColor and 0x00ff00 shr 8) * 1.25.toInt() - (endColor and 0x00ff00 shr 8) * 0.25.toInt(),
            (startColor and 0x0000ff) * 1.25.toInt() - (endColor and 0x0000ff) * 0.25.toInt()
        )
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setWillNotDraw(false)
        if (recordState == RecordState.CANCEL) {
            canvas?.drawCircle(
                rectF?.centerX(),
                rectF?.centerY(),
                rectF?.width() / 2,
                cancelCirclePaint!!
            )
        }
        canvas?.drawArc(rectF, startAngle, currentValue, false, paint!!)
    }

}