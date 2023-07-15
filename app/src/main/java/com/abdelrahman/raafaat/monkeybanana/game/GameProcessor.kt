package com.abdelrahman.raafaat.monkeybanana.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.SurfaceHolder
import androidx.annotation.WorkerThread
import com.abdelrahman.raafaat.monkeybanana.R
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils.MIN_MONKEES
import com.abdelrahman.raafaat.monkeybanana.game.model.BananaSprite
import com.abdelrahman.raafaat.monkeybanana.game.model.GroundSprite
import com.abdelrahman.raafaat.monkeybanana.game.model.MonkeySprite
import com.abdelrahman.raafaat.monkeybanana.game.model.Sprite
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils

class GameProcessor(
    private val context: Context,
    private val holder: SurfaceHolder,
    private val globalPaint: Paint,
    private var gameInterface: GameInterface?
) {
    private var isGamePaused: Boolean = false
    private var msPerFrame = 20
    private var currentStatus = GameStatus.STATUS_NOT_STARTED
    private var points: Int = 0
    private var workSprites: MutableList<Sprite> = mutableListOf()
    private var bananaSprite: BananaSprite? = null
    private var groundSprite: GroundSprite? = null
    private var monkeySprite: MonkeySprite? = null
    private var countMonkees: Int = 0
    private val monkeyWidth = GameUtils.getDimenInPx(context, R.dimen.monkey_width)
    private val monkeyInterval = GameUtils.getDimenInPx(context, R.dimen.monkey_interval)

    init {
        startGame()
    }

    private fun startGame() {
        resetGame()
        currentStatus = GameStatus.STATUS_PLAY
        gameInterface?.onGameStart()
    }

    /**
     * Cleans all sprites. Resets the score.
     */
    private fun resetGame() {
        workSprites = mutableListOf()
        monkeySprite = null
        bananaSprite = null
        groundSprite = null
        countMonkees = 0
        points = 0
    }

    @WorkerThread
    fun execute() {
        /*
        In our GameProcessor, we loop as long as the Thread is active.
        First, we take care of the rendering of our game. To do this, we obtain a reference to the
        Canvas of our SurfaceHolder object by calling its lockCanvas method.
        We then empty the content of this Canvas before iterating on all the elements of our
        game that we want to return to the screen. These elements being our Sprites.
         */
        while (!Thread.interrupted()) {
            if (isGamePaused) {
                continue
            }

            val canvas = holder.lockCanvas()
            val screenWidth = canvas.width.toFloat()

            removeDiedSprites(canvas)
            if (!renderFrames()) {
                break
            }

            when (currentStatus) {
                GameStatus.STATUS_NOT_STARTED -> {
                    // Show the home screen
                }

                GameStatus.STATUS_GAME_OVER -> {
                    // Show the Game Over screen.
                }

                GameStatus.STATUS_PLAY -> {
                    // Show the game.
                    // Draw the score, the ground, the bird...
                    if (groundSprite == null || !groundSprite!!.isAlive()) {
                        groundSprite = GroundSprite(context)
                        workSprites.add(groundSprite!!)
                    }
                    if (bananaSprite == null || !bananaSprite!!.isAlive()) {
                        bananaSprite = BananaSprite(context)
                        workSprites.add(bananaSprite!!)
                    }

                    // don't forget the obstacles and the rewards !
                    var nextMonkeyX = screenWidth
                    if (monkeySprite != null) {
                        nextMonkeyX = monkeySprite!!.x + monkeyInterval
                    }
                    while (countMonkees < MIN_MONKEES && points == 0) {
                        monkeySprite = MonkeySprite(
                            context,
                            nextMonkeyX,
                            monkeySprite?.lastBlockY
                        )
                        workSprites.add(0, monkeySprite!!)

                        nextMonkeyX += monkeyWidth + monkeyInterval
                        countMonkees++
                    }

                    updateGame()
                }
            }
        }
    }

    private fun renderFrames(): Boolean {
        val startTime = System.currentTimeMillis()
        /*
            The rendering time is measured before comparing this time to a constant called GAP.
            This constant allows us to add, if necessary, a delay to avoid that the rendering
            phase of the Game Loop be too fast.
             */
        val duration = System.currentTimeMillis() - startTime
        val gap = msPerFrame - duration
        if (gap > 0) {
            return try {
                Thread.sleep(gap)
                true
            } catch (e: Exception) {
                false
            }
        }
        return true
    }

    private fun removeDiedSprites(canvas: Canvas) {
        try {
            cleanCanvas(canvas)

            /*
            This iteration is performed via an Iterator object and we use it to delete Sprites
            considered as no longer alive. This work is encapsulated within a try / finally block.
            In the finally part, we ask that the updates we have made on the Canvas be posted on
            the SurfaceHolder via a call to the unlockCanvasAndPost method with the current
            instance of Canvas passed as a parameter.
             */
            val iterator: MutableListIterator<Sprite> = workSprites.listIterator()
            while (iterator.hasNext()) {
                val sprite = iterator.next()
                if (sprite.isAlive()) {
                    sprite.onDraw(canvas, globalPaint, currentStatus)
                } else {
                    if (sprite is MonkeySprite) {
                        points++
                        countMonkees--
                        gameInterface?.onGetPoint()
                    }
                    iterator.remove()
                    if (workSprites.size == 2) {
                        currentStatus = GameStatus.STATUS_GAME_OVER
                        gameInterface?.onGameOver()
                    }
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun updateGame() {
        /*
        We can now complete our Game Loop with game updates about Sprites.
        We will add the following code to detect if the game should stop because
        the player has lost.
         */
        val iterator: MutableListIterator<Sprite> = workSprites.listIterator()
        while (iterator.hasNext()) {
            val sprite = iterator.next()
            if (sprite.isHit(bananaSprite!!) && sprite is MonkeySprite) {
                gameInterface?.onHit()
                currentStatus = GameStatus.STATUS_GAME_OVER
                gameInterface?.onGameOver()
            }
        }
    }

    /**
     * The goal of Flying Bird is to allow the player to progress the bird by tapping on the screen,
     * so we must act within the onTouch method of the OnTouchListener interface that our main
     * activity inherits. It is in this method that we will interact with the player when they type
     * on the screen.
     */
    fun onTap() {
        when (currentStatus) {
            GameStatus.STATUS_NOT_STARTED -> UInt

            GameStatus.STATUS_PLAY -> {
                /*
                In case the game is in progress, we call the onTap method of the BirdSprite
                to increment its current speed which causes the bird to rise on the screen.
                This rise on the bird’s screen fights against the effect of gravity by preventing
                it from falling to the ground.
                 */
                bananaSprite?.jump()
            }

            GameStatus.STATUS_GAME_OVER -> {
                startGame()
            }
        }
    }

    private fun cleanCanvas(canvas: Canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
    }

    /**
     * Pauses the game (pauses the game loop).
     */
    fun pause() {
        isGamePaused = true
    }

    /**
     * Resumes the game (resumes the game loop).
     */
    fun resume() {
        isGamePaused = false
    }

    fun release() {
        gameInterface = null
    }

    interface GameInterface {
        fun onGameStart()
        fun onGetPoint()
        fun onHit()
        fun onGameOver()
    }
}
