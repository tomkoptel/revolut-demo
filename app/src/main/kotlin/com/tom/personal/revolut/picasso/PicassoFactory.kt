package com.tom.personal.revolut.picasso

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import com.tom.personal.revolut.BuildConfig
import com.tom.personal.revolut.data.CountryApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
object PicassoFactory {
    fun create(context: Context): Picasso {
        val cacheSize = 30 * 1024 * 1024 // 30 MiB
        val cacheDir = File(context.cacheDir, "picasso-cache").apply { mkdir() }
        val cache = Cache(cacheDir, cacheSize.toLong())

        val client = OkHttpClient.Builder()
            .cache(cache)
            .build()

        val countryService = CountryApiService.Factory.create(context)
        val delegateDownloader = OkHttp3Downloader(client)
        val flagDownloader = FlagDownloader(delegateDownloader, countryService)

        val picassoBuilder = Picasso.Builder(context)
            .downloader(flagDownloader)
        if (BuildConfig.DEBUG) {
            picassoBuilder
                .loggingEnabled(true)
                .indicatorsEnabled(true)
                .listener { _, _, exception -> Log.e("REVOLUT", "Picasso crashed", exception) }
        }

        return picassoBuilder.build()
    }

    private class FlagDownloader(
        private val downloader: Downloader,
        private val countryService: CountryApiService
    ) : Downloader {
        override fun load(uri: Uri, networkPolicy: Int): Downloader.Response {
            if (uri.host == "flag") {
                val currencyCode = uri.pathSegments.first().toLowerCase()
                val call = countryService.getCountryMetadata(currencyCode)
                val response = call.execute()
                val responseCode = response.code()
                if (responseCode >= 300) {
                    throw Downloader.ResponseException(
                        "$responseCode ${response.message()}",
                        networkPolicy,
                        responseCode
                    )
                }
                val country = response.body()!!
                return downloader.load(Uri.parse(country.flagUrl), networkPolicy)
            }

            return downloader.load(uri, networkPolicy)
        }

        override fun shutdown() {
            downloader.shutdown()
        }
    }
}