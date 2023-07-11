package com.abdelrahman.raafaat.monkeybanana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.abdelrahman.raafaat.monkeybanana.Sprite.Companion.UNDEFINED

class BananaSprite(
    context: Context,
) : Sprite {

    private val bananaDrawable: Drawable =  Utils.getDrawable(context, R.drawable.banana)
    private val birdHeight: Float = Utils.getDimenInPx(context, R.dimen.banana_height)
    private val birdWidth: Float =
        birdHeight * bananaDrawable.intrinsicWidth / bananaDrawable.intrinsicHeight
    private val groundHeight: Float = Utils.getDimenInPx(context, R.dimen.ground_height)
    private var x: Float = UNDEFINED
    private var y: Float = UNDEFINED
    private val acceleration: Float = Utils.getDimenInPx(context, R.dimen.banana_acceleration)
    private var currentSpeed: Float = 0f
    private val tapSpeed: Float = Utils.getFloat(context, R.dimen.banana_tap_speed)
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        isAlive = status != Sprite.STATUS_NOT_STARTED
        val maxY = canvas.height - birdHeight - groundHeight
        val minY = 0f
        if(x == UNDEFINED && y == UNDEFINED) {
            x = canvas.width / 4 - birdWidth / 2 // 25%
            y = canvas.height - birdHeight / 2 // 100%
        }

        if(status != Sprite.STATUS_NOT_STARTED) {
            // Reproduce the effect of gravity on our bird
            y += currentSpeed
            synchronized (this) {
                currentSpeed += acceleration
            }
        }
        if(y < minY) {
            // Ensure that the bird remains within the limits of the screen by resetting its
            // current position to 0 if it reaches the top of the screen.
            y = minY
        } else if(y > maxY) {
            // The same is done for its position at the bottom of the screen
            y = maxY
        }
        bananaDrawable.bounds = getRect()
        bananaDrawable.draw(canvas)
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: Sprite): Boolean = false

    override fun getScore(): Int = 0
    fun getRect(): Rect = RectF(
        x,
        y,
        x + birdWidth,
        y + birdHeight
    ).toRect()

    fun jump() {
        synchronized(this) {
            currentSpeed = tapSpeed
            y -= currentSpeed
        }
    }

}
