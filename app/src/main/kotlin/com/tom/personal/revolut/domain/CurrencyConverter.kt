@file:JvmName("CurrencyConverter")

package com.tom.personal.revolut.domain

import com.tom.personal.revolut.data.Rates

/**
 * Performs related conversions on the basis of [ConversionRequest]. Creates collection of conversion result with
 * respect of requested currency.
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

/**
 * Performs related conversions on the basis of [ConversionRequest]. Produces current valid conversion rate for
 * specific currency.
 */
fun reduceRatesToConversion(rates: Rates, request: ConversionRequest, requestedCurrency: String): Conversion {
    val currencies = rates.values.plus(rates.base to 1.0)
    currencies[request.currency]?.let { conversionRate ->
        currencies[requestedCurrency]?.let { rate ->
            val conversionResult = ((rate / conversionRate) * request.value)
            return Conversion(requestedCurrency, conversionResult)
        }
    }
    return Conversion.NULL
}