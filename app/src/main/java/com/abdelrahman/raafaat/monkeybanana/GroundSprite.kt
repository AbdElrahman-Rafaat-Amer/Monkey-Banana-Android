package com.abdelrahman.raafaat.monkeybanana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect

class GroundSprite(
    context: Context
) : Sprite {
    private val speed: Float = Utils.getDimenInPx(context, R.dimen.sprite_speed)
    private var layerX: Float = 0f
    private var layerY: Float = 0f
    private val groundUp: Drawable = Utils.getDrawable(context, R.drawable.ic_ground)
    private val groundWidth: Float = Utils.getDimenInPx(context, R.dimen.ground_width)
    private val groundHeight: Float = Utils.getDimenInPx(context, R.dimen.ground_height)

    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: Int) {
        isAlive = status != Sprite.STATUS_NOT_STARTED

        val screenWidth = canvas.width.toFloat()
        val screenHeight = canvas.height.toFloat()

        if (status == Sprite.STATUS_PLAY) {
            layerX -= speed
        }
        if (layerX < -groundWidth) {
            layerX = 0f
        }
        layerY = screenHeight - groundHeight

        for (x in layerX.toInt() until screenWidth.toInt() step groundWidth.toInt()) {
            groundUp.bounds = RectF(
                x.toFloat(),
                layerY,
                x + groundWidth,
                screenHeight
            ).toRect()
            groundUp.draw(canvas)
        }
    }

    override fun isAlive(): Boolean = isAlive

    override fun isHit(sprite: Sprite): Boolean = (sprite is BananaSprite
            && sprite.getRect().bottom >= layerY)

    override fun getScore(): Int = 0

}
