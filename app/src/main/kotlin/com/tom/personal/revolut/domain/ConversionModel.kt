package com.tom.personal.revolut.domain

import android.util.Log
import com.tom.personal.revolut.data.Rates
import com.tom.personal.revolut.data.RevolutService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class ConversionModel {
    private val api = RevolutService.Factory.create()
    private val subject = BehaviorSubject.create<Rates>().toSerialized()

    val disposable: Disposable = Observable.interval(1, TimeUnit.SECONDS)
        .switchMap { api.latest("EUR").toObservable().subscribeOn(Schedulers.io()) }
        .distinctUntilChanged()
        .subscribe(subject::onNext, { Log.e("REVOLUT", "ConversionModel crashed", it) })

    fun onCurrenciesChange(): Observable<Rates> = subject
}