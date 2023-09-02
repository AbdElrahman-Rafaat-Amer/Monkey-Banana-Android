package com.abdelrahman.raafaat.monkeybanana.ui.game

import android.app.AlertDialog
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.abdelrahman.raafaat.monkeybanana.R
import com.abdelrahman.raafaat.monkeybanana.databinding.FragmentGameBinding
import com.abdelrahman.raafaat.monkeybanana.game.GameProcessor
import com.abdelrahman.raafaat.monkeybanana.game.utils.GameUtils
import com.abdelrahman.raafaat.monkeybanana.ui.viewmodel.GameViewModel


class GameFragment : Fragment(), View.OnTouchListener, SurfaceHolder.Callback,
    GameProcessor.GameInterface {

    private lateinit var binding: FragmentGameBinding
    private lateinit var gameProcessor: GameProcessor
    private lateinit var holder: SurfaceHolder
    private val globalPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var drawingThread: Thread? = null
    private var points: Int = 0
    private val gameViewModel: GameViewModel by activityViewModels()
    private var isGamePaused = false
    private var isGameOver = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        binding.surfaceView.keepScreenOn = true
        holder = binding.surfaceView.holder
        binding.surfaceView.setZOrderOnTop(true)
        binding.surfaceView.setOnTouchListener(this)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Initialize the GameProcessor.
        gameProcessor = GameProcessor(requireContext(), holder, globalPaint, this)
    }

    private fun initViews() {
        binding.pauseImage.setOnClickListener {
            if (!isGamePaused) {
                pauseGame()
            }
        }

        binding.pauseLayout.resumeButton.setOnClickListener {
            resumeGame()
        }

        binding.pauseLayout.leaveButton.setOnClickListener {
            navigateToNextScreen()
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
            Log.e(GameUtils.TAG, "Failed to interrupt the drawing thread")
        }
        drawingThread = null
    }

    //GameInterface
    override fun onGameStart() {
        activity?.runOnUiThread {
            binding.pointsTextView.visibility = View.VISIBLE
            binding.pointsTextView.text = getString(R.string.points, 0)
            gameViewModel.onGameStarted()
        }
    }

    override fun onGetPoint() {
        activity?.runOnUiThread {
            points++
            binding.pointsTextView.text = getString(R.string.points, points)
        }
    }

    override fun onHit() {
        Log.i(GameUtils.TAG, "onHit: will play hit sound. This feature will add in next commits")
    }

    override fun onGameOver() {
        isGameOver = true
        activity?.runOnUiThread {
            showGameOverDialog()
            binding.pointsTextView.visibility = View.GONE
        }
    }

    private fun showGameOverDialog() {
        val view = layoutInflater.inflate(R.layout.scan_resulat_menu, null)
        val scoreTextView = view.findViewById<TextView>(R.id.score_textView)
        val bestScoreTextView = view.findViewById<TextView>(R.id.best_score_textView)
        val cancelButton = view.findViewById<Button>(R.id.cancel_Button)
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        val window = alertDialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        scoreTextView.text = getString(R.string.score, points)
        bestScoreTextView.text = getString(R.string.best_score, gameViewModel.getBestScore())
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
            navigateToNextScreen()
            gameViewModel.onGameEnded(points)
            points = 0
        }

    }

    private fun navigateToNextScreen() {
        val gameOverAction = GameFragmentDirections.actionGameFragmentToStartFragment()
        binding.root.findNavController().navigate(gameOverAction)
    }

    private fun resumeGame() {
        binding.pauseLayout.root.visibility = View.GONE
        isGamePaused = false
        gameProcessor.resume()
    }

    override fun onPause() {
        super.onPause()
        if (!isGamePaused && !isGameOver) {
            pauseGame()
        }
    }

    private fun pauseGame() {
        isGamePaused = true
        gameProcessor.pause()
        binding.pauseLayout.root.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameProcessor.release()
    }
}