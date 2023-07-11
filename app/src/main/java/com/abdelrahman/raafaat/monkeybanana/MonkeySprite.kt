package com.abdelrahman.raafaat.monkeybanana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.abdelrahman.raafaat.monkeybanana.Sprite.Companion.UNDEFINED
import kotlin.random.Random

class MonkeySprite(
    context: Context,
    var x: Float,
    val lastBlockY: Float?
) : Sprite {

    private val drawableMonkey: Drawable = Utils.getDrawable(context, R.drawable.chimpanzee)
    private val speed: Float = Utils.getDimenInPx(context, R.dimen.sprite_speed)
    private val monkeyWidth: Float = Utils.getDimenInPx(context, R.dimen.monkey_width)
    private val groundHeight: Float = Utils.getDimenInPx(context, R.dimen.ground_height)
    private var upHeight: Float = UNDEFINED
    private var downHeight: Float = UNDEFINED
    private var scored: Boolean = false
    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        if (upHeight == UNDEFINED) {
            val screenHeight = canvas.height
            val maxHeight = screenHeight * 9 / 10 //  9/10 of screenHeight is the max height of monkey
            val max = maxHeight - groundHeight
            val min = (screenHeight - maxHeight) / 3 + max
            upHeight = getRandomInt(max.toInt(), min.toInt()).toFloat()
            downHeight = screenHeight - groundHeight

        }

        isAlive = (status != Sprite.STATUS_NOT_STARTED && x + monkeyWidth >= 0f)

        if (status == Sprite.STATUS_NOT_STARTED) {
            return
        }
        if (status == Sprite.STATUS_PLAY) {
            x -= speed
        }
        drawableMonkey.bounds = getBottomPipeRect()
        drawableMonkey.draw(canvas)
    }

    private fun getRandomInt(minValue: Int, maxValue: Int): Int = Random.nextInt(minValue, maxValue)

    override fun isHit(sprite: Sprite): Boolean = (isAlive() && sprite is BananaSprite
            && getBottomPipeRect().intersect(sprite.getRect()))

    private fun getBottomPipeRect() = RectF(
        x,
        upHeight,
        x + monkeyWidth,
        downHeight
    ).toRect()

    override fun isAlive(): Boolean = isAlive

    override fun getScore(): Int = if (scored) 1 else 0

}
