package com.tom.personal.revolut.domain

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class CurrencyTable(private val currencies: Map<String, Double>) {
    /**
     * Performs related conversions on the basis of [ConversionRequest]. Creates collection of conversion result with
     * respect of requested currency.
     */
    fun convert(request: ConversionRequest): List<Conversion> {
        currencies[request.currency]?.let { conversionRate ->
            return currencies.mapTo(mutableListOf()) { (currency, rate) ->
                val conversionResult = ((rate / conversionRate) * request.value)
                Conversion(currency, conversionResult)
            }
        }
        return emptyList()
    }
}