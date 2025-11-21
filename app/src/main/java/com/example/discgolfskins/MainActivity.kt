package com.example.discgolfskins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    when {
        !gameState.isGameStarted -> {
            PlayerSetupScreen(
                players = gameState.players,
                onAddPlayer = { name ->
                    val newPlayer = Player(name, gameState.players.size)
                    gameState = gameState.copy(
                        players = gameState.players + newPlayer
                    )
                },
                onRemovePlayer = { player ->
                    gameState = gameState.copy(
                        players = gameState.players.filter { it.id != player.id }
                    )
                },
                onStartGame = {
                    if (gameState.players.size >= 2) {
                        gameState = gameState.copy(isGameStarted = true)
                    }
                }
            )
        }
        gameState.isGameFinished -> {
            GameSummaryScreen(
                gameState = gameState,
                onNewGame = {
                    gameState = GameState()
                }
            )
        }
        else -> {
            ScoreEntryScreen(
                gameState = gameState,
                onScoreEntered = { playerId, score ->
                    val currentHoleIndex = gameState.currentHole - 1
                    val holes = gameState.holes.toMutableList()
                    
                    // Ensure we have enough holes
                    while (holes.size <= currentHoleIndex) {
                        holes.add(Hole(holes.size + 1))
                    }
                    
                    val currentHole = holes[currentHoleIndex]
                    val updatedScores = currentHole.scores.toMutableMap()
                    updatedScores[playerId] = score
                    holes[currentHoleIndex] = currentHole.copy(scores = updatedScores)
                    
                    gameState = gameState.copy(holes = holes)
                },
                onNextHole = {
                    gameState = gameState.copy(currentHole = gameState.currentHole + 1)
                },
                onFinishGame = {
                    gameState = gameState.copy(isGameFinished = true)
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
    onStartGame: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }

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
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    onAddPlayer(playerName)
                    playerName = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_player))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Players (${players.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(players) { player ->
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
                        TextButton(onClick = { onRemovePlayer(player) }) {
                            Text(stringResource(R.string.remove))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth(),
            enabled = players.size >= 2
        ) {
            Text(stringResource(R.string.start_game))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreEntryScreen(
    gameState: GameState,
    onScoreEntered: (Int, Int) -> Unit,
    onNextHole: () -> Unit,
    onFinishGame: () -> Unit
) {
    val currentHoleIndex = gameState.currentHole - 1
    val currentHole = gameState.holes.getOrNull(currentHoleIndex)
    val allScoresEntered = gameState.players.all { player ->
        currentHole?.scores?.containsKey(player.id) == true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.hole_number, gameState.currentHole),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(gameState.players) { player ->
                val currentScore = currentHole?.scores?.get(player.id)
                var scoreText by remember(gameState.currentHole) { 
                    mutableStateOf(currentScore?.toString() ?: "") 
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                text = "Skins: ${gameState.getPlayerSkins(player.id)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = scoreText,
                            onValueChange = { newValue ->
                                scoreText = newValue
                                newValue.toIntOrNull()?.let { score ->
                                    if (score > 0) {
                                        onScoreEntered(player.id, score)
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.enter_score)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allScoresEntered) {
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
    }
}

@Composable
fun GameSummaryScreen(
    gameState: GameState,
    onNewGame: () -> Unit
) {
    val skins = gameState.calculateSkins()
    val playerSkins = gameState.players.associate { 
        it.id to gameState.getPlayerSkins(it.id) 
    }

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
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${playerSkins[player.id] ?: 0}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        .padding(vertical = 4.dp)
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
