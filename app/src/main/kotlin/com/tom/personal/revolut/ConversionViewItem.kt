package com.tom.personal.revolut

import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
interface ConversionViewItem {
    fun onConversionRequest(): Observable<ConversionRequest>
    fun onFocusChanges(): Observable<Boolean>
    fun render(state: State)

    sealed class State {
        class Update(val conversion: Conversion) : State()
    }
}
