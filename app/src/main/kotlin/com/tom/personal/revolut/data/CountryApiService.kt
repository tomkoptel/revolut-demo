package com.tom.personal.revolut.data

import android.content.Context
import com.ncornette.cache.OkCacheControl
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/21/18
 */
interface CountryApiService {
    @GET("/v1/Country/getCountries")
    fun getCountryMetadata(@Query(value = "pCurrencyCode") currencyCode: String): Call<Country>

    /**
     * Isolates logic of creating Retrofit service. We are trying to cache as much data as possible.
     */
    object Factory {
        fun create(context: Context, baseUrl: String = "http://countryapi.gear.host/"): CountryApiService {
            val moshiConverter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .let { MoshiConverterFactory.create(it) }

            val cacheSize = 10 * 1024 * 1024 // 20 MiB
            val cacheDir = File(context.cacheDir, "country").apply { mkdir() }
            val cache = Cache(cacheDir, cacheSize.toLong())

            val client = OkCacheControl.on(OkHttpClient.Builder())
                .overrideServerCachePolicy(30, TimeUnit.MINUTES)
                .apply()
                .cache(cache)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(moshiConverter)
                .client(client)
                .build()

            return retrofit.create(CountryApiService::class.java)
        }
    }
}