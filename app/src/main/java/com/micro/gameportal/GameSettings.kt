package com.micro.gameportal

import android.content.Context

/** Cross-launch game options persisted in shared preferences. */
class GameSettings(private val context: Context) {

    companion object {
        val MINESWEEPER_PRESETS = listOf(
            "Beginner (8x8, 10 mines)",
            "Intermediate (10x10, 20 mines)",
            "Expert (12x12, 30 mines)",
        )
        val PUZZLE_SIZES = listOf(3, 4, 5)
        val LIGHTS_OUT_SIZES = listOf(3, 4, 5, 6)
        val TTT_OPPONENTS = listOf("2 players", "Easy", "Medium", "Hard")
    }

    private val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)

    var puzzleSize: Int
        get() = prefs.getInt("puzzle_size", 4)
        set(value) = prefs.edit().putInt("puzzle_size", value).apply()

    var minesweeperPreset: Int
        get() = prefs.getInt("minesweeper_preset", 0).coerceIn(0, MINESWEEPER_PRESETS.lastIndex)
        set(value) = prefs.edit().putInt("minesweeper_preset", value).apply()

    var lightsOutSize: Int
        get() = prefs.getInt("lights_out_size", 5)
        set(value) = prefs.edit().putInt("lights_out_size", value).apply()

    /** TicTacToeAi.Difficulty ordinal used when a fresh game starts. */
    var ticTacToeOpponent: Int
        get() = prefs.getInt("ttt_opponent", 0)
        set(value) = prefs.edit().putInt("ttt_opponent", value).apply()

    /** rows x cols x mines for the selected minesweeper preset. */
    fun minesweeperField(): Triple<Int, Int, Int> = when (minesweeperPreset) {
        1 -> Triple(10, 10, 20)
        2 -> Triple(12, 12, 30)
        else -> Triple(8, 8, 10)
    }
}
