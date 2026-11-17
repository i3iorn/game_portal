package com.micro.gameportal

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

/**
 * End-of-game overlay: a centered result card over a dimmed screen with
 * "New game" and "Close" actions. One overlay at a time, app-wide.
 */
object ResultOverlay {

    private var current: Dialog? = null

    fun dismiss() {
        current?.dismiss()
        current = null
    }

    fun show(activity: Activity, title: String, detail: String, onNewGame: () -> Unit) {
        dismiss()
        val palette = Ui.theme(activity)
        val inner = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val card = MaterialCardView(activity).apply {
            setCardBackgroundColor(palette.surface)
            radius = Ui.dp(activity, 18f).toFloat()
            cardElevation = Ui.dp(activity, 6f).toFloat()
            setPadding(Ui.dp(activity, 24f), Ui.dp(activity, 24f), Ui.dp(activity, 24f), Ui.dp(activity, 20f))
            addView(
                inner,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        inner.addView(
            TextView(activity).apply {
                text = title
                textSize = 22f
                setTextColor(palette.text)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        inner.addView(
            TextView(activity).apply {
                text = detail
                textSize = 14f
                setTextColor(palette.textDim)
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(activity, 8f), 0, Ui.dp(activity, 16f))
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        actions.addView(Ui.button(activity, "New game") {
            dismiss()
            onNewGame()
        })
        actions.addView(Ui.button(activity, "Close", size = Ui.ButtonSize.COMPACT, quiet = true) {
            dismiss()
        })
        inner.addView(actions)
        val dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(card)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        dialog.show()
        current = dialog
    }
}
