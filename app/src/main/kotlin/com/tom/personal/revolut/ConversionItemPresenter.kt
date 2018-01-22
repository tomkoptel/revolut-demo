package com.tom.personal.revolut

import android.content.Context
import com.tom.personal.revolut.ext.logError
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * @author Tom Koptel: tom.koptel@gmail.com
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

        view.onCurrencyEditTextFocusChanges()
            .switchMap { focused ->
                if (focused) {
                    view.onConversionRequest()
                } else {
                    Observable.empty()
                }
            }
            .subscribeBy(
                onNext = viewModel::updateRequestedCurrency,
                onError = ::logError
            )
            .apply { disposables.add(this) }

        val currency = view.getCurrency()
        view.onCurrencyEditTextFocusChanges()
            .switchMap { focused ->
                if (focused) {
                    Observable.empty()
                } else {
                    viewModel.onConversionChange(currency)
                        .subscribeOn(Schedulers.io())
                }
            }
            .map { ConversionViewItem.State.Update(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = ::renderState,
                onError = ::logError
            )
            .apply { disposables.add(this) }
    }

    fun detach() {
        disposables.clear()
        view = null
    }

    private fun renderState(state: ConversionViewItem.State) {
        this.view?.render(state)
    }
}