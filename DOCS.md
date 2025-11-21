# App Flow and Screenshots

## Application Flow

```
┌─────────────────────────┐
│  Player Setup Screen    │
│                         │
│  • Add Players          │
│  • Remove Players       │
│  • Start Game (min 2)   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Score Entry Screen     │
│                         │
│  • Enter scores/hole    │
│  • View current skins   │
│  • Next Hole           │
│  • Finish Game         │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Game Summary Screen    │
│                         │
│  • Total skins/player   │
│  • Hole-by-hole results │
│  • New Game            │
└─────────────────────────┘
```

## Screen Details

### Player Setup Screen
- **Purpose**: Configure players before starting the game
- **Actions**:
  - Enter player name in text field
  - Tap "Add Player" to add to list
  - Tap "Remove" next to a player to delete
  - Tap "Start Game" when ready (requires ≥2 players)
- **Validation**: Start Game button is disabled until at least 2 players are added

### Score Entry Screen
- **Purpose**: Record scores for each hole during play
- **Display**:
  - Current hole number at top
  - List of all players with:
    - Player name
    - Current total skins count
    - Score input field
- **Actions**:
  - Enter numeric scores for each player
  - Tap "Next Hole" when all scores entered (advances to next hole)
  - Tap "Finish Game" to end the match
- **Validation**: Next Hole button only appears when all players have scores entered

### Game Summary Screen
- **Purpose**: Display final results
- **Display**:
  - Total skins won by each player (sorted by most skins)
  - Hole-by-hole breakdown:
    - Hole number
    - Winner name (or "Carried Over" for ties)
    - Skin value for that hole
- **Actions**:
  - Tap "New Game" to restart with new players

## Skins Calculation Logic

### Basic Rules
1. **Single Winner**: Lowest score on a hole wins 1 skin
2. **Tie**: Skin carries over to next hole (adds value)
3. **Carried Skins**: Winner gets base skin + all carried skins

### Examples

#### Example 1: Simple Win
```
Hole 1: Alice=3, Bob=4
Result: Alice wins 1 skin
```

#### Example 2: Carry-over
```
Hole 1: Alice=3, Bob=3 (TIE)
Hole 2: Alice=3, Bob=4
Result: Alice wins 2 skins (1 for hole 2 + 1 carried from hole 1)
```

#### Example 3: Multiple Carry-overs
```
Hole 1: Alice=3, Bob=3 (TIE)
Hole 2: Alice=4, Bob=4 (TIE)
Hole 3: Alice=5, Bob=4
Result: Bob wins 3 skins (1 for hole 3 + 2 carried from holes 1 & 2)
```

#### Example 4: Three Players
```
Hole 1: Alice=3, Bob=4, Charlie=5
Result: Alice wins 1 skin

Hole 2: Alice=4, Bob=4, Charlie=3
Result: Charlie wins 1 skin

Hole 3: Alice=3, Bob=3, Charlie=5 (TIE between Alice and Bob)
Result: Carried over

Hole 4: Alice=5, Bob=4, Charlie=6
Result: Bob wins 2 skins (1 for hole 4 + 1 carried from hole 3)

Final: Alice=1, Bob=2, Charlie=1
```

## Technical Implementation

### State Management
The app uses Jetpack Compose's `remember` and `mutableStateOf` for reactive state management. The `GameState` data class is the single source of truth.

### UI Components
- **Material Design 3**: Modern, accessible components
- **LazyColumn**: Efficient scrolling lists
- **TextField**: Score input with numeric keyboard
- **Button/OutlinedButton**: Primary and secondary actions
- **Card**: Grouped content display

### Data Flow
1. User actions trigger state updates
2. State changes trigger UI recomposition
3. Business logic in `GameState` calculates derived data (skins)
4. UI displays current state reactively
