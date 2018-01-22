package com.tom.personal.revolut

import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
interface ConversionViewPage {
    fun onInitialConversion(): Single<ConversionRequest>
    fun isEmpty(): Observable<Boolean>
    fun render(state: State)

    sealed class State {
        class Update(val conversions: List<Conversion>) : State()
        object ConnectionError : State()
        object ConnectionLost : State()
    }
}