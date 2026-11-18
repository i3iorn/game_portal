package com.micro.gameportal

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/** "How to play" dialog per game, opened from the top bar help button. */
object GameHelp {

    private val TEXTS = mapOf(
        "ttt" to ("Take turns claiming squares; first to line up three in a row — " +
            "across, down, or diagonally — wins. Play a friend or the built-in opponent."),
        "puzzle" to ("Tap a tile next to the empty slot to slide it. Order the tiles " +
            "1 through N with the blank at the bottom right. Fewest moves wins."),
        "memory" to ("Flip two cards per turn. Matching pairs stay face up. Find all " +
            "eight pairs to clear the board."),
        "2048" to ("Swipe to slide all tiles. Equal tiles merge into their sum. Reach " +
            "2048 — then keep going for a higher score."),
        "mines" to ("Tap a square to reveal it; numbers show adjacent mines. Long-press " +
            "to flag suspected mines. Clear every safe square to win — avoid the mines."),
        "lights" to ("Tapping a light toggles it and its four neighbours. Turn every " +
            "light off in as few moves as possible."),
        "connect4" to ("Tap a column to drop your disc; gravity fills the lowest free " +
            "slot. First to connect four in a row — across, up, or diagonally — wins. " +
            "RED always moves first."),
    )

    fun show(activity: Activity, key: String) {
        val palette = Ui.theme(activity)
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = Ui.rounded(activity, palette.surface, radiusDp = 18f, ripple = false)
            setPadding(Ui.dp(activity, 24f), Ui.dp(activity, 24f), Ui.dp(activity, 24f), Ui.dp(activity, 16f))
        }
        card.addView(
            TextView(activity).apply {
                text = "How to play"
                textSize = 20f
                setTextColor(palette.text)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        card.addView(
            TextView(activity).apply {
                text = TEXTS[key] ?: ""
                textSize = 14f
                setTextColor(palette.textDim)
                setPadding(0, Ui.dp(activity, 12f), 0, Ui.dp(activity, 16f))
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        var closeAction: (() -> Unit)? = null
        card.addView(Ui.button(activity, "Got it", size = Ui.ButtonSize.MEDIUM) { closeAction?.invoke() })
        val dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(card)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        closeAction = { dialog.dismiss() }
        dialog.show()
    }
}
