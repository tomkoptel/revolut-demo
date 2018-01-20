@file:JvmName("CurrencyConverter")
package com.tom.personal.revolut.domain

import com.tom.personal.revolut.data.Rates

/**
 * Performs related conversions on the basis of [ConversionRequest]. Creates collection of conversion result with
 * respect of requested currency.
 *
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
fun reduceRatesToConversions(rates: Rates, request: ConversionRequest): List<Conversion> {
    val currencies = rates.values.plus(rates.base to 1.0)
    currencies[request.currency]?.let { conversionRate ->
        return currencies.mapTo(mutableListOf()) { (currency, rate) ->
            val conversionResult = ((rate / conversionRate) * request.value)
            Conversion(currency, conversionResult)
        }
    }
    return emptyList()
}