package com.tom.personal.revolut.data

import com.squareup.moshi.Json

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
data class Country(@Json(name = "Response") val data: List<CountryData>) {
    val flagUrl: String
        get() {
            // FIXME: seems to be not safe, but for the sake of simplicity we ignore it
            val countryData = data.firstOrNull()!!
            return countryData.flagUrl
        }
}

data class CountryData(@Json(name = "FlagPng") val flagUrl: String)
