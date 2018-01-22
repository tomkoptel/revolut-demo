package com.tom.personal.revolut.domain

/**
 * Represents the conversion Unit association with the latest calculated value.
 *
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/20/18
 */
data class Conversion(val currency: String, private val result: Double) {
    fun toHumanFormat(): String = "%.2f".format(result)

    override fun toString(): String {
        return "Conversion(currency=$currency, result=${toHumanFormat()})"
    }

    companion object {
        val NULL = Conversion("", 0.0)
    }
}