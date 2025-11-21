package com.example.discgolfskins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiscGolfSkinsApp()
                }
            }
        }
    }
}

@Composable
fun DiscGolfSkinsApp() {
    var gameState by remember { mutableStateOf(GameState()) }
    var showQuitDialog by remember { mutableStateOf(false) }
    
    // Handle back button
    BackHandler(enabled = gameState.isGameStarted) {
        when {
            gameState.isGameFinished -> {
                // From summary, go back to last hole
                val lastHoleWithScores = gameState.holes.size
                if (lastHoleWithScores > 0) {
                    gameState = gameState.copy(
                        isGameFinished = false,
                        viewingHole = lastHoleWithScores
                    )
                }
            }
            gameState.viewingHole != null -> {
                // From viewing past hole, go back to current
                gameState = gameState.copy(viewingHole = null)
            }
            else -> {
                // From current hole, show quit confirmation
                showQuitDialog = true
            }
        }
    }
    
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text(stringResource(R.string.confirm_quit)) },
            text = { Text(stringResource(R.string.quit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    gameState = GameState()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    when {
        !gameState.isGameStarted -> {
            PlayerSetupScreen(
                players = gameState.players,
                onAddPlayer = { name ->
                    val newPlayer = Player(name, gameState.nextPlayerId)
                    gameState = gameState.copy(
                        players = gameState.players + newPlayer,
                        nextPlayerId = gameState.nextPlayerId + 1
                    )
                },
                onRemovePlayer = { player ->
                    gameState = gameState.copy(
                        players = gameState.players.filter { it.id != player.id }
                    )
                },
                onStartGame = { orderedPlayers ->
                    gameState = gameState.copy(
                        players = orderedPlayers,
                        isGameStarted = true
                    )
                }
            )
        }
        gameState.isGameFinished -> {
            GameSummaryScreen(
                gameState = gameState,
                onNewGame = {
                    gameState = GameState()
                },
                onViewHole = { holeNumber ->
                    gameState = gameState.copy(
                        isGameFinished = false,
                        viewingHole = holeNumber
                    )
                }
            )
        }
        else -> {
            ScoreEntryScreen(
                gameState = gameState,
                onScoreChanged = { playerId, delta ->
                    val holeNumber = gameState.viewingHole ?: gameState.currentHole
                    val holeIndex = holeNumber - 1
                    val holes = gameState.holes.toMutableList()
                    
                    // Ensure we have enough holes
                    while (holes.size <= holeIndex) {
                        holes.add(Hole(holes.size + 1))
                    }
                    
                    val hole = holes[holeIndex]
                    val currentScore = hole.scores[playerId] ?: 3
                    val newScore = (currentScore + delta).coerceAtLeast(1)
                    val updatedScores = hole.scores.toMutableMap()
                    updatedScores[playerId] = newScore
                    holes[holeIndex] = hole.copy(scores = updatedScores)
                    
                    gameState = gameState.copy(holes = holes)
                },
                onNextHole = {
                    // Ensure all players have scores for current hole (default to 3)
                    val currentHoleIndex = gameState.currentHole - 1
                    val holes = gameState.holes.toMutableList()
                    
                    // Ensure we have enough holes
                    while (holes.size <= currentHoleIndex) {
                        holes.add(Hole(holes.size + 1))
                    }
                    
                    val currentHole = holes[currentHoleIndex]
                    val updatedScores = currentHole.scores.toMutableMap()
                    
                    // Add default score (3) for any players not yet scored
                    gameState.players.forEach { player ->
                        if (!updatedScores.containsKey(player.id)) {
                            updatedScores[player.id] = 3
                        }
                    }
                    
                    holes[currentHoleIndex] = currentHole.copy(scores = updatedScores)
                    
                    val nextHole = gameState.currentHole + 1
                    // After hole 18, automatically finish the game
                    if (nextHole > GameState.MAX_HOLES) {
                        gameState = gameState.copy(
                            holes = holes,
                            isGameFinished = true,
                            viewingHole = null
                        )
                    } else {
                        gameState = gameState.copy(
                            holes = holes,
                            currentHole = nextHole,
                            viewingHole = null
                        )
                    }
                },
                onFinishGame = {
                    gameState = gameState.copy(isGameFinished = true, viewingHole = null)
                },
                onViewHole = { holeNumber ->
                    gameState = gameState.copy(viewingHole = holeNumber)
                },
                onBackToCurrent = {
                    gameState = gameState.copy(viewingHole = null)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSetupScreen(
    players: List<Player>,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (Player) -> Unit,
    onStartGame: (List<Player>) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var currentPlayers by remember { mutableStateOf(players) }
    
    // Update when players change externally
    LaunchedEffect(players) {
        currentPlayers = players
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text(stringResource(R.string.player_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (playerName.isNotBlank()) {
                        val capitalizedName = playerName.trim()
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                                    else it.toString() 
                                }
                            }
                        onAddPlayer(capitalizedName)
                        playerName = ""
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    val capitalizedName = playerName.trim()
                        .split(" ")
                        .joinToString(" ") { word ->
                            word.replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                                else it.toString() 
                            }
                        }
                    onAddPlayer(capitalizedName)
                    playerName = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_player))
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Randomize button
        if (currentPlayers.size >= 2) {
            Button(
                onClick = {
                    currentPlayers = currentPlayers.shuffled()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.randomize_order))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Players (${currentPlayers.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(currentPlayers) { player ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { 
                            onRemovePlayer(player)
                            currentPlayers = currentPlayers.filter { it.id != player.id }
                        }) {
                            Text(stringResource(R.string.remove))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onStartGame(currentPlayers) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentPlayers.size >= 2
        ) {
            Text(stringResource(R.string.start_game))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreEntryScreen(
    gameState: GameState,
    onScoreChanged: (Int, Int) -> Unit,
    onNextHole: () -> Unit,
    onFinishGame: () -> Unit,
    onViewHole: (Int) -> Unit,
    onBackToCurrent: () -> Unit
) {
    val viewingHoleNumber = gameState.viewingHole ?: gameState.currentHole
    val isViewingPast = gameState.viewingHole != null
    val holeIndex = viewingHoleNumber - 1
    val hole = gameState.holes.getOrNull(holeIndex)
    
    // Get players in rotated order for this hole
    val playersInOrder = gameState.getPlayersInOrder(viewingHoleNumber)
    
    // For current hole: all scores are considered entered (default to 3)
    // For past holes: need explicit scores
    val allScoresEntered = if (isViewingPast) {
        playersInOrder.all { player ->
            hole?.scores?.containsKey(player.id) == true
        }
    } else {
        true  // Current hole always shows "Next Hole" button
    }
    
    val skinsUpForGrabs = gameState.getCurrentSkinsValue()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hole navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    if (viewingHoleNumber > 1) {
                        onViewHole(viewingHoleNumber - 1)
                    }
                },
                enabled = viewingHoleNumber > 1
            ) {
                Text("←", fontSize = 24.sp)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.hole_number, viewingHoleNumber),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!isViewingPast) {
                    Text(
                        text = stringResource(R.string.skins_up_for_grabs, skinsUpForGrabs),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(
                onClick = { 
                    // Allow navigating forward if hole exists OR if we're on the last completed hole and current hole is the next one
                    val nextHole = viewingHoleNumber + 1
                    if (nextHole < gameState.holes.size || 
                        (nextHole == gameState.currentHole && !gameState.isGameFinished)) {
                        onViewHole(nextHole)
                    } else if (nextHole == gameState.currentHole && gameState.isGameFinished) {
                        // If on last hole in finished game, go back to current view
                        onBackToCurrent()
                    }
                },
                enabled = viewingHoleNumber < gameState.holes.size || 
                         (viewingHoleNumber + 1 == gameState.currentHole && !gameState.isGameFinished)
            ) {
                Text("→", fontSize = 24.sp)
            }
        }
        
        if (isViewingPast) {
            Button(
                onClick = onBackToCurrent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.back_to_current))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(playersInOrder) { player ->
                val currentScore = hole?.scores?.get(player.id) ?: 3

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Skins: ${gameState.getPlayerSkinsCompleted(player.id)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total: ${gameState.getTotalScore(player.id)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // +/- buttons for score
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { onScoreChanged(player.id, -1) },
                                    enabled = currentScore > 1
                                ) {
                                    Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Text(
                                    text = currentScore.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                IconButton(
                                    onClick = { onScoreChanged(player.id, 1) }
                                ) {
                                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isViewingPast && allScoresEntered) {
            Button(
                onClick = onNextHole,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.next_hole))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onFinishGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.finish_game))
            }
        }
        
        // Round summary button
        if (!isViewingPast) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onFinishGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.round_summary))
            }
        }
    }
}

@Composable
fun GameSummaryScreen(
    gameState: GameState,
    onNewGame: () -> Unit,
    onViewHole: (Int) -> Unit
) {
    val skins = gameState.calculateSkins()
    val playerSkins = gameState.players.associate { 
        it.id to gameState.getPlayerSkins(it.id) 
    }
    val unclaimedSkins = gameState.getUnclaimedSkins()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.skins_summary),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.total_skins),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                gameState.players
                    .sortedByDescending { playerSkins[it.id] ?: 0 }
                    .forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Score: ${gameState.getTotalScore(player.id)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "${playerSkins[player.id] ?: 0}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                
                // Show unclaimed skins if any
                if (unclaimedSkins > 0) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = stringResource(R.string.unclaimed_skins, unclaimedSkins),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.playoff_needed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Text(
            text = "Hole by Hole",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(skins) { skinResult ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onViewHole(skinResult.holeNumber) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.hole_number, skinResult.holeNumber),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        if (skinResult.winnerId != null) {
                            val winner = gameState.players.find { it.id == skinResult.winnerId }
                            Text(
                                text = "${winner?.name} (${skinResult.skinsValue})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.carried_over),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.new_game))
        }
    }
}
