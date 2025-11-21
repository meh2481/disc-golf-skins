package com.example.discgolfskins

data class Player(
    val name: String,
    val id: Int
)

data class HoleScore(
    val playerId: Int,
    val score: Int
)

data class Hole(
    val number: Int,
    val scores: Map<Int, Int> = emptyMap()
)

data class SkinResult(
    val holeNumber: Int,
    val winnerId: Int?,
    val skinsValue: Int
)

data class GameState(
    val players: List<Player> = emptyList(),
    val holes: List<Hole> = emptyList(),
    val currentHole: Int = 1,
    val isGameStarted: Boolean = false,
    val isGameFinished: Boolean = false
) {
    fun calculateSkins(): List<SkinResult> {
        val results = mutableListOf<SkinResult>()
        var carriedOver = 0

        holes.forEach { hole ->
            val scores = hole.scores
            if (scores.isEmpty()) return@forEach

            val minScore = scores.values.minOrNull() ?: return@forEach
            val winners = scores.filter { it.value == minScore }

            if (winners.size == 1) {
                // Single winner
                val winnerId = winners.keys.first()
                results.add(SkinResult(hole.number, winnerId, 1 + carriedOver))
                carriedOver = 0
            } else {
                // Tie - carry over
                carriedOver++
                results.add(SkinResult(hole.number, null, 1 + carriedOver - 1))
            }
        }

        // Add carried over skins to last hole if game is finished
        if (carriedOver > 0 && results.isNotEmpty()) {
            val lastResult = results.last()
            if (lastResult.winnerId == null) {
                results[results.lastIndex] = lastResult.copy(skinsValue = carriedOver)
            }
        }

        return results
    }

    fun getPlayerSkins(playerId: Int): Int {
        return calculateSkins()
            .filter { it.winnerId == playerId }
            .sumOf { it.skinsValue }
    }
}
