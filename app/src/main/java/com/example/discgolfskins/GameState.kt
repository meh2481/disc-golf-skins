package com.example.discgolfskins

data class Player(
    val name: String,
    val id: Int
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
    val isGameFinished: Boolean = false,
    val nextPlayerId: Int = 0,
    val viewingHole: Int? = null  // null means viewing current hole
) {
    companion object {
        const val MAX_HOLES = 18
    }
    
    private fun calculateSkinsForHoles(holesToProcess: List<Hole>): List<SkinResult> {
        val results = mutableListOf<SkinResult>()
        var carriedOver = 0

        holesToProcess.forEach { hole ->
            val scores = hole.scores
            if (scores.isEmpty()) return@forEach

            val minScore = scores.values.minOrNull() ?: return@forEach
            val winners = scores.filter { it.value == minScore }

            if (winners.size == 1) {
                // Single winner - gets this skin plus any carried over
                val winnerId = winners.keys.first()
                results.add(SkinResult(hole.number, winnerId, 1 + carriedOver))
                carriedOver = 0
            } else {
                // Tie - carry over to next hole
                results.add(SkinResult(hole.number, null, 1))
                carriedOver++
            }
        }

        return results
    }
    
    fun calculateSkins(): List<SkinResult> {
        return calculateSkinsForHoles(holes)
    }

    fun getPlayerSkins(playerId: Int): Int {
        return calculateSkins()
            .filter { it.winnerId == playerId }
            .sumOf { it.skinsValue }
    }
    
    fun getUnclaimedSkins(): Int {
        // Count skins that are carried over (tied) and not yet awarded
        val skins = calculateSkins()
        var carriedOver = 0
        skins.forEach { result ->
            if (result.winnerId == null) {
                carriedOver++
            } else {
                carriedOver = 0
            }
        }
        return carriedOver
    }
    
    fun getCurrentSkinsValue(): Int {
        // Calculate how many skins are up for grabs on current hole
        // Only count COMPLETED holes (holes before current hole)
        val completedHoles = holes.take(currentHole - 1)
        val results = calculateSkinsForHoles(completedHoles)
        
        // Count carried over skins
        var carriedOver = 0
        results.forEach { result ->
            if (result.winnerId == null) {
                carriedOver++
            } else {
                carriedOver = 0
            }
        }
        
        return 1 + carriedOver
    }
    
    fun getPlayerSkinsCompleted(playerId: Int): Int {
        // Only count skins from completed holes (not current hole being edited)
        val completedHoles = holes.take(currentHole - 1)
        return calculateSkinsForHoles(completedHoles)
            .filter { it.winnerId == playerId }
            .sumOf { it.skinsValue }
    }
    
    fun getPlayersInOrder(holeNumber: Int): List<Player> {
        // Rotate player order based on hole number
        if (players.isEmpty()) return emptyList()
        val rotations = (holeNumber - 1) % players.size
        return players.takeLast(rotations) + players.dropLast(rotations)
    }
    
    fun getTotalScore(playerId: Int): Int {
        return holes.sumOf { it.scores[playerId] ?: 0 }
    }
    
    fun isRoundComplete(): Boolean {
        return currentHole > MAX_HOLES
    }
}
