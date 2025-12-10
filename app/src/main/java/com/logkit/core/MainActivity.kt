package com.logkit.core

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val TAG = "LogKitDemo"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        testNormalLogs()
        testSensitiveLogs()
    }

    private fun testNormalLogs() {
        LogKit.v(TAG) { "Verbose log – should appear only in DEBUG build" }
        LogKit.d(TAG) { "Debug log – should appear only in DEBUG build" }
        LogKit.i(TAG) { "Info log – should appear in DEBUG + RELEASE" }
        LogKit.w(TAG) { "Warning log – should appear in DEBUG + RELEASE" }
        LogKit.e(TAG) { "Error log – should appear in DEBUG + RELEASE" }
    }

    private fun testSensitiveLogs() {
        val vin = "W0L000000000VIN1234"

        LogKit.i(TAG) { "Car VIN=$vin" }
        LogKit.d(TAG) { "Debug: sending token=ABC123XYZ for telemetry" }
    }
}