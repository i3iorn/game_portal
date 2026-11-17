package com.micro.gameportal

/**
 * Opponent for tic-tac-toe: HUMAN (never moves), EASY (random),
 * MEDIUM (wins/blocks, else random), HARD (perfect minimax play).
 */
class TicTacToeAi(private val game: TicTacToeGame, private val difficulty: Difficulty) {

    enum class Difficulty { HUMAN, EASY, MEDIUM, HARD }

    /** Picks a move for the player whose turn it is; -1 when the game is over. */
    fun bestMove(): Int {
        if (game.winner != TicTacToeGame.EMPTY || game.isFull) return -1
        return when (difficulty) {
            Difficulty.HUMAN -> -1
            Difficulty.EASY -> openCells().random()
            Difficulty.MEDIUM -> winningMove(game.current)
                ?: winningMove(opponent())
                ?: openCells().random()
            Difficulty.HARD -> openCells().maxByOrNull { index ->
                minimax(scratchAfter(index, game.current), opponent(), 1)
            } ?: -1
        }
    }

    private fun openCells(): List<Int> =
        game.cells.indices.filter { game.cells[it] == TicTacToeGame.EMPTY }

    private fun opponent(): Int =
        if (game.current == TicTacToeGame.X) TicTacToeGame.O else TicTacToeGame.X

    private fun scratchAfter(index: Int, player: Int): TicTacToeGame =
        TicTacToeGame(game.cells, game.current).also { it.forcePlay(index, player) }

    /** A move that immediately completes a line for [player], if one exists. */
    private fun winningMove(player: Int): Int? = openCells().firstOrNull { index ->
        scratchAfter(index, player).winner == player
    }

    /**
     * Score of [scratch] for the AI player (game.current), with [player] to move.
     * Win = 10 - depth (prefer fast wins), loss = depth - 10, draw = 0.
     */
    private fun minimax(scratch: TicTacToeGame, player: Int, depth: Int): Int {
        val terminal = when {
            scratch.winner == game.current -> 10 - depth
            scratch.winner != TicTacToeGame.EMPTY -> depth - 10
            scratch.isFull -> 0
            else -> null
        }
        if (terminal != null) return terminal
        val next = if (player == TicTacToeGame.X) TicTacToeGame.O else TicTacToeGame.X
        val scores = openIn(scratch).map { cell ->
            val deeper = TicTacToeGame(scratch.cells, player)
            deeper.forcePlay(cell, player)
            minimax(deeper, next, depth + 1)
        }
        return if (player == game.current) scores.max() else scores.min()
    }

    private fun openIn(scratch: TicTacToeGame): List<Int> =
        scratch.cells.indices.filter { scratch.cells[it] == TicTacToeGame.EMPTY }
}
