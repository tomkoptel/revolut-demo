package com.tom.personal.revolut

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.tom.personal.revolut.domain.Conversion
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * @author Tom Koptel: tom.koptel@gmail.com
 * @since 1/21/18
 */
class ConversionAdapter : RecyclerView.Adapter<ConversionViewHolder>() {
    private val presentersController = ConversionItemPresenter.Factory
    private var items: MutableList<Conversion> = mutableListOf()
    private val clickSubject = PublishSubject.create<View>()

    fun addAll(items: List<Conversion>) {
        this.items = items.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Performs the swapping of items. We pass the position of view we want to put on the first place.
     */
    fun swapFirstItem(targetPosition: Int) {
        val target = items[targetPosition]
        val first = items.first()

        items[0] = target
        items[targetPosition] = first

        notifyItemChanged(0)
        notifyItemChanged(targetPosition)
    }

    /**
     * Expose the full data necessary to backtrack click source. [OnItemClick] describes event of user clicking on
     * [RecyclerView].
     */
    fun onViewClicked(recyclerView: RecyclerView): Observable<OnItemClick> {
        return clickSubject.hide()
            .map { OnItemClick(it, recyclerView.getChildAdapterPosition(it)) }
            .filter { it.isValid() }
    }

    override fun onBindViewHolder(holder: ConversionViewHolder?, position: Int) {
        items[position].let {
            holder?.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.simple_item, parent, false)
        val conversionViewHolder = ConversionViewHolder(root, presentersController.create(parent.context))

        // Yet another way to delegate view clicks. Code owned by Stackoverflow community
        // https://stackoverflow.com/questions/44433115/recyclerview-item-click-using-rxjava2-rxbinding-not-working-after-fragment-rep/44840296
        RxView.clicks(root)
            .takeUntil(RxView.detaches(parent))
            .map { root }
            .subscribe(clickSubject)

        return conversionViewHolder
    }

    override fun getItemCount(): Int = items.size

    override fun onViewDetachedFromWindow(holder: ConversionViewHolder?) {
        // It will be called a lot during lifecycle, but it is ok to drop the state of presenter even if it is done
        // not multiple times
        holder?.itemPresenter?.detach()
        super.onViewDetachedFromWindow(holder)
    }
}