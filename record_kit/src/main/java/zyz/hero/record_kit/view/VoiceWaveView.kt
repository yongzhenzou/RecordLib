package zyz.hero.record_kit.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import zyz.hero.record_kit.R
import zyz.hero.record_kit.RecordState
import zyz.hero.record_kit.dp
import kotlin.math.min

/**
 * @author yongzhen_zou@163.com
 * @date 2022/4/1 9:39 下午
 */
class VoiceWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var recorder: MediaRecorder? = null
    var lineColor = 0
        //线条颜色
        set( value) {
            field = value
            postInvalidate()
        }
    var cancelLineColor = 0
        //取消线条颜色
        set( value) {
            field = value
            postInvalidate()
        }
    var lineWidth = 2.dp
        //线条宽度
        set(value) {
            field = value
            postInvalidate()
        }
    var lineSpace = 3.dp
        //线条间隔
        set(value) {
            field = value
            postInvalidate()
        }
    var maxHeight = 20.dp
        //线条最大高度
        set(value) {
            field = value
            postInvalidate()
        }
    var minHeight = 5.dp
        //线条最小高度
        set(value) {
            field = value
            postInvalidate()
        }
    var timeInterval = 50
        //线条滚动时间间隔，单位毫秒
        set(value) {
            field = value
            postInvalidate()
        }
    var maxValue: Int = 60
    private var paint: Paint? = null
    private var rectF: RectF = RectF()

    //存储voice对应的高度
    lateinit var voiceArray: IntArray
    var recordState: Int = RecordState.RECORDING
        set(value) {
            when (value) {
                RecordState.START -> {
                    animationStart()
                    paint?.color = lineColor
                }
                RecordState.RECORDING -> {
                    paint?.color = lineColor
                }
                RecordState.CANCEL -> {
                    paint?.color = cancelLineColor
                }
                RecordState.RELEASE -> {
                    animator?.cancel()
                    recorder = null
                }
            }
            field = value
        }
    var animator: Animator? = null
    private fun animationStart() {
        if (::voiceArray.isInitialized&&voiceArray!=null){
            voiceArray.forEachIndexed { index, i ->
                voiceArray[index] = 0
            }
            invalidate()
        }
        animator = ValueAnimator.ofFloat(0f, maxValue * 1000f / timeInterval).apply {
            duration = maxValue * 1000L
            repeatCount = 0
            interpolator = LinearInterpolator()
            addUpdateListener {
                if (recordState != RecordState.RELEASE){
                    postInvalidate()
                }
            }
            start()
        }
    }

    init {
        var typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView,defStyleAttr,0)
        lineColor =
            typedArray.getColor(R.styleable.WaveView_wave_line_color, Color.parseColor("#AAB8E2"))
        cancelLineColor =
            typedArray.getColor(R.styleable.WaveView_wave_line_cancel_color,
                Color.parseColor("#FFE8E8"))
        lineWidth = typedArray.getDimension(R.styleable.WaveView_wave_line_width, 1f)
        lineSpace = typedArray.getDimension(R.styleable.WaveView_wave_line_space, 1f)
        maxHeight = typedArray.getDimension(R.styleable.WaveView_wave_line_max_height, 20f)
        minHeight = typedArray.getDimension(R.styleable.WaveView_wave_line_min_height, 5f)
        maxValue = typedArray.getInt(R.styleable.WaveView_wave_line_max_value, 60)
        timeInterval = typedArray.getInt(R.styleable.WaveView_wave_line_time_interval, 50)
        typedArray.recycle()
        paint = Paint()
        paint?.color = lineColor
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var lineCount = width / (lineWidth + lineSpace).toInt()
        if (lineCount == 0) return
        when {
            !::voiceArray.isInitialized || voiceArray == null -> {
                voiceArray = IntArray(lineCount) { 0 }
            }
            voiceArray.size < lineCount -> {
                var tempArray = IntArray(lineCount) { 0 }
                System.arraycopy(
                    voiceArray,
                    0,
                    tempArray,
                    lineCount - voiceArray.size,
                    voiceArray.size
                )
                voiceArray = tempArray
            }
            voiceArray.size > lineCount -> {
                var tempArray = IntArray(lineCount) { 0 }
                System.arraycopy(
                    voiceArray,
                    voiceArray.size - lineCount,
                    tempArray,
                    0,
                    tempArray.size
                )
            }
        }
        System.arraycopy(voiceArray, 1, voiceArray, 0, voiceArray.size - 1)
        voiceArray[voiceArray.lastIndex] = getNewVoice()
        paint?.apply {
            style = Paint.Style.FILL
            strokeWidth = lineWidth
            isAntiAlias = true
            voiceArray.forEachIndexed { index, h ->
                rectF.left = (lineWidth + lineSpace) * index
                rectF.top = height / 2.0f - h / 2.0f
                rectF.right = rectF.left + lineWidth
                rectF.bottom = rectF.top + h
                canvas?.drawRoundRect(
                    rectF,
                    lineWidth / 2,
                    lineWidth / 2,
                    paint!!
                )
            }
        }
    }

    private fun getNewVoice(): Int {
        recorder ?: return 0
        return (minHeight + (maxHeight - minHeight) * min(
            1.0, (recorder?.maxAmplitude
                ?: 0) * 2 / 32768.0
        )).toInt()
    }

    fun setRecorder(recorder: MediaRecorder) {
        this.recorder = recorder
    }
}