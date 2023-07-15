package com.abdelrahman.raafaat.monkeybanana.game.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.abdelrahman.raafaat.monkeybanana.R
import com.abdelrahman.raafaat.monkeybanana.game.GameStatus
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils.UNDEFINED

class BananaSprite(
    context: Context,
) : Sprite {

    private val bananaDrawable: Drawable = GameUtils.getDrawable(context, R.drawable.banana)
    private val bananaHeight: Float = GameUtils.getDimenInPx(context, R.dimen.banana_height)
    private val bananaWidth: Float =
        bananaHeight * bananaDrawable.intrinsicWidth / bananaDrawable.intrinsicHeight
    private val groundHeight: Float = GameUtils.getDimenInPx(context, R.dimen.ground_height)
    private var x: Float = UNDEFINED
    private var y: Float = UNDEFINED
    private val acceleration: Float = GameUtils.getDimenInPx(context, R.dimen.banana_acceleration)
    private var currentSpeed: Float = 0f
    private val tapSpeed: Float = GameUtils.getFloat(context, R.dimen.banana_tap_speed)
    private var isAlive: Boolean = true
    private var maxY = 0f

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: GameStatus) {
        isAlive = status != GameStatus.STATUS_NOT_STARTED
        maxY = canvas.height - bananaHeight - groundHeight

        if (x == UNDEFINED && y == UNDEFINED) {
            x = canvas.width / 4 - bananaWidth / 2
            y = canvas.height - bananaWidth / 2
        }

        if (status == GameStatus.STATUS_PLAY) {
            // Reproduce the effect of gravity on our bird
            y += currentSpeed
            synchronized(this) {
                currentSpeed += acceleration
            }
        }
        if (y > maxY) {
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
        x + bananaWidth,
        y + bananaHeight
    ).toRect()

    fun jump() {
        synchronized(this) {
            if (y >= maxY) {
                currentSpeed = tapSpeed
                y -= currentSpeed
            }
        }
    }

}
