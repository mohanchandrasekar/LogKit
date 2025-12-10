package com.logkit.core

object LogKit {

    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6
    const val ASSERT = 7

    @Volatile
    private var debugMode: Boolean = false

    @Volatile
    private var allowSensitiveLogs: Boolean = false

    private val trees = mutableListOf<Tree>()
    private var guard: LogGuard = DefaultLogGuard()

    interface Tree {
        fun isLoggable(priority: Int, tag: String): Boolean = true
        fun log(priority: Int, tag: String, message: String, throwable: Throwable? = null)
    }

    /**
     * Call this once in your Application.onCreate().
     *
     * @param debug typically BuildConfig.DEBUG
     * @param allowSensitive typically BuildConfig.ALLOW_SENSITIVE_LOGS
     * @param newTrees one or more output targets (LogcatTree, file tree, etc.)
     */
    @JvmStatic
    fun init(
        debug: Boolean,
        allowSensitive: Boolean,
        vararg newTrees: Tree
    ) {
        debugMode = debug
        allowSensitiveLogs = allowSensitive
        trees.clear()
        trees.addAll(newTrees)
    }

    internal fun isDebugBuild(): Boolean = debugMode
    internal fun isSensitiveAllowed(): Boolean = allowSensitiveLogs

    @JvmStatic
    fun setLogGuard(newGuard: LogGuard) {
        guard = newGuard
    }

    // ------------- Simple String APIs (Java + Kotlin) -------------

    @JvmStatic
    fun v(tag: String, msg: String) = log(VERBOSE, tag, msg, null)

    @JvmStatic
    fun d(tag: String, msg: String) = log(DEBUG, tag, msg, null)

    @JvmStatic
    fun i(tag: String, msg: String) = log(INFO, tag, msg, null)

    @JvmStatic
    fun w(tag: String, msg: String) = log(WARN, tag, msg, null)

    @JvmStatic
    fun e(tag: String, msg: String) = log(ERROR, tag, msg, null)

    @JvmStatic
    fun e(tag: String, t: Throwable, msg: String) = log(ERROR, tag, msg, t)

    // Java-friendly format overloads

    @JvmStatic
    fun d(tag: String, format: String, vararg args: Any?) =
        log(DEBUG, tag, formatMessage(format, args), null)

    @JvmStatic
    fun i(tag: String, format: String, vararg args: Any?) =
        log(INFO, tag, formatMessage(format, args), null)

    @JvmStatic
    fun w(tag: String, format: String, vararg args: Any?) =
        log(WARN, tag, formatMessage(format, args), null)

    @JvmStatic
    fun e(tag: String, format: String, vararg args: Any?) =
        log(ERROR, tag, formatMessage(format, args), null)

    @JvmStatic
    fun e(tag: String, t: Throwable, format: String, vararg args: Any?) =
        log(ERROR, tag, formatMessage(format, args), t)

    // ------------- Kotlin lambda APIs (nice sugar) -------------

    @JvmStatic
    fun v(tag: String, msg: () -> String) {
        logLazy(VERBOSE, tag, msg, null)
    }

    @JvmStatic
    fun d(tag: String, msg: () -> String) {
        logLazy(DEBUG, tag, msg, null)
    }

    @JvmStatic
    fun i(tag: String, msg: () -> String) {
        logLazy(INFO, tag, msg, null)
    }

    @JvmStatic
    fun w(tag: String, msg: () -> String) {
        logLazy(WARN, tag, msg, null)
    }

    @JvmStatic
    fun e(tag: String, msg: () -> String) {
        logLazy(ERROR, tag, msg, null)
    }

    @JvmStatic
    fun e(tag: String, t: Throwable, msg: () -> String) {
        logLazy(ERROR, tag, msg, t)
    }

    // ------------- Internal helpers -------------

    private fun formatMessage(format: String?, args: Array<out Any?>?): String {
        if (format == null) return ""
        if (args == null || args.isEmpty()) return format
        return try {
            String.format(java.util.Locale.US, format, *args)
        } catch (e: Exception) {
            "$format (format error: ${e.message})"
        }
    }

    private fun log(priority: Int, tag: String, message: String?, throwable: Throwable?) {
        if (trees.isEmpty()) return

        // 🔒 Runtime log policy:
        // Debug build  -> allow all levels
        // Release build -> drop VERBOSE + DEBUG
        if (!debugMode && (priority == VERBOSE || priority == DEBUG)) {
            return
        }

        var msg = message ?: ""

        // clickable location in debug builds (Logcat stack-frame style)
        if (debugMode) {
            val loc = buildLocation()
            if (loc.isNotEmpty()) {
                msg = "$loc\n$msg"
            }
        }

        // sensitive-data guard
        val filtered = guard.filter(priority, tag, msg) ?: return

        trees.forEach { tree ->
            if (tree.isLoggable(priority, tag)) {
                tree.log(priority, tag, filtered, throwable)
            }
        }
    }

    private fun logLazy(
        priority: Int,
        tag: String,
        provider: () -> String,
        throwable: Throwable?
    ) {
        if (trees.isEmpty()) return

        // Same policy here as in log()
        if (!debugMode && (priority == VERBOSE || priority == DEBUG)) {
            return
        }

        if (trees.none { it.isLoggable(priority, tag) }) return
        log(priority, tag, provider(), throwable)
    }

    /**
     * Build a fake stack-frame line like:
     *   at com.example.MyClass.method(MyClass.kt:123)
     *
     * Logcat renders this as a clickable link to your source.
     */
    private fun buildLocation(): String {
        val stack = Throwable().stackTrace
        val index = 5 // tune if needed depending on call depth
        if (stack.size <= index) return ""
        val e = stack[index]
        val className = e.className
        val method = e.methodName
        val file = e.fileName ?: "UnknownFile"
        val line = e.lineNumber
        return "at $className.$method($file:$line)"
    }
}
