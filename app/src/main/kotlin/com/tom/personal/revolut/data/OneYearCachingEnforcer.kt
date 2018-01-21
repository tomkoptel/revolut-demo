package com.tom.personal.revolut.data

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class OneYearCachingEnforcer : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val forceFromCache = CacheControl.Builder()
            .onlyIfCached()
            .build()

        val request = originRequest.newBuilder()
            .cacheControl(forceFromCache)
            .build()
        val response = chain.proceed(request)

        if (response.code() == 504) {
            val response1 = chain.proceed(originRequest)
                .newBuilder()
                .removeHeader("Cache-Control")
                .removeHeader("Pragma")
                .removeHeader("Expires")
                .addHeader("Cache-Control", "public, max-age=${TimeUnit.DAYS.toSeconds(365)}")
                .build()
            return response1
        }

        return response
    }
}