package com.tom.personal.revolut.data

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/20/18
 */
class RevolutServiceTest {
    private val _2018_01_19 = Calendar.getInstance().run {
        set(Calendar.YEAR, 2018)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 19)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        time
    }
    @Rule @JvmField val mockWebServer = MockWebServer()

    private lateinit var service: RevolutService

    @Before
    fun setUp() {
        val mockUrl = mockWebServer.url("/").toString()
        service = RevolutService.Factory.create(mockUrl)
    }

    @Test
    fun test_end2end_happy_path() {
        mockWebServer.enqueue(happyResponse())

        service.latest("EUR").test().let {
            it.awaitTerminalEvent(1, TimeUnit.SECONDS)
            it.assertValue {
                with(it) {
                    date shouldEqual _2018_01_19
                    base shouldEqual "EUR"
                    values shouldNotBe emptyMap<String, Double>()
                }
                true
            }
        }
    }

    private fun happyResponse() = MockResponse().apply {
        body = fileToBytes(resource("payload.json"))
        setResponseCode(200)
    }

    private fun resource(name: String) =
        javaClass.classLoader.getResource(name).run {
            File(this!!.path)
        }

    private fun fileToBytes(file: File): Buffer = Buffer().apply {
        writeAll(Okio.source(file))
    }
}