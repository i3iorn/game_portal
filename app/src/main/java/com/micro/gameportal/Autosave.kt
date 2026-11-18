package com.micro.gameportal

import android.content.Context

/**
 * Persistent per-game autosave: while a user is mid-game and leaves the
 * screen, the current state is written here and restored on the next entry.
 * Cleared when a round ends or the user starts a new game.
 */
object Autosave {

    fun prefs(context: Context, game: String) =
        context.getSharedPreferences("autosave_$game", Context.MODE_PRIVATE)

    fun clear(context: Context, game: String) {
        prefs(context, game).edit().clear().apply()
    }

    fun exists(context: Context, game: String, key: String): Boolean =
        prefs(context, game).contains(key)
}
