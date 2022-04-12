package com.example.recordlib

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import zyz.hero.record_kit.view.RecordLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var textView = findViewById<TextView>(R.id.touchView)
        var recordLayout = findViewById<RecordLayout>(R.id.recordLayout)
        recordLayout.onRequestPermissions = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO), 123)
            }
        }
        recordLayout.setOnRecordComplete { recordFile, recordTimeMillis ->
            //
            if (recordTimeMillis < 1000) {
                if (recordFile?.exists() == true) {
                    recordFile?.delete()
                }
                Toast.makeText(this, "说话时间太短", Toast.LENGTH_SHORT).show()
            } else {

            }
        }
        textView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {

                }
                MotionEvent.ACTION_DOWN -> {
                    recordLayout.setFile(FileUtils.getVoiceFile(this))

                }
                else->{

                }
            }
            recordLayout.delegate(v,event)
           return@setOnTouchListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            PackageManager.PERMISSION_GRANTED != checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO), 123)
            return false
        }
        return true
    }

}