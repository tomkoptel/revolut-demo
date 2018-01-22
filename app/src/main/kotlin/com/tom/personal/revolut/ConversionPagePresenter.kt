package com.tom.personal.revolut

import android.content.Context
import android.net.NetworkInfo
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionPagePresenter(
    private val context: Context,
    private val conversionModel: CurrenciesViewModel
) {
    private var view: ConversionViewPage? = null
    private val disposables = CompositeDisposable()

    fun attach(view: ConversionViewPage) {
        this.view = view

        view.onInitialConversion()
            .flatMapObservable {
                conversionModel.loadConversions(it)
                    .subscribeOn(Schedulers.io())
                    .take(1)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .map { ConversionViewPage.State.Update(it) }
            .subscribeBy(onNext = ::renderState, onError = ::logError)
            .apply { disposables.add(this) }


        ReactiveNetwork.observeNetworkConnectivity(context)
            .filter(ConnectivityPredicate.hasState(NetworkInfo.State.DISCONNECTED))
            .flatMap {
                view.isEmpty()
                    .map { isEmpty ->
                        if (isEmpty) {
                            ConversionViewPage.State.ConnectionError
                        } else {
                            ConversionViewPage.State.ConnectionLost
                        }
                    }
            }
            .subscribeBy(onNext = ::renderState, onError = ::logError)
            .apply { disposables.add(this) }
    }

    fun detach() {
        disposables.clear()
        this.view = null
    }

    private fun renderState(state: ConversionViewPage.State) {
        this.view?.render(state)
    }

    private fun logError(error: Throwable) {
        Log.e("REVOLUT", "Crash in ConversionPagePresenter", error)
    }
}