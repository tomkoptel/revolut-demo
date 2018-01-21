package com.tom.personal.revolut

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author Tom Koptel: tom.koptel@showmax.com
 * @since 1/21/18
 */
data class OnItemClick(val view: View, val position: Int) {
    fun isValid(): Boolean = position != RecyclerView.NO_POSITION
}