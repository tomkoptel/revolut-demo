@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("RxLogging")

package com.tom.personal.revolut.ext

import android.util.Log
import io.reactivex.*

/**
 * The code is not owned by me. See [the source](https://proandroiddev.com/briefly-about-rxjava-logging-20308b013e6d)
 */
inline fun <reified T> printEvent(tag: String, success: T?, error: Throwable?) =
    when {
        success == null && error == null -> Log.d(tag, "Complete") /* Only with Maybe */
        success != null -> Log.d(tag, "Success $success")
        error != null -> Log.d(tag, "Error $error")
        else -> -1 /* Cannot happen*/
    }

inline fun printEvent(tag: String, error: Throwable?) =
    when {
        error != null -> Log.d(tag, "Error $error")
        else -> Log.d(tag, "Complete")
    }

inline fun <reified T> Single<T>.log(): Single<T> {
    val tag = line()
    return doOnEvent { success, error -> printEvent(tag, success, error) }
        .doOnSubscribe { Log.d(tag, "Subscribe") }
        .doOnDispose { Log.d(tag, "Dispose") }
}

inline fun <reified T> Maybe<T>.log(): Maybe<T> {
    val tag = line()
    return doOnEvent { success, error -> printEvent(tag, success, error) }
        .doOnSubscribe { Log.d(tag, "Subscribe") }
        .doOnDispose { Log.d(tag, "Dispose") }
}

inline fun Completable.log(): Completable {
    val tag = line()
    return doOnEvent { printEvent(tag, it) }
        .doOnSubscribe { Log.d(tag, "Subscribe") }
        .doOnDispose { Log.d(tag, "Dispose") }
}

inline fun <reified T> Observable<T>.log(): Observable<T> {
    val line = line()
    return doOnEach { Log.d(line, "Each $it") }
        .doOnSubscribe { Log.d(line, "Subscribe") }
        .doOnDispose { Log.d(line, "Dispose") }
}

inline fun <reified T> Flowable<T>.log(): Flowable<T> {
    val line = line()
    return doOnEach { Log.d(line, "Each $it") }
        .doOnSubscribe { Log.d(line, "Subscribe") }
        .doOnCancel { Log.d(line, "Cancel") }
}