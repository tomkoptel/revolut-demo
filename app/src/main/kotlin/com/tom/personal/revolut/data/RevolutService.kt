package com.tom.personal.revolut.data

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
interface RevolutService {
    @GET("/latest")
    fun latest(@Query("base") currency: String): Single<Result<Rates>>

    object Factory {
        fun create(baseUrl: String = "https://revolut.duckdns.org/"): RevolutService {
            val moshiConverter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .build()
                .let { MoshiConverterFactory.create(it) }
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(moshiConverter)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return retrofit.create(RevolutService::class.java)
        }
    }
}