package com.abdelrahman.raafaat.monkeybanana

import android.graphics.Canvas
import android.graphics.Paint

/**
 * Sprites have in common a certain number of behaviors, we will create an interface to model
 * these behaviors.
 */
interface Sprite {
    companion object {
        const val STATUS_PLAY = 0
        const val STATUS_NOT_STARTED = 1
        const val STATUS_GAME_OVER = 2
        const val MIN_MONKEES = 60
        const val MS_PER_FRAME = 20
        const val UNDEFINED = -999f
        const val TAG = "MONKEY_BANANA"
    }

    /**
     * to request the drawing on the Sprite’s Canvas
     */
    fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int)

    /**
     * to know if a Sprite is still alive or not
     */
    fun isAlive(): Boolean

    /**
     * to manage the collision between a Sprite and another Sprite
     */
    fun isHit(sprite: Sprite): Boolean

    /**
     * Returns the number of points associated with a Sprite instance
     */
    fun getScore(): Int
}