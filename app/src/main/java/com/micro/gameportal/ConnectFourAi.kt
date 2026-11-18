package com.micro.gameportal

/**
 * Connect Four opponent. Scoring AI: take an immediate win, block the
 * opponent's immediate win, then score columns (centre preference, future
 * threats) with a one-ply lookahead guard. Plays YELLOW (second).
 */
class ConnectFourAi(private val game: ConnectFourGame) {

    /** Returns the column to drop into, or -1 when the game is over. */
    fun bestMove(): Int {
        if (game.isOver) return -1
        val cols = game.cols

        game.winningMove(game.current)?.let { return it }
        game.winningMove(ConnectFourGame.RED)?.let { return it } // opponent = RED

        // Score columns: centre weight minus danger of enabling an opponent win above our drop.
        var bestCol = -1
        var bestScore = Int.MIN_VALUE
        for (col in 0 until cols) {
            if (game.landingRow(col) == -1) continue
            var score = when {
                col == cols / 2 -> 4
                col == cols / 2 - 1 || col == cols / 2 + 1 -> 2
                else -> 1
            }
            // If we drop here and the opponent then drops on top, do they win?
            val probe = ConnectFourGame(cols = game.cols, rows = game.rows, seed = 1)
            probe.restore(game.cells, game.current)
            probe.drop(col)
            if (!probe.isOver) {
                probe.drop(col) // opponent answers on top
                if (probe.winner != ConnectFourGame.EMPTY) score -= 100
            }
            if (score > bestScore) { bestScore = score; bestCol = col }
        }
        return bestCol
    }
}
