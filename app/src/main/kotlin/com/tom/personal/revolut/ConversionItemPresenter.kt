package com.tom.personal.revolut

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionItemPresenter(private val viewModel: CurrenciesViewModel) {
    private val disposables = CompositeDisposable()
    private var view: ConversionViewItem? = null

    companion object Factory {
        fun create(context: Context) = CurrenciesViewModel.create(context).let { ConversionItemPresenter(it) }
    }

    fun reattach(view: ConversionViewItem) {
        disposables.clear()
        this.view = view

        view.onFocusChanges()
            .switchMap { focused ->
                if (focused) {
                    view.onConversionRequest()
                } else {
                    Observable.empty()
                }
            }
            .subscribe(
                { viewModel.updateRequestedCurrency(it) },
                { logError(it) }
            )
            .apply { disposables.add(this) }

        val currency = view.getCurrency()
        view.onFocusChanges()
            .switchMap { focused ->
                if (focused) {
                    Observable.empty()
                } else {
                    viewModel.onConversionChange(currency).subscribeOn(Schedulers.io())
                }
            }
            .map { ConversionViewItem.State.Update(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ this.view?.render(it) }, { logError(it) })
            .apply { disposables.add(this) }
    }

    fun detach() {
        disposables.clear()
        view = null
    }

    private fun logError(throwable: Throwable) = Log.e("REVOLUT", "Crash in ConversionPresenter", throwable)
}