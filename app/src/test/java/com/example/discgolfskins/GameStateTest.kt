package com.example.discgolfskins

import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {

    @Test
    fun testSimpleSkinWin() {
        // Player 0 scores 3, Player 1 scores 4
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4))
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(1, skins.size)
        assertEquals(0, skins[0].winnerId) // Alice wins
        assertEquals(1, skins[0].skinsValue) // Worth 1 skin
        
        assertEquals(1, gameState.getPlayerSkins(0)) // Alice has 1 skin
        assertEquals(0, gameState.getPlayerSkins(1)) // Bob has 0 skins
    }

    @Test
    fun testTieCarryOver() {
        // Hole 1: Tie (3-3), Hole 2: Player 0 wins (3-4)
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3)),
            Hole(2, mapOf(0 to 3, 1 to 4))
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(2, skins.size)
        
        // Hole 1: Tie
        assertEquals(null, skins[0].winnerId)
        assertEquals(1, skins[0].skinsValue)
        
        // Hole 2: Alice wins 2 skins (1 for this hole + 1 carried over)
        assertEquals(0, skins[1].winnerId)
        assertEquals(2, skins[1].skinsValue)
        
        assertEquals(2, gameState.getPlayerSkins(0)) // Alice has 2 skins
        assertEquals(0, gameState.getPlayerSkins(1)) // Bob has 0 skins
    }

    @Test
    fun testMultipleTieCarryOver() {
        // Hole 1: Tie, Hole 2: Tie, Hole 3: Player 1 wins
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3)),
            Hole(2, mapOf(0 to 4, 1 to 4)),
            Hole(3, mapOf(0 to 5, 1 to 4))
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(3, skins.size)
        
        // Hole 1: Tie
        assertEquals(null, skins[0].winnerId)
        
        // Hole 2: Tie
        assertEquals(null, skins[1].winnerId)
        
        // Hole 3: Bob wins 3 skins (1 for this hole + 2 carried over)
        assertEquals(1, skins[2].winnerId)
        assertEquals(3, skins[2].skinsValue)
        
        assertEquals(0, gameState.getPlayerSkins(0)) // Alice has 0 skins
        assertEquals(3, gameState.getPlayerSkins(1)) // Bob has 3 skins
    }

    @Test
    fun testThreePlayerSkin() {
        // Player 0 scores 3, Player 1 scores 4, Player 2 scores 5
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1),
            Player("Charlie", 2)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4, 2 to 5))
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(1, skins.size)
        assertEquals(0, skins[0].winnerId) // Alice wins
        assertEquals(1, skins[0].skinsValue)
        
        assertEquals(1, gameState.getPlayerSkins(0)) // Alice has 1 skin
        assertEquals(0, gameState.getPlayerSkins(1)) // Bob has 0 skins
        assertEquals(0, gameState.getPlayerSkins(2)) // Charlie has 0 skins
    }

    @Test
    fun testThreePlayerTieWithTwoPlayers() {
        // Player 0 scores 3, Player 1 scores 3, Player 2 scores 5 (tie between 0 and 1)
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1),
            Player("Charlie", 2)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3, 2 to 5))
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(1, skins.size)
        assertEquals(null, skins[0].winnerId) // Tie, carry over
        assertEquals(1, skins[0].skinsValue)
    }

    @Test
    fun testCompleteGame() {
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1),
            Player("Charlie", 2)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4, 2 to 4)), // Alice wins
            Hole(2, mapOf(0 to 3, 1 to 3, 2 to 3)), // Tie
            Hole(3, mapOf(0 to 5, 1 to 4, 2 to 3)), // Charlie wins 2
            Hole(4, mapOf(0 to 4, 1 to 3, 2 to 4))  // Bob wins
        )
        val gameState = GameState(players = players, holes = holes)
        
        assertEquals(1, gameState.getPlayerSkins(0)) // Alice: 1 skin
        assertEquals(1, gameState.getPlayerSkins(1)) // Bob: 1 skin
        assertEquals(2, gameState.getPlayerSkins(2)) // Charlie: 2 skins
    }

    @Test
    fun testEmptyHoleIgnored() {
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4)),
            Hole(2, emptyMap()) // Empty hole should be ignored
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(1, skins.size) // Only hole 1 counted
        assertEquals(0, skins[0].winnerId)
        assertEquals(1, skins[0].skinsValue)
    }
}
