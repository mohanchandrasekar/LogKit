package com.logkit.core

/**
 * Intercepts log messages before they are written.
 *
 * Return:
 * - null  -> block this log completely
 * - value -> sanitized message to log
 */
interface LogGuard {
    fun filter(priority: Int, tag: String, message: String): String?
}
