package com.micro.gameportal

import android.content.Context

/**
 * Best-score persistence shared by all games. Values are Longs (ms, moves,
 * points); one file per game, keys may include a variant suffix (e.g. board size).
 */
class ScoreStore(context: Context, name: String) {

    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /** The stored best value, or null when nothing has been recorded yet. */
    fun best(key: String): Long? = if (prefs.contains(key)) prefs.getLong(key, 0) else null

    /** Records [value]; returns true when it became the new best. */
    fun submit(key: String, value: Long, lowerIsBetter: Boolean): Boolean {
        val current = best(key)
        val better = current == null || (if (lowerIsBetter) value < current else value > current)
        if (better) prefs.edit().putLong(key, value).apply()
        return better
    }
}
