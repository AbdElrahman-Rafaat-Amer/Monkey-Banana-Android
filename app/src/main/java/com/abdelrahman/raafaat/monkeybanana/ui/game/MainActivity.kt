package com.abdelrahman.raafaat.monkeybanana.ui.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.abdelrahman.raafaat.monkeybanana.R
import com.abdelrahman.raafaat.monkeybanana.databinding.ActivityMainBinding
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils.TAG
import com.abdelrahman.raafaat.monkeybanana.ui.viewmodel.GameViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mInterstitialAd: InterstitialAd? = null
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAds()
        observeViewModel()
    }

    private fun initAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    Log.i(TAG, "onAdLoaded: ")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    Log.i(TAG, "onAdFailedToLoad:error.cause    ${adError.cause}")
                    Log.i(TAG, "onAdFailedToLoad:error.code     ${adError.code}")
                    Log.i(TAG, "onAdFailedToLoad:error.domain   ${adError.domain}")
                    Log.i(TAG, "onAdFailedToLoad:error.message  ${adError.message}")
                }
            })

        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                super.onAdClicked()
                Log.i(TAG, "onAdClicked: ")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                Log.i(TAG, "onAdDismissedFullScreenContent: ")
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                mInterstitialAd = null
                Log.i(TAG, "onAdFailedToShowFullScreenContent:error.cause    ${error.cause}")
                Log.i(TAG, "onAdFailedToShowFullScreenContent:error.code     ${error.code}")
                Log.i(TAG, "onAdFailedToShowFullScreenContent:error.domain   ${error.domain}")
                Log.i(TAG, "onAdFailedToShowFullScreenContent:error.message  ${error.message}")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.i(TAG, "onAdImpression: ")
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.i(TAG, "onAdShowedFullScreenContent: ")
            }
        }
    }

    private fun observeViewModel() {
        gameViewModel.isGameEnded.observe(this) {
            if (it) {
                showAds()
            } else {
                initAds()
            }
        }
    }

    private fun showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        }
    }
}