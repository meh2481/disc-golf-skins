# Disc Golf Skins Tracker

An Android app for tracking scores in a disc golf skins match.

## What is a Skins Match?

A "skins" match is a popular disc golf competition format where:
- Each hole has a prize (a "skin")
- The player with the lowest score on a hole wins that skin
- If there's a tie, the skin carries over to the next hole (making it worth more)
- At the end, the player with the most skins wins

## Features

- **Player Management**: Add and remove players before starting a game (minimum 2 players required)
- **Score Tracking**: Enter scores for each player on each hole
- **Real-time Skins Count**: See how many skins each player has won during the game
- **Carry-over Logic**: Automatically handles tied holes and carries skins forward
- **Game Summary**: View final results with total skins won and hole-by-hole breakdown

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: Compose state management
- **Minimum Android Version**: API 24 (Android 7.0)
- **Target Android Version**: API 34 (Android 14)

## Building the App

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK with API level 34
- JDK 8 or higher

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/meh2481/disc-golf-skins.git
   cd disc-golf-skins
   ```

2. Open the project in Android Studio

3. Wait for Gradle to sync and download dependencies

4. Build and run:
   - Click the "Run" button (green play icon)
   - Or use the command line: `./gradlew assembleDebug`

## How to Use

1. **Start Screen**: 
   - Enter player names and tap "Add Player"
   - Add at least 2 players
   - Remove players by tapping "Remove" if needed
   - Tap "Start Game" when ready

2. **Score Entry**:
   - Enter the score for each player on the current hole
   - The app shows each player's current total skins
   - Once all scores are entered, tap "Next Hole" to continue
   - Or tap "Finish Game" to end the match

3. **Game Summary**:
   - View the total skins won by each player
   - See hole-by-hole results showing who won each skin
   - Tied holes are marked as "Carried Over"
   - Tap "New Game" to start over

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/discgolfskins/
│   │   ├── MainActivity.kt          # Main activity with UI screens
│   │   └── GameState.kt             # Data models and game logic
│   ├── res/
│   │   ├── values/
│   │   │   ├── strings.xml          # String resources
│   │   │   ├── colors.xml           # Color definitions
│   │   │   └── themes.xml           # App theme
│   │   ├── drawable/                # Drawable resources
│   │   └── mipmap-*/                # Launcher icons
│   └── AndroidManifest.xml          # App manifest
└── build.gradle                      # App-level build configuration
```

## License

This is free and unencumbered software released into the public domain. See LICENSE for details.
