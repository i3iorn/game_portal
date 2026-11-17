package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Settings, grouped per game: each section has a header and aligned
 * label/value rows; tapping a row's value cycles it and rebuilds the screen.
 */
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
        root.addView(Ui.status(this, "Settings"))

        root.addView(Ui.header(this, "15 PUZZLE"))
        root.addView(optionRow("Board size", "${settings.puzzleSize}x${settings.puzzleSize}") {
            settings.puzzleSize = nextSize(GameSettings.PUZZLE_SIZES, settings.puzzleSize)
        })

        root.addView(Ui.header(this, "MINESWEEPER"))
        root.addView(optionRow("Field", GameSettings.MINESWEEPER_PRESETS[settings.minesweeperPreset]) {
            settings.minesweeperPreset = (settings.minesweeperPreset + 1) % GameSettings.MINESWEEPER_PRESETS.size
        })

        root.addView(Ui.header(this, "LIGHTS OUT"))
        root.addView(optionRow("Board size", "${settings.lightsOutSize}x${settings.lightsOutSize}") {
            settings.lightsOutSize = nextSize(GameSettings.LIGHTS_OUT_SIZES, settings.lightsOutSize)
        })

        root.addView(Ui.header(this, "TIC-TAC-TOE"))
        root.addView(optionRow("Default opponent", GameSettings.TTT_OPPONENTS[settings.ticTacToeOpponent]) {
            settings.ticTacToeOpponent = (settings.ticTacToeOpponent + 1) % GameSettings.TTT_OPPONENTS.size
        })

        root.addView(Ui.header(this, "GENERAL"))
        root.addView(optionRow("Color theme", Palette.byIndex(settings.colorTheme).name) {
            settings.colorTheme = (settings.colorTheme + 1) % Palette.PRESETS.size
        })
        root.addView(optionRow("Sound & vibration", if (settings.feedbackEnabled) "On" else "Off") {
            settings.feedbackEnabled = !settings.feedbackEnabled
        })

        Ui.addCentered(root, Ui.button(this, "Back", size = Ui.ButtonSize.COMPACT, quiet = true) { finish() })
        return root
    }

    /** One settings row: caption on the left, cycling value button on the right. */
    private fun optionRow(label: String, value: String, cycle: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32, 4, 32, 4)
        }
        row.addView(
            TextView(this).apply {
                text = label
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            },
        )
        row.addView(
            Ui.button(this, value, size = Ui.ButtonSize.COMPACT, quiet = true) {
                cycle()
                setContentView(build())
            },
        )
        return row
    }
}
