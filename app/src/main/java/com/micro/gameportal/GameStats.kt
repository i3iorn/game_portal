package com.micro.gameportal

import android.content.Context

/** Lightweight per-game counters: games played and games won. */
object GameStats {

    private fun prefs(context: Context, game: String) =
        context.getSharedPreferences("stats_$game", Context.MODE_PRIVATE)

    /** Called when a game round ends. */
    fun recordEnd(context: Context, game: String, won: Boolean) {
        val prefs = prefs(context, game)
        prefs.edit()
            .putLong("played", prefs.getLong("played", 0) + 1)
            .putLong("won", prefs.getLong("won", 0) + if (won) 1 else 0)
            .apply()
    }

    /** "Played N · Won M", or null when nothing has been recorded yet. */
    fun summary(context: Context, game: String): String? {
        val prefs = prefs(context, game)
        if (!prefs.contains("played")) return null
        val played = prefs.getLong("played", 0)
        val won = prefs.getLong("won", 0)
        return "Played $played · Won $won"
    }
}
