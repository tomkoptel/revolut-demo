package com.tom.personal.revolut.domain

import android.content.Context
import android.net.NetworkInfo
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.tom.personal.revolut.data.Rates
import com.tom.personal.revolut.data.RevolutService
import com.tom.personal.revolut.ext.logError
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * The core logic of whole implementation. It keeps emitting items until we discover disconnected. All API errors
 * ignored and consumed. The stream either emits [Rates] objects or waits until the 'healthy' response comes back.
 *
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/20/18
 */
class ConversionModel(
    private val api: RevolutService,
    connectivityStream: () -> Observable<Connectivity>
) {
    companion object {
        fun create(context: Context) = ConversionModel(RevolutService.Factory.create()) {
            ReactiveNetwork.observeNetworkConnectivity(context)
        }
    }

    // We perform multicasting with a help of [BehaviorSubject] that will replay the latest seen value.
    private val subject = BehaviorSubject.create<Rates>().toSerialized()

    val disposable: Disposable

    init {
        // Network connectivity drives emissions.
        disposable = connectivityStream.invoke()
            .subscribeOn(Schedulers.io())
            .switchMap({
                val connected = it.state == NetworkInfo.State.CONNECTED
                if (connected) {
                    // We need to defer API observable creation as we combine interval() and startWith() operators
                    val apiCall = Observable.defer {
                        api.latest("EUR").toObservable()
                            .subscribeOn(Schedulers.io())
                            .flatMap {
                                if (!it.isError) {
                                    val response = it.response()
                                    if (response != null) {
                                        val rates = response.body()
                                        if (rates != null) {
                                            return@flatMap Observable.just(rates)
                                        }
                                    }
                                }
                                return@flatMap Observable.empty<Rates>()
                            }
                    }
                    Observable.interval(1, TimeUnit.SECONDS)
                        .switchMap { apiCall }
                        .startWith(apiCall)
                } else {
                    Observable.empty<Rates>()
                }
            })
            .subscribe(subject::onNext, ::logError)
    }

    fun onCurrenciesChange(): Observable<Rates> = subject.hide()
}