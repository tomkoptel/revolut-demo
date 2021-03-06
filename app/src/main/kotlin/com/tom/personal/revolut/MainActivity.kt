package com.tom.personal.revolut

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.tom.personal.revolut.domain.ConversionRequest
import com.tom.personal.revolut.ext.logError
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ConversionViewPage {
    private lateinit var viewModel: CurrenciesViewModel
    private lateinit var presenter: ConversionPagePresenter
    private lateinit var adapter: ConversionAdapter
    private lateinit var swapDisposable: Disposable

    private var userValue: Double = DEFAULT_INITIAL_VALUE
    private var userCurrency: String = DEFAULT_CURRENCY

    /**
     * State management related routine. If the activity dies out because of system killing the app, we at least can
     * restore the latest chosen by user conversion.
     */
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

        viewModel = CurrenciesViewModel.create(this)
        presenter = ConversionPagePresenter(this, viewModel)

        adapter = ConversionAdapter()
        list.also {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(this)
            it.itemAnimator = DefaultItemAnimator()
            it.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
            it.adapter = adapter
        }

        swapDisposable = adapter.onViewClicked(list)
            .map { it.position }
            .subscribeBy(
                onNext = { adapter.swapFirstItem(it); list.scrollToPosition(0) },
                onError = ::logError
            )
    }

    override fun isEmpty() = Observable.fromCallable { adapter.itemCount == 0 }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        swapDisposable.dispose()
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

    @SuppressLint("SetTextI18n")
    override fun render(state: ConversionViewPage.State) {
        when (state) {
            is ConversionViewPage.State.Update -> {
                progressBar.visibility = View.GONE
                list.visibility = View.VISIBLE
                errorTxt.visibility = View.GONE

                adapter.addAll(state.conversions)
            }
            is ConversionViewPage.State.ConnectionError -> {
                progressBar.visibility = View.GONE
                list.visibility = View.GONE
                errorTxt.visibility = View.VISIBLE

                errorTxt.text = "Network error. Can not load data!"
            }
            is ConversionViewPage.State.ConnectionLost -> {
                Toast.makeText(this, "Ops... Connection loss!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
