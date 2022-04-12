package zyz.hero.record_kit.view

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import zyz.hero.record_kit.R
import zyz.hero.record_kit.RecordState
import java.io.File

/**
 * @author yongzhen_zou@163.com
 * @date 2022/4/1 9:38 下午
 */
class RecordLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), AudioManager.OnAudioFocusChangeListener {
    var recordingText: String = "松手发送，上划取消"
    var cancelText: String? = "松开取消"
    var recordingTextColor: Int = Color.parseColor("#979797")
    var cancelTextColor: Int = Color.parseColor("#F34848")
    var maxLength: Int = 60 //秒
    var waveView: VoiceWaveView? = null
    private var progressView: TimeProgressView? = null
    private var stateTextView: TextView? = null
    private var voiceButtonLayout: ConstraintLayout? = null
    private var volumeImg: ImageView? = null
    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var audioManager: AudioManager? = null
    var startRecordTimeMillis: Long = 0
    private var onRecordComplete: ((recordFile: File?, recordTimeMillis: Long) -> Unit)? = null
    var recordState: Int = RecordState.RECORDING
        set(value) {
            when (value) {
                RecordState.START -> {
                    setTextCancel(false)
                    startRecorder()
                    voiceButtonLayout?.setBackgroundResource(R.drawable.bg_voice_button_layout)
                    volumeImg?.setImageResource(R.drawable.icon_volume)
                }
                RecordState.RECORDING -> {
                    setTextCancel(false)
                    voiceButtonLayout?.setBackgroundResource(R.drawable.bg_voice_button_layout)
                    volumeImg?.setImageResource(R.drawable.icon_volume)
                }
                RecordState.CANCEL -> {
                    setTextCancel(true)
                    voiceButtonLayout?.setBackgroundResource(R.drawable.bg_voice_button_layout_cancel)
                    volumeImg?.setImageResource(R.drawable.icon_volume_cancel)
                }
                RecordState.RELEASE -> {
                    stopRecord(field == RecordState.CANCEL)
                }
            }
            waveView?.recordState = value
            progressView?.recordState = value
            field = value
        }

    private fun stopRecord(cancel: Boolean) {
        try {
            visibility = View.GONE
            audioManager?.abandonAudioFocus(this)
            recorder?.setOnErrorListener(null)
            recorder?.setPreviewDisplay(null)
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
            if (cancel) {
                recordFile?.delete()
            } else {
                onRecordComplete?.invoke(
                    recordFile,
                    System.currentTimeMillis() - startRecordTimeMillis
                )
            }
        } catch (e: Exception) {
            onRecordComplete?.invoke(recordFile, 0)
            e.printStackTrace()
        }

    }

    fun setOnRecordComplete(onRecordComplete: (recordFile: File?, recordTimeMillis: Long) -> Unit) {
        this.onRecordComplete = onRecordComplete
    }

    private fun startRecorder() {
        try {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            );
            audioManager?.mode = AudioManager.MODE_NORMAL
            recorder = MediaRecorder()
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder?.setAudioChannels(1) //1
            recorder?.setAudioSamplingRate(44100)
            recorder?.setAudioEncodingBitRate(96000) //64
            recorder?.setOutputFile(recordFile?.absolutePath)
            recorder?.setMaxDuration(maxLength * 1000)
            waveView?.setRecorder(recorder!!)
            recorder?.prepare()
            recorder?.start()
            startRecordTimeMillis = System.currentTimeMillis()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_record, this)
        waveView = findViewById(R.id.waveView)
        stateTextView = findViewById(R.id.stateText)
        progressView = findViewById(R.id.progressView)
        voiceButtonLayout = findViewById(R.id.voiceButtonLayout)
        volumeImg = findViewById(R.id.volumeImg)
        var typeArray = context.obtainStyledAttributes(attrs, R.styleable.RecordLayout)
        recordingText = typeArray.getString(R.styleable.RecordLayout_record_layout_recording_text)
            ?: "松手发送，上划取消"
        cancelText =
            typeArray.getString(R.styleable.RecordLayout_record_layout_cancel_text) ?: "松开取消"
        recordingTextColor = typeArray.getColor(
            R.styleable.RecordLayout_record_layout_recording_text_color,
            Color.parseColor("#979797")
        )
        cancelTextColor = typeArray.getColor(
            R.styleable.RecordLayout_record_layout_cancel_text_color,
            Color.parseColor("#F34848")
        )
        maxLength = typeArray.getInt(R.styleable.RecordLayout_record_layout_max_time_second, 60)
        progressView?.maxValue = maxLength
        progressView?.onRecordEnd = {
            stopRecord(false)
        }
        typeArray.recycle()
    }
    fun setFile(file: File) {
        this.recordFile = file
    }

    private fun setTextCancel(cancel: Boolean) {
        if (cancel) {
            stateTextView?.text = cancelText
            stateTextView?.setTextColor(cancelTextColor)
        } else {
            stateTextView?.text = recordingText
            stateTextView?.setTextColor(recordingTextColor)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {

    }

    var location: IntArray = IntArray(2)
    fun delegate(v: View?, event: MotionEvent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermissions()) {
            return
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                vibrate()
                visibility = View.VISIBLE
                recordState = RecordState.START

            }
            MotionEvent.ACTION_MOVE -> {
                if (visibility!=View.VISIBLE){
                    return
                }
                voiceButtonLayout?.getLocationOnScreen(location)
                if (event.rawX.toInt() in location[0]..location[0] + voiceButtonLayout!!.width && event.rawY.toInt() > location[1]) {
                    recordState = RecordState.RECORDING
                } else {
                    recordState = RecordState.CANCEL
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (visibility!=View.VISIBLE){
                    return
                }
                recordState = RecordState.RELEASE
            }
            MotionEvent.ACTION_UP -> {
                if (visibility!=View.VISIBLE){
                    return
                }
                recordState = RecordState.RELEASE

            }
            else -> {}
        }
    }

    var onRequestPermissions: (() -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasPermissions(): Boolean {
        if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
        ) {
            onRequestPermissions?.invoke()
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun vibrate() {
        var vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(50)
    }
}