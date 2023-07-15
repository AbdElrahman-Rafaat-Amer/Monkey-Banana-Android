package com.abdelrahman.raafaat.monkeybanana.game.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.toRect
import com.abdelrahman.raafaat.monkeybanana.R
import com.abdelrahman.raafaat.monkeybanana.game.GameStatus
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils

class GroundSprite(
    context: Context
) : Sprite {
    private val speed: Float = GameUtils.getDimenInPx(context, R.dimen.sprite_speed)
    private var layerX: Float = 0f
    private var layerY: Float = 0f
    private val groundUp: Drawable = GameUtils.getDrawable(context, R.drawable.ic_ground)
    private val groundWidth: Float = GameUtils.getDimenInPx(context, R.dimen.ground_width)
    private val groundHeight: Float = GameUtils.getDimenInPx(context, R.dimen.ground_height)

    private var isAlive: Boolean = true

    override fun onDraw(canvas: Canvas, globalPaint: Paint, status: GameStatus) {
        isAlive = status != GameStatus.STATUS_NOT_STARTED

        val screenWidth = canvas.width.toFloat()
        val screenHeight = canvas.height.toFloat()

        if (status == GameStatus.STATUS_PLAY) {
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
