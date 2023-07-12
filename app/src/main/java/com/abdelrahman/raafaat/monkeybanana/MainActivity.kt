package com.abdelrahman.raafaat.monkeybanana

import android.graphics.Paint
import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import com.abdelrahman.raafaat.monkeybanana.Sprite.Companion.TAG
import com.abdelrahman.raafaat.monkeybanana.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity(), View.OnTouchListener, SurfaceHolder.Callback,
    GameProcessor.GameInterface {

    private lateinit var binding: ActivityMainBinding
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var gameProcessor: GameProcessor
    private lateinit var holder: SurfaceHolder
    private val globalPaint: Paint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint
    }
    private var drawingThread: Thread? = null
    private var points: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAds()

        binding.surfaceView.keepScreenOn = true
        holder = binding.surfaceView.holder
        binding.surfaceView.setZOrderOnTop(true)
        binding.surfaceView.setOnTouchListener(this)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Initialize the GameProcessor.
        gameProcessor = GameProcessor(applicationContext, holder, globalPaint, this)

    }

    private fun initAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                mInterstitialAd = null
            }

        }
    }

    //OnTouchListener
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            gameProcessor.onTap()
        }
        return false
    }

    //SurfaceHolder
    override fun surfaceCreated(holder: SurfaceHolder) {
        startDrawingThread()
    }

    private fun startDrawingThread() {
        stopDrawingThread()
        drawingThread = Thread {
            gameProcessor.execute()
        }
        drawingThread!!.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //Will not used for now
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopDrawingThread()
    }

    private fun stopDrawingThread() {
        drawingThread?.interrupt()
        try {
            drawingThread?.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Failed to interrupt the drawing thread")
        }
        drawingThread = null
    }

    //GameInterface
    override fun onGameStart() {
        runOnUiThread {
            binding.pointsTextView.visibility = View.VISIBLE
            binding.pointsTextView.text = getString(R.string.points, 0)
        }
    }

    override fun onGetPoint() {
        runOnUiThread {
            points++
            binding.pointsTextView.text = getString(R.string.points, points)
        }
    }

    override fun onHit() {
        Log.i(TAG, "onHit: will play hit sound. This feature will add in next commits")
    }

    override fun onGameOver() {
        runOnUiThread {
            points = 0
            binding.pointsTextView.visibility = View.GONE
            showAds()
        }
    }

    private fun showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        }
    }
}