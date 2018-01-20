package com.tom.personal.revolut

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()

    private lateinit var viewModel: CurrenciesViewModel
    private lateinit var adapter: ArrayAdapter<Conversion>

    private var userValue: Double = DEFAULT_INITIAL_VALUE
    private var userCurrency: String = DEFAULT_CURRENCY

    companion object {
        const val EXTRA_REQUESTED_CURRENCY = "EXTRA_REQUESTED_CURRENCY"
        const val EXTRA_REQUESTED_VALUE = "EXTRA_REQUESTED_VALUE"
        const val DEFAULT_CURRENCY = "EUR"
        const val DEFAULT_INITIAL_VALUE = 100.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            userValue = it.getDouble(EXTRA_REQUESTED_VALUE, DEFAULT_INITIAL_VALUE)
            userCurrency = it.getString(EXTRA_REQUESTED_CURRENCY, DEFAULT_CURRENCY)
        }
        setContentView(R.layout.activity_main)

        adapter = ArrayAdapter<Conversion>(this, android.R.layout.simple_list_item_1).apply {
            list.adapter = this
        }
        viewModel = ViewModelProviders.of(this, CurrenciesViewModel.Factory)
            .get(CurrenciesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        render(requestConversion(userCurrency, userValue))
        render(viewModel.onConversionChange().subscribeOn(Schedulers.io()))

        RxTextView.afterTextChangeEvents(currencyField)
            .map { currencyField.text.toString() }
            .filter { !it.isEmpty() }
            .map { it.toDouble() }
            .switchMap { requestConversion("EUR", it) }
            .let { render(it) }
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putDouble(EXTRA_REQUESTED_CURRENCY, userValue)
    }

    private fun requestConversion(currency: String, value: Double): Observable<List<Conversion>> =
        viewModel.onConversionRequest(ConversionRequest(currency, value)).subscribeOn(Schedulers.io())

    private fun render(source: Observable<List<Conversion>>) {
        source
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items -> adapter.let { it.clear(); it.addAll(items) } }, { Log.e("REVOLUT", "OPS!", it) })
            .apply { disposables.add(this) }
    }
}
