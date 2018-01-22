package com.tom.personal.revolut.ext

import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

/**
 * Picasso extension to let us first force cache lookup and only then fallback to the network.
 */
fun Picasso.loadWithCache(uri: Uri, target: ImageView) {
    load(uri).networkPolicy(NetworkPolicy.OFFLINE)
        .into(target, object : Callback {
            override fun onSuccess() {
            }

            override fun onError() {
                load(uri).into(target)
            }
        })
}