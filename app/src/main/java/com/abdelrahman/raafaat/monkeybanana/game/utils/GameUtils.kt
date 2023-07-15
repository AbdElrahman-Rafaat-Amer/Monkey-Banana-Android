package com.abdelrahman.raafaat.monkeybanana.game.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat


/**
 * Utility methods.
 */
object GameUtils {

    const val MIN_MONKEES = 10
    const val UNDEFINED = -999f
    const val TAG = "MONKEY_BANANA"

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