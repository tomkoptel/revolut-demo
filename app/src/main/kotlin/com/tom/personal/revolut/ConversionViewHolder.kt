package com.tom.personal.revolut

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tom.personal.revolut.domain.Conversion
import com.tom.personal.revolut.domain.ConversionRequest
import com.tom.personal.revolut.ext.loadWithCache
import com.tom.personal.revolut.picasso.PicassoFactory
import io.reactivex.Observable
import kotlinx.android.synthetic.main.simple_item.view.*

/**
 * The key component of UI that is get updated on every 1sec call. The particular view emits changes to the currency
 * value with a help of RxBinding library. We do listen changes in EditText and emit the value to the shared view
 * model local singleton.
 *
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/21/18
 */
class ConversionViewHolder(
    view: View,
    val itemPresenter: ConversionItemPresenter
) : RecyclerView.ViewHolder(view) {
    private val txtField: TextView = view.txtField
    private val editText: EditText = view.editField
    private val flagImg: ImageView = view.flagImg
    private val picasso = PicassoFactory.create(view.context)

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

        val flagSource = Uri.parse("https://flag/${conversion.currency}")
        picasso.loadWithCache(flagSource, flagImg)

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

        override fun onCurrencyEditTextFocusChanges(): Observable<Boolean> {
            return focusEvents
        }

        override fun render(state: ConversionViewItem.State) = when (state) {
            is ConversionViewItem.State.Update -> updateConversion(state.value)
        }
    }
}