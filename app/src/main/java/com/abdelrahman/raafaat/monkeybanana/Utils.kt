package com.abdelrahman.raafaat.monkeybanana

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.random.Random


/**
 * Utility methods.
 */
object Utils {

    /**
     * Retrieve a dimensional for a particular resource ID for use as a size in raw pixels.
     */
    fun getDimenInPx(context: Context, @DimenRes id: Int): Float =
        context.resources.getDimensionPixelSize(id).toFloat()

    /**
     * Retrieve a dimensional for a particular resource ID.
     */
    fun getFloat(context: Context, @DimenRes id: Int): Float = context.resources.getDimension(id)

    /**
     * Returns a drawable object associated with a particular resource ID.
     */
    fun getDrawable(context: Context, @DrawableRes id: Int): Drawable =
        ContextCompat.getDrawable(context, id)!!
}

/**
 * Parses number to digits.
 */
fun Int.toDigits(): Array<Int> {
    val digits = mutableListOf<Int>()
    var i = this
    if (i == 0) {
        digits.add(0)
    } else {
        while (i > 0) {
            digits.add(i % 10)
            i /= 10
        }
    }
    return digits.toTypedArray()
}