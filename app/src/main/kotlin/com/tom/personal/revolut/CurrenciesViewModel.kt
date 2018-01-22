package com.tom.personal.revolut

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionModel
import com.tom.personal.revolut.domain.ConversionRequest
import com.tom.personal.revolut.domain.reduceRatesToConversions
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class CurrenciesViewModel(appContext: Context) : ViewModel() {
    private val model = ConversionModel.create(appContext)
    private var latestRequest = BehaviorSubject.create<ConversionRequest>().toSerialized()

    fun updateRequestedCurrency(request: ConversionRequest) {
        latestRequest.onNext(request)
    }

    fun onConversionChange(currency: String): Observable<Conversion> {
        return Observables.combineLatest(model.onCurrenciesChange(), latestRequest, { rates, request ->
            reduceRatesToConversions(rates, request)
        })
            .map { conversions -> conversions.find { it.currency == currency } ?: Conversion.NULL }
            .skipWhile { it == Conversion.NULL }
    }

    fun loadConversions(request: ConversionRequest): Observable<List<Conversion>> {
        latestRequest.onNext(request)

        return model.onCurrenciesChange().map { reduceRatesToConversions(it, request) }
    }

    override fun onCleared() = model.disposable.dispose()

    companion object {
        fun create(context: Context) =
            ViewModelProviders.of(context as AppCompatActivity, CurrenciesViewModel.Factory(context))
                .get(CurrenciesViewModel::class.java)
    }

    private class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrenciesViewModel::class.java)) {
                return CurrenciesViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown $modelClass class ")
        }
    }
}