package com.abdelrahman.raafaat.monkeybanana.game.model

import android.graphics.Canvas
import android.graphics.Paint
import com.abdelrahman.raafaat.monkeybanana.game.GameStatus

/**
 * Sprites have in common a certain number of behaviors, we will create an interface to model
 * these behaviors.
 */
interface Sprite {

    /**
     * to request the drawing on the Sprite’s Canvas
     */
    fun onDraw(canvas: Canvas, globalPaint: Paint, status: GameStatus)

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