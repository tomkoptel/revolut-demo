package com.tom.personal.revolut.ext

import android.util.Log

private const val APP_TAG = "REVOLUT"

fun logError(error: Throwable) {
    Log.e(APP_TAG, line(), error)
}

inline fun line() =
    Thread.currentThread().stackTrace
        .first { it.fileName.endsWith(".kt") }
        .let { stack -> "${stack.fileName.removeSuffix(".kt")}::${stack.methodName}:${stack.lineNumber}" }
