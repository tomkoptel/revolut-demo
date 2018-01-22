package com.tom.personal.revolut

import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/21/18
 */
interface ConversionViewPage {
    /**
     * Exposes the last requested by user conversion.
     */
    fun onInitialConversion(): Single<ConversionRequest>

    /**
     * Exposes state of our view. If the data was set the it will emit [false].
     */
    fun isEmpty(): Observable<Boolean>

    /**
     * Renders state of view.
     */
    fun render(state: State)

    sealed class State {
        /**
         * We need to update UI.
         */
        class Update(val conversions: List<Conversion>) : State()

        /**
         * We need show connectivity error.
         */
        object ConnectionError : State()

        /**
         * We need to notify user about connection loss.
         */
        object ConnectionLost : State()
    }
}