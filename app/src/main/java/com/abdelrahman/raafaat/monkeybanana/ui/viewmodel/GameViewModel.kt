package com.abdelrahman.raafaat.monkeybanana.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val HIGH_SCORE_KEY = "HIGH_SCORE_KEY"

class GameViewModel(private var application: Application) : AndroidViewModel(application) {

    private var _isGameEnded = MutableLiveData(false)
    var isGameEnded: LiveData<Boolean> = _isGameEnded

    private val sharedPreferences: SharedPreferences
        get() {
            return application.getSharedPreferences(
                "com.abdelrahman.raafaat.monkeybanana",
                Context.MODE_PRIVATE
            )
        }

    fun onGameStarted() {
        _isGameEnded.value = false
    }

    fun onGameEnded(score: Int) {
        _isGameEnded.value = true
        storeHighScore(score)
    }

    private fun storeHighScore(score: Int) {
        if (isBestScore(score)) {
            sharedPreferences.edit().putInt(HIGH_SCORE_KEY, score).apply()
        }
    }

    private fun isBestScore(score: Int): Boolean = getBestScore() < score


    fun getBestScore(): Int = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)

}