package com.tom.personal.revolut

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ConversionViewPage {
    private lateinit var viewModel: CurrenciesViewModel
    private lateinit var presenter: ConversionPagePresenter
    private lateinit var adapter: ConversionAdapter

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

        viewModel = ViewModelProviders.of(this, CurrenciesViewModel.Factory)
            .get(CurrenciesViewModel::class.java)
        presenter = ConversionPagePresenter(viewModel)

        adapter = ConversionAdapter()
        list.also {
            it.layoutManager = LinearLayoutManager(this)
            it.itemAnimator = DefaultItemAnimator()
            it.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
            it.adapter = adapter
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.apply {
            putString(EXTRA_REQUESTED_CURRENCY, userCurrency)
            putDouble(EXTRA_REQUESTED_VALUE, userValue)
        }
    }

    override fun onInitialConversion() = Single.just(ConversionRequest(userCurrency, userValue))

    override fun render(state: ConversionViewPage.State) {
        when (state) {
            is ConversionViewPage.State.Update -> adapter.addAll(state.conversions)
        }
    }
}
