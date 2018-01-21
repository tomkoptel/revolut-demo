package com.tom.personal.revolut

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionPagePresenter(private val conversionModel: CurrenciesViewModel) {
    private var view: ConversionViewPage? = null
    private val disposables = CompositeDisposable()

    fun attach(view: ConversionViewPage) {
        this.view = view
        view.onInitialConversion()
            .flatMapObservable {
                conversionModel.loadConversions(it).subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    this.view?.render(ConversionViewPage.State.Update(it))
                },
                { Log.e("REVOLUT", "Crash in ConversionPagePresenter", it) }
            ).apply { disposables.add(this) }
    }

    fun detach() {
        disposables.clear()
        this.view = null
    }
}