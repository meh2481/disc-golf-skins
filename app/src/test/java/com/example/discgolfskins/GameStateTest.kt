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

    @Test
    fun testGameEndingWithTie() {
        // Game ends with a tie - carried skins remain unawarded
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4)), // Alice wins
            Hole(2, mapOf(0 to 3, 1 to 3))  // Tie - game ends
        )
        val gameState = GameState(players = players, holes = holes)
        
        val skins = gameState.calculateSkins()
        assertEquals(2, skins.size)
        
        // Hole 1: Alice wins
        assertEquals(0, skins[0].winnerId)
        assertEquals(1, skins[0].skinsValue)
        
        // Hole 2: Tie
        assertEquals(null, skins[1].winnerId)
        assertEquals(1, skins[1].skinsValue)
        
        // Alice has 1 skin, Bob has 0 (the tied skin is not awarded)
        assertEquals(1, gameState.getPlayerSkins(0))
        assertEquals(0, gameState.getPlayerSkins(1))
    }
    
    @Test
    fun testPlayerOrderRotation() {
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1),
            Player("Charlie", 2),
            Player("Dave", 3)
        )
        val gameState = GameState(players = players)
        
        // Hole 1: Original order
        val hole1Order = gameState.getPlayersInOrder(1)
        assertEquals("Alice", hole1Order[0].name)
        assertEquals("Bob", hole1Order[1].name)
        assertEquals("Charlie", hole1Order[2].name)
        assertEquals("Dave", hole1Order[3].name)
        
        // Hole 2: Rotated (last player moves to front)
        val hole2Order = gameState.getPlayersInOrder(2)
        assertEquals("Dave", hole2Order[0].name)
        assertEquals("Alice", hole2Order[1].name)
        assertEquals("Bob", hole2Order[2].name)
        assertEquals("Charlie", hole2Order[3].name)
        
        // Hole 3: Rotated again
        val hole3Order = gameState.getPlayersInOrder(3)
        assertEquals("Charlie", hole3Order[0].name)
        assertEquals("Dave", hole3Order[1].name)
        assertEquals("Alice", hole3Order[2].name)
        assertEquals("Bob", hole3Order[3].name)
    }
    
    @Test
    fun testGetCurrentSkinsValue() {
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        
        // No holes played - should be 1 skin
        val gameState1 = GameState(players = players, holes = emptyList())
        assertEquals(1, gameState1.getCurrentSkinsValue())
        
        // One hole with tie - should be 2 skins up for grabs
        val gameState2 = GameState(players = players, holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3))
        ))
        assertEquals(2, gameState2.getCurrentSkinsValue())
        
        // Two holes with ties - should be 3 skins up for grabs
        val gameState3 = GameState(players = players, holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3)),
            Hole(2, mapOf(0 to 4, 1 to 4))
        ))
        assertEquals(3, gameState3.getCurrentSkinsValue())
        
        // After a win - back to 1 skin
        val gameState4 = GameState(players = players, holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 3)),
            Hole(2, mapOf(0 to 3, 1 to 4))
        ))
        assertEquals(1, gameState4.getCurrentSkinsValue())
    }
    
    @Test
    fun testGetTotalScore() {
        val players = listOf(
            Player("Alice", 0),
            Player("Bob", 1)
        )
        val holes = listOf(
            Hole(1, mapOf(0 to 3, 1 to 4)),
            Hole(2, mapOf(0 to 4, 1 to 3)),
            Hole(3, mapOf(0 to 5, 1 to 5))
        )
        val gameState = GameState(players = players, holes = holes)
        
        assertEquals(12, gameState.getTotalScore(0)) // Alice: 3+4+5
        assertEquals(12, gameState.getTotalScore(1)) // Bob: 4+3+5
    }
}
