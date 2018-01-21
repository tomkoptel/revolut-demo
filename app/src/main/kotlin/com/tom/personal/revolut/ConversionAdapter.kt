package com.tom.personal.revolut

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.simple_item.view.*

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionAdapter : RecyclerView.Adapter<ConversionAdapter.ViewItemHolder>() {
    private val presentersController = ConversionItemPresenter.Controller()
    private var items: List<Conversion> = emptyList()

    fun addAll(items: List<Conversion>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewItemHolder?, position: Int) {
        items[position].let {
            holder?.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.simple_item, parent, false)
        return ConversionAdapter.ViewItemHolder(root, presentersController.create(parent.context))
    }

    override fun getItemCount(): Int = items.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        presentersController.release()
    }

    class ViewItemHolder(
        view: View,
        private val itemPresenter: ConversionItemPresenter
    ) : RecyclerView.ViewHolder(view), ConversionViewItem {
        private val txtField: TextView = view.txtField
        private val editText: EditText = view.editField
        private var currentConversion: Conversion? = null

        private val disposables = CompositeDisposable()
        private val focusEvents: PublishSubject<Boolean> = PublishSubject.create()

        fun bind(conversion: Conversion) {
            currentConversion = conversion
            updateUI(conversion)
            itemPresenter.reattach(this, conversion.currency)

            RxView.focusChanges(editText).subscribe(focusEvents::onNext)
                .apply { disposables.add(this) }
        }

        override fun onFocusChanges(): Observable<Boolean> {
            return focusEvents
        }

        override fun onConversionRequest(): Observable<ConversionRequest> {
            val conversion = currentConversion
            return if (conversion == null) {
                Observable.empty<ConversionRequest>()
            } else {
                RxTextView.textChanges(editText)
                    .map { it.toString() }
                    .filter { !it.isEmpty() }
                    .map { it.toDouble() }
                    .map { ConversionRequest(conversion.currency, it) }
            }
        }

        override fun render(state: ConversionViewItem.State) {
            when (state) {
                is ConversionViewItem.State.Update -> {
                    updateUI(state.conversion)
                }
            }
        }

        private fun updateUI(conversion: Conversion) {
            txtField.text = conversion.currency
            editText.setText(conversion.toHumanFormat())
        }
    }
}