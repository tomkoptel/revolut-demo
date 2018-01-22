package com.tom.personal.revolut.domain

import android.net.NetworkInfo
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.tom.personal.revolut.RxSchedulerRule
import com.tom.personal.revolut.data.Rates
import com.tom.personal.revolut.data.RevolutService
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import java.util.concurrent.TimeUnit


/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/22/18
 */
class ConversionModelTest {
    private val testScheduler = TestScheduler()
    @Rule @JvmField var rxSchedulerRule = RxSchedulerRule(testScheduler)

    private val rates1: Rates = mockk(name = "first response")
    private val rates2: Rates = mockk(name = "second response")
    private val apiReturnsSuccess = mockk<RevolutService>().apply {
        val responses = listOf<Rates>(rates1, rates2).mapTo(mutableListOf()) {
            Single.just(Result.response(Response.success(it)))
        }
        every { latest(any()) } returnsMany responses
    }

    private val disConnected = Connectivity.Builder()
        .state(NetworkInfo.State.DISCONNECTED)
        .build()
    private val connected = Connectivity.Builder()
        .state(NetworkInfo.State.CONNECTED)
        .build()

    @Test
    fun should_emit_nothing_if_the_network_not_reachable() {
        val model = ConversionModel(apiReturnsSuccess) { Observable.just(disConnected) }
        val test = model.onCurrenciesChange().test()

        test.await(100, TimeUnit.MILLISECONDS)
        test.assertEmpty()
    }

    @Test
    fun should_start_emitting_values_when_network_connected() {
        val model = ConversionModel(apiReturnsSuccess) { Observable.just(connected) }

        val test = model.onCurrenciesChange().test()

        test.assertValues(rates1)
    }

    @Test
    fun should_emit_values_per_second() {
        val model = ConversionModel(apiReturnsSuccess) { Observable.just(connected) }

        val test = model.onCurrenciesChange().test()
        test.assertValue(rates1)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        test.assertValueAt(1, rates2)
    }

    @Test
    fun should_resume_requesting_api_after_network_is_back() {
        val connectionBus = PublishSubject.create<Connectivity>()
        val model = ConversionModel(apiReturnsSuccess) { connectionBus.hide() }

        val test = model.onCurrenciesChange().test()

        connectionBus.onNext(connected)
        test.assertValues(rates1)

        connectionBus.onNext(disConnected)
        connectionBus.onNext(connected)
        test.assertValueAt(1, rates2)
    }

    @Test
    fun should_not_keep_emitting_if_connection_was_dropped() {
        val connectionBus = PublishSubject.create<Connectivity>()
        val model = ConversionModel(apiReturnsSuccess) { connectionBus.hide() }

        val test = model.onCurrenciesChange().test()

        connectionBus.onNext(connected)
        test.assertValue(rates1)
        connectionBus.onNext(disConnected)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        test.assertValueCount(1)
    }

    @Test
    fun should_not_emit_if_api_returned_error() {
        val apiReturns500 = mockk<RevolutService>().apply {
            val error500 = Response.error<Rates>(500, ResponseBody.create(null, ""))
            every { latest(any()) } returns  Single.just(Result.response(error500))
        }
        val model = ConversionModel(apiReturns500) { Observable.just(connected) }

        val test = model.onCurrenciesChange().test()
        test.await(100, TimeUnit.MILLISECONDS)
        test.assertEmpty()
    }

    @Test
    fun should_not_emit_if_response_holds_null_reference() {
        val apiReturnsNull = mockk<RevolutService>().apply {
            val rates: Rates? = null
            val nullableResponse = Response.success(rates)
            every { latest(any()) } returns  Single.just(Result.response(nullableResponse))
        }
        val model = ConversionModel(apiReturnsNull) { Observable.just(connected) }

        val test = model.onCurrenciesChange().test()
        test.await(100, TimeUnit.MILLISECONDS)
        test.assertEmpty()
    }
}