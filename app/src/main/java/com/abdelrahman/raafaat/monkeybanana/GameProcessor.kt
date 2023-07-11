package com.abdelrahman.raafaat.monkeybanana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.WorkerThread
import com.abdelrahman.raafaat.monkeybanana.Sprite.Companion.TAG

class GameProcessor(
    private val context: Context,
    private val holder: SurfaceHolder,
    private val globalPaint: Paint,
    private var gameInterface: GameInterface?
) {


    private var currentStatus: Int = Sprite.STATUS_NOT_STARTED
    private var points: Int = 0
    private var workSprites: MutableList<Sprite> = mutableListOf()
    private var bananaSprite: BananaSprite? = null
    private var groundSprite: GroundSprite? = null
    private var monkeySprite: MonkeySprite? = null
    private var countMonkees: Int = 0
    private val monkeyWidth = Utils.getDimenInPx(context, R.dimen.monkey_width)
    private val monkeyInterval = Utils.getDimenInPx(context, R.dimen.monkey_interval)
    private var newBestScore: Boolean = false

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
//            if (isPaused) continue


            val canvas = holder.lockCanvas()
            val screenWidth = canvas.width.toFloat()

            removeDiedSprites(canvas)
            if (!renderFrames()) {
                break
            }

            when (currentStatus) {
                Sprite.STATUS_NOT_STARTED -> {
                    // Show the home screen
                }

                Sprite.STATUS_GAME_OVER -> {
                    // Show the Game Over screen.
                }

                Sprite.STATUS_PLAY -> {
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
                    while (countMonkees < Sprite.MIN_MONKEES && points == 0) {
                        monkeySprite = MonkeySprite(
                            context,
                            nextMonkeyX,
                            monkeySprite?.lastBlockY
                        )
                        workSprites.add(0, monkeySprite!!)

                        nextMonkeyX += monkeyWidth + monkeyInterval
                        countMonkees++
                    }


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
                            /*
                            The game is over and we will have to display the end screen to
                            the player the next time the Game Loop passes.
                             */
                            currentStatus = Sprite.STATUS_GAME_OVER
                            gameInterface?.onGameOver()
                        }
                    }
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
        val gap = Sprite.MS_PER_FRAME - duration
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
                        currentStatus = Sprite.STATUS_GAME_OVER
                        gameInterface?.onGameOver()
                    }
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
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
            Sprite.STATUS_NOT_STARTED -> {
                /*
                If the game has not yet started, a first tap on the screen will allow you to
                change its status to the STATUS_PLAY constant. Have a good game!
                 */
                currentStatus = Sprite.STATUS_PLAY
                startGame()
            }

            Sprite.STATUS_PLAY -> {
                /*
                In case the game is in progress, we call the onTap method of the BirdSprite
                to increment its current speed which causes the bird to rise on the screen.
                This rise on the bird’s screen fights against the effect of gravity by preventing
                it from falling to the ground.
                 */
                bananaSprite?.jump()
            }

            Sprite.STATUS_GAME_OVER -> {
                /*
                Finally, if the game is over, we call the onTap method of the GameOverSprite object
                which will return a constant as output allowing us to know which part of the end
                screen the player to touch in order to react accordingly either to start a new game
                or to share the successful score via social networks for example.
                 */
                gameInterface?.onGameOver()
                currentStatus = Sprite.STATUS_PLAY
                startGame()
            }
        }
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

    private fun startGame() {
        resetGame()
        currentStatus = Sprite.STATUS_PLAY
        gameInterface?.onGameStart()
    }

    private fun cleanCanvas(canvas: Canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
    }

    interface GameInterface {
        fun onGameStart()
        fun onGetPoint()
        fun onHit()
        fun onGameOver()
    }
}
