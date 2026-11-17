package com.micro.gameportal

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Memory match: 4x4 grid, eight emoji pairs, flip two at a time. */
class MemoryActivity : Activity() {

    private companion object {
        val FACES = listOf("🍀", "🚀", "🎲", "🌟", "🎵", "🍎", "⚡", "🧩")
    }

    private lateinit var faces: List<String>
    private lateinit var cells: Array<Button>
    private lateinit var status: TextView
    private var firstOpen = -1
    private var matched = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        faces = FACES + FACES
        faces = faces.shuffled()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        status = TextView(this).apply { text = "Find all pairs!"; textSize = 18f; setPadding(0, 24, 0, 24) }
        root.addView(status)
        root.addView(buildGrid())
        setContentView(root)
    }

    private fun buildGrid(): GridLayout {
        cells = Array(faces.size) { Button(this) }
        return GridLayout(this).apply {
            columnCount = 4
            cells.forEachIndexed { index, button ->
                button.textSize = 22f
                button.text = ""
                button.setOnClickListener { onCellTap(index) }
                addView(
                    button,
                    GridLayout.LayoutParams().apply {
                        width = 170; height = 170
                        setMargins(4, 4, 4, 4)
                    },
                )
            }
        }
    }

    private fun onCellTap(index: Int) {
        val button = cells[index]
        if (button.text.isNotEmpty() || firstOpen == index) return
        button.text = faces[index]
        if (firstOpen == -1) {
            firstOpen = index
            return
        }
        if (faces[firstOpen] == faces[index]) {
            matched += 2
            firstOpen = -1
            if (matched == faces.size) status.text = "Cleared! All pairs found."
        } else {
            val first = firstOpen
            firstOpen = -1
            button.postDelayed({ cells[first].text = ""; cells[index].text = "" }, 700)
        }
    }
}
