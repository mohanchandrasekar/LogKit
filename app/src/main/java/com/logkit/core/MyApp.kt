package com.logkit.core

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        LogKit.init(
            debug = BuildConfig.DEBUG,
            allowSensitive = BuildConfig.ALLOW_SENSITIVE_LOGS,
            // Tree accepts all levels; LogKit itself drops V/D in release
            LogcatTree(minPriority = LogKit.VERBOSE)
        )
    }
}
