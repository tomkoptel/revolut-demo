package com.tom.personal.revolut.data

import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import com.tom.personal.revolut.domain.CurrencyTable
import org.amshove.kluent.shouldContain
import org.junit.Test

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class CurrencyTableTest {
    private val table = CurrencyTable(
        mapOf(
            "EUR" to 1.0,
            "UAH" to 35.43,
            "CZK" to 25.39,
            "RUB" to 69.32
        )
    )

    @Test
    fun should_produce_conversions_on_non_base_currency() {
        val conversions = performConversion("UAH", 100.0)

        conversions shouldContain ("EUR" to "2.82")
        conversions shouldContain ("UAH" to "100.00")
        conversions shouldContain ("CZK" to "71.66")
        conversions shouldContain ("RUB" to "195.65")
    }

    @Test
    fun should_produce_conversions_on_base_currency() {
        val conversions = performConversion("EUR", 100.0)

        conversions shouldContain ("EUR" to "100.00")
        conversions shouldContain ("UAH" to "3543.00")
        conversions shouldContain ("CZK" to "2539.00")
        conversions shouldContain ("RUB" to "6932.00")
    }

    private fun performConversion(requestedCurrency: String, value: Double) =
        toFriendlyFormat(table.convert(ConversionRequest(requestedCurrency, value)))

    private fun toFriendlyFormat(conversions: List<Conversion>) =
        conversions.map { conversion -> conversion.currency to conversion.toHumanFormat() }
}