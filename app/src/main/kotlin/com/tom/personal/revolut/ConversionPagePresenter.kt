package com.tom.personal.revolut

import android.content.Context
import android.net.NetworkInfo
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.tom.personal.revolut.ext.logError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * The whole page wide presenter. The responsibility of one to control page global events:
 * * load first collection of conversion items
 * * show network related errors
 *
 * @author Tom Koptel: tom.koptel@gmail.com
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

        // Load items on the page
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


        // If network state changes we make sure to let user know
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
}