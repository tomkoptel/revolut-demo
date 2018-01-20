package com.tom.personal.revolut.domain

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
data class Conversion(val currency: String, private val result: Double) {
    fun toHumanFormat(): String = "%.2f".format(result)
}