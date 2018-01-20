package com.tom.personal.revolut.data

import io.reactivex.Single

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class RevolutApi(private val service: RevolutService) {
    companion object Factory {
        fun create() = RevolutApi(RevolutService.Factory.create())
    }

    fun getLatest(baseCurrency: String): Single<RatesResponse> =
        service.latest(baseCurrency).map { RatesResponse(baseCurrency, it) }
}