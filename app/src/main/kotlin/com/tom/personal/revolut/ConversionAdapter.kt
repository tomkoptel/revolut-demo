package com.tom.personal.revolut

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tom.personal.revolut.domain.Conversion

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
class ConversionAdapter : RecyclerView.Adapter<ConversionViewHolder>() {
    private val presentersController = ConversionItemPresenter.Factory
    private var items: List<Conversion> = emptyList()

    fun addAll(items: List<Conversion>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ConversionViewHolder?, position: Int) {
        items[position].let {
            holder?.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.simple_item, parent, false)
        return ConversionViewHolder(root, presentersController.create(parent.context))
    }

    override fun getItemCount(): Int = items.size

    override fun onViewDetachedFromWindow(holder: ConversionViewHolder?) {
        // It will be called a lot during lifecycle, but it is ok to drop the state of presenter even if it is done
        // not multiple times
        holder?.itemPresenter?.detach()
        super.onViewDetachedFromWindow(holder)
    }
}