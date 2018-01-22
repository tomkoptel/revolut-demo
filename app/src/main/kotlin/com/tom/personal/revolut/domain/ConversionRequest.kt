package com.tom.personal.revolut.domain

/**
 * Represents the request to the conversion update.
 *
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/20/18
 */
data class ConversionRequest(val currency: String, val value: Double)