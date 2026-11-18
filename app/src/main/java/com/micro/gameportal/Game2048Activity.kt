package com.micro.gameportal

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/** 2048: swipe to slide and merge tiles. Score and best score shown live. */
class Game2048Activity : Activity() {

    private companion object {
        const val STATE_BOARD = "board"
        const val STATE_SCORE = "score"
        const val STATE_STATUS = "status"
        const val KEY_BEST_SCORE = "best_score_v2"
    }

    private lateinit var game: Game2048
    private lateinit var tiles: Array<TextView>
    private lateinit var status: TextView
    private lateinit var boardGrid: View
    private val scores by lazy { ScoreStore(this, "game2048") }
    private var previousBoard: IntArray? = null
    private var winShown = false
    private var hintView: TextView? = null
    private val settings by lazy { GameSettings(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.setupEnter(this)
        game = Game2048()
        val scaffold = Ui.gameScaffold(this)
        val root = scaffold.root
        scaffold.controls.addView(Ui.topBar(this, "2048", { Ui.back(this) }, { GameHelp.show(this, "2048") }))
        status = Ui.status(this)
        if (!settings.hintSeen("2048")) {
            hintView = Ui.hint(this, "Swipe to slide and merge tiles")
            scaffold.controls.addView(hintView)
        }
        val grid = buildGrid()
        grid.setOnTouchListener(SwipeListener { direction -> onSwipe(direction) })
        scaffold.controls.addView(status)
        boardGrid = grid
        scaffold.board.addView(grid, SquareGridLayout.fillLayoutParams())
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        actions.addView(Ui.button(this, "Undo", size = Ui.ButtonSize.COMPACT, quiet = true) {
            if (game.undo()) render()
        })
        actions.addView(Ui.button(this, "New game") {
            ResultOverlay.dismiss()
            winShown = false
            previousBoard = null
            Autosave.clear(this, "2048")
            game.newGame()
            render()
        })
        Ui.addCentered(scaffold.controls, actions)
        setContentView(root)
        restoreOrStart(savedInstanceState)
        if (savedInstanceState == null && Autosave.exists(this, "2048", "board")) {
            val autosave = Autosave.prefs(this, "2048")
            val board = autosave.getString("board", "")!!.split(",").map { it.toInt() }.toIntArray()
            game.restore(board, autosave.getInt("score", 0), autosave.getInt("status", 0), board.any { it >= Game2048.WIN_TILE })
            winShown = game.status == Game2048.WIN
        }
        render()
    }

    override fun onPause() {
        super.onPause()
        if (game.isOver) {
            Autosave.clear(this, "2048")
        } else {
            Autosave.prefs(this, "2048").edit()
                .putString("board", game.board.joinToString(","))
                .putInt("score", game.score)
                .putInt("status", game.status)
                .apply()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_BOARD, game.board)
        outState.putInt(STATE_SCORE, game.score)
        outState.putInt(STATE_STATUS, game.status)
        if (game.status == Game2048.WIN) winShown = true
    }

    private fun restoreOrStart(saved: Bundle?) {
        val board = saved?.getIntArray(STATE_BOARD)
        if (board != null) {
            game.restore(board, saved.getInt(STATE_SCORE), saved.getInt(STATE_STATUS), board.any { it >= Game2048.WIN_TILE })
        }
    }

    private fun buildGrid(): GridLayout {
        tiles = Array(Game2048.CELLS) { TextView(this) }
        return SquareGridLayout(this).apply {
            columnCount = Game2048.SIZE
            tiles.forEachIndexed { index, tile ->
                tile.textSize = 22f
                tile.gravity = Gravity.CENTER
                addView(tile, cellParams(index / Game2048.SIZE, index % Game2048.SIZE, 4))
            }
        }
    }

    private fun onSwipe(direction: Game2048.Direction) {
        val previousScore = game.score
        if (!game.move(direction)) return
        consumeHint()
        val gained = game.score - previousScore
        if (gained > 0) {
            Feedback.play(this, Feedback.Sound.MATCH)
            Feedback.haptic(boardGrid)
        } else {
            Feedback.haptic(boardGrid, Feedback.HapticStrength.LIGHT)
        }
        scores.submit(KEY_BEST_SCORE, game.score.toLong(), lowerIsBetter = false)
        render()
        showEndOverlayIfNeeded()
    }

    private fun consumeHint() {
        if (hintView != null) {
            settings.markHintSeen("2048")
            hintView?.visibility = View.GONE
            hintView = null
        }
    }

    private fun showEndOverlayIfNeeded() {
        when {
            game.status == Game2048.LOST -> {
                GameStats.recordEnd(this, "2048", won = false)
                Autosave.clear(this, "2048")
                ResultOverlay.show(this, "No moves left", "Score ${game.score} · best ${bestScore()}") {
                    ResultOverlay.dismiss()
                    winShown = false
                    previousBoard = null
                    game.newGame()
                    render()
                }
            }
            game.status == Game2048.WIN && !winShown -> {
                winShown = true
                GameStats.recordEnd(this, "2048", won = true)
                ResultOverlay.show(this, "2048!", "Score ${game.score} — keep going for more") { }
            }
        }
    }

    private fun bestScore(): Long = scores.best(KEY_BEST_SCORE) ?: 0L

    private fun render() {
        tiles.forEachIndexed { index, tile ->
            val v = game.board[index]
            tile.text = if (v == 0) "" else v.toString()
            Ui.cell(tile, if (v == 0) Ui.theme(this@Game2048Activity).surfaceDim else tileColor(v), radiusDp = 10f)
            tile.setTextColor(if (v >= 8) Color.WHITE else Color.DKGRAY)
            animateTileChange(tile, previousBoard?.get(index), v)
        }
        previousBoard = game.board.copyOf()
        status.text = when {
            game.status == Game2048.LOST -> "No moves left! Score: ${game.score}  (best ${bestScore()})"
            else -> "Score: ${game.score}  (best ${bestScore()})" +
                if (game.status == Game2048.WIN) "  — 2048! Keep going" else ""
        }
    }

    /** Pop-in for spawns, bump for merges; silent when no previous board. */
    private fun animateTileChange(tile: TextView, old: Int?, new: Int) {
        when {
            old == null || (old == 0 && new == 0) || old == new -> return
            old == 0 -> {
                tile.scaleX = 0.55f; tile.scaleY = 0.55f
                tile.animate().scaleX(1f).scaleY(1f).setDuration(130).start()
            }
            new > old -> {
                tile.animate().scaleX(1.18f).scaleY(1.18f).setDuration(80)
                    .withEndAction { tile.animate().scaleX(1f).scaleY(1f).setDuration(90).start() }
                    .start()
            }
        }
    }

    private fun tileColor(v: Int): Int = when {
        v >= 1024 -> 0xFFFFB300.toInt()
        v >= 512 -> 0xFFFB8C00.toInt()
        v >= 256 -> 0xFFF4511E.toInt()
        v >= 128 -> 0xFFE53935.toInt()
        v >= 64 -> 0xFF8E24AA.toInt()
        v >= 32 -> 0xFF3949AB.toInt()
        v >= 16 -> 0xFF1E88E5.toInt()
        v >= 8 -> 0xFF00897B.toInt()
        v >= 4 -> 0xFF7CB342.toInt()
        else -> 0xFFC0CA33.toInt()
    }
}

/** Translates swipes on any view into [Game2048.Direction] callbacks. */
class SwipeListener(private val onSwipe: (Game2048.Direction) -> Unit) : View.OnTouchListener {

    private companion object {
        const val SWIPE_MIN_PX = 60f
    }

    private var downX = 0f
    private var downY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - downX
                val dy = event.y - downY
                when {
                    maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) < SWIPE_MIN_PX -> return false
                    kotlin.math.abs(dx) > kotlin.math.abs(dy) ->
                        onSwipe(if (dx > 0) Game2048.Direction.RIGHT else Game2048.Direction.LEFT)
                    else -> onSwipe(if (dy > 0) Game2048.Direction.DOWN else Game2048.Direction.UP)
                }
                view.performClick()
            }
        }
        return true
    }
}
