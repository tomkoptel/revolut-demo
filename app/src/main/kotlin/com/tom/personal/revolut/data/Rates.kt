package com.tom.personal.revolut.data

import com.squareup.moshi.Json
import java.util.*

/**
 * Data class that represent mapping of response from Revolut currency API.
 *
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/20/18
 */
data class Rates(
    val base: String,
    val date: Date,
    @Json(name = "rates") val values: Map<String, Double>
)