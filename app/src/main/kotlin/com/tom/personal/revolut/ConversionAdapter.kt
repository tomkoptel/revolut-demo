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
import kotlinx.android.synthetic.main.simple_item.view.*

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionAdapter : RecyclerView.Adapter<ConversionAdapter.ViewItemHolder>() {
    private val presentersController = ConversionItemPresenter.Factory
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

    override fun onViewDetachedFromWindow(holder: ViewItemHolder?) {
        // It will be called a lot during lifecycle, but it is ok to drop the state of presenter even if it is done
        // not multiple times
        holder?.itemPresenter?.detach()
        super.onViewDetachedFromWindow(holder)
    }

    class ViewItemHolder(
        view: View,
        val itemPresenter: ConversionItemPresenter
    ) : RecyclerView.ViewHolder(view) {
        private val txtField: TextView = view.txtField
        private val editText: EditText = view.editField

        // Lets keep our listener events multicasted, with always latest item replayed to subscriber
        private val focusEvents = RxView.focusChanges(editText)
            .replay(1)
            .refCount()
        private val textChanges = RxTextView.textChanges(editText)
            .map { it.toString() }
            .filter { !it.isEmpty() }
            .replay(1)
            .refCount()

        fun bind(conversion: Conversion) {
            editText.isEnabled = (layoutPosition == 0)
            updateConversion(conversion)
            itemPresenter.reattach(ItemView(conversion.currency))
        }

        private fun updateConversion(conversion: Conversion) {
            txtField.text = conversion.currency
            editText.setText(conversion.toHumanFormat())
        }

        /**
         * Additional class to leverage the change in the view state. That is the easiest way to hold a state and
         * avoid ugly null checks. In particular case our view state is the 'currency' we are currently displaying.
         */
        inner class ItemView(private val currency: String) : ConversionViewItem {
            override fun getCurrency(): String = currency

            override fun onConversionRequest(): Observable<ConversionRequest> {
                return textChanges.map { ConversionRequest(currency, it.toDouble()) }
            }

            override fun onFocusChanges(): Observable<Boolean> {
                return focusEvents
            }

            override fun render(state: ConversionViewItem.State) = when (state) {
                is ConversionViewItem.State.Update -> updateConversion(state.value)
            }
        }
    }
}