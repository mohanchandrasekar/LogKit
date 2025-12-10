package com.logkit.core

import android.util.Log

/**
 * Simple Tree that writes to Android's Logcat.
 */
class LogcatTree(
    private val minPriority: Int = LogKit.VERBOSE
) : LogKit.Tree {

    override fun isLoggable(priority: Int, tag: String): Boolean {
        return priority >= minPriority
    }

    override fun log(priority: Int, tag: String, message: String, throwable: Throwable?) {
        val finalMsg = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }

        when (priority) {
            LogKit.VERBOSE -> Log.v(tag, finalMsg)
            LogKit.DEBUG   -> Log.d(tag, finalMsg)
            LogKit.INFO    -> Log.i(tag, finalMsg)
            LogKit.WARN    -> Log.w(tag, finalMsg)
            LogKit.ERROR   -> Log.e(tag, finalMsg)
            LogKit.ASSERT  -> Log.wtf(tag, finalMsg)
            else           -> Log.d(tag, finalMsg)
        }
    }
}
