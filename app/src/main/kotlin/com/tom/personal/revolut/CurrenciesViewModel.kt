package com.tom.personal.revolut

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.tom.personal.revolut.data.Rates
import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionModel
import com.tom.personal.revolut.domain.ConversionRequest
import com.tom.personal.revolut.domain.reduceRatesToConversions
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class CurrenciesViewModel : ViewModel() {
    private val model = ConversionModel()
    private var latestRequest = BehaviorSubject.create<ConversionRequest>().toSerialized()

    fun updateRequestedCurrency(request: ConversionRequest) {
        latestRequest.onNext(request)
    }

    fun onConversionChange(currency: String): Observable<Conversion> {
        return Observable.combineLatest(model.onCurrenciesChange(), latestRequest, BiFunction<Rates, ConversionRequest,
                List<Conversion>> { rates, request ->
            reduceRatesToConversions(rates, request)
        })
            .map { conversions -> conversions.find { it.currency == currency } ?: Conversion.NULL }
            .skipWhile { it == Conversion.NULL }
    }

    fun loadConversions(request: ConversionRequest): Observable<List<Conversion>> {
        latestRequest.onNext(request)

        return model.onCurrenciesChange()
            .take(1)
            .map { reduceRatesToConversions(it, request) }
    }

    override fun onCleared() = model.disposable.dispose()

    object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrenciesViewModel::class.java)) {
                return CurrenciesViewModel() as T
            }
            throw IllegalArgumentException("Unknown $modelClass class ")
        }
    }
}