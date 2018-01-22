package com.tom.personal.revolut

import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable

/**
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/21/18
 */
interface ConversionViewItem {
    /**
     * Exposes the currency this particular view associated with.
     */
    fun getCurrency(): String

    /**
     * Exposes the conversion request. E.g. user start to type in edit text.
     */
    fun onConversionRequest(): Observable<ConversionRequest>

    /**
     * Exposes focus state changes of our currency edit text.
     */
    fun onCurrencyEditTextFocusChanges(): Observable<Boolean>

    /**
     * Takes the state objects and renders it to the View
     */
    fun render(state: State)

    sealed class State {
        class Update(val value: Conversion) : State()
    }
}
