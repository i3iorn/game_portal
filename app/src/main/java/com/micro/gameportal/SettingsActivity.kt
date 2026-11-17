package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

/** Settings screen: puzzle size, minesweeper preset, lights out size, tic-tac-toe opponent. */
class SettingsActivity : Activity() {

    private lateinit var settings: GameSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = GameSettings(this)
        setContentView(build())
    }

    private fun nextSize(options: List<Int>, current: Int): Int =
        options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]

    private fun build(): LinearLayout {
        val root = Ui.gameRoot(this)
        root.addView(Ui.status(this, "Settings — tap an option to change it"))
        root.addView(Ui.button(this, "15 Puzzle: ${settings.puzzleSize}x${settings.puzzleSize}") {
            settings.puzzleSize = nextSize(GameSettings.PUZZLE_SIZES, settings.puzzleSize)
            setContentView(build())
        })
        root.addView(Ui.button(this, "Minesweeper: ${GameSettings.MINESWEEPER_PRESETS[settings.minesweeperPreset]}") {
            settings.minesweeperPreset = (settings.minesweeperPreset + 1) % GameSettings.MINESWEEPER_PRESETS.size
            setContentView(build())
        })
        root.addView(Ui.button(this, "Lights Out: ${settings.lightsOutSize}x${settings.lightsOutSize}") {
            settings.lightsOutSize = nextSize(GameSettings.LIGHTS_OUT_SIZES, settings.lightsOutSize)
            setContentView(build())
        })
        root.addView(Ui.button(this, "Tic-Tac-Toe opponent: ${GameSettings.TTT_OPPONENTS[settings.ticTacToeOpponent]}") {
            settings.ticTacToeOpponent = (settings.ticTacToeOpponent + 1) % GameSettings.TTT_OPPONENTS.size
            setContentView(build())
        })
        root.addView(Ui.button(this, "Back") { finish() })
        return root
    }
}
