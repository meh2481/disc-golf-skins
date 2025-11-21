# Contributing to Disc Golf Skins Tracker

Thank you for your interest in contributing to the Disc Golf Skins Tracker! This document provides guidelines and information for contributors.

## Development Setup

1. **Prerequisites**
   - Android Studio (Arctic Fox or later)
   - JDK 8 or higher
   - Android SDK with API level 34
   - Git

2. **Clone and Build**
   ```bash
   git clone https://github.com/meh2481/disc-golf-skins.git
   cd disc-golf-skins
   ```
   
3. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository
   - Wait for Gradle sync to complete

## Project Architecture

### Structure Overview

```
app/src/main/java/com/example/discgolfskins/
├── MainActivity.kt      # Main activity with all Compose UI screens
└── GameState.kt         # Data models and business logic
```

### Key Components

1. **GameState.kt**
   - `Player`: Represents a player with name and ID
   - `Hole`: Represents a hole with scores for each player
   - `SkinResult`: Result of skins calculation for a hole
   - `GameState`: Main state container with game logic
     - `calculateSkins()`: Determines skin winners with carry-over logic
     - `getPlayerSkins()`: Calculates total skins for a player

2. **MainActivity.kt**
   - `DiscGolfSkinsApp`: Main composable that manages screen navigation
   - `PlayerSetupScreen`: UI for adding/removing players
   - `ScoreEntryScreen`: UI for entering scores per hole
   - `GameSummaryScreen`: UI for displaying final results

## Development Guidelines

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and concise

### UI/UX Principles

- Use Material Design 3 components
- Ensure responsive layouts that work on different screen sizes
- Provide clear feedback for user actions
- Maintain consistent spacing and typography

### Testing

- Write unit tests for business logic in `GameState`
- Run tests before submitting PR: `./gradlew test`
- Ensure all tests pass

### Making Changes

1. **Fork the repository**
   - Click "Fork" on GitHub
   - Clone your fork locally

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Write clean, documented code
   - Add tests for new functionality
   - Update README if needed

4. **Test your changes**
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "Description of your changes"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request**
   - Go to the original repository on GitHub
   - Click "New Pull Request"
   - Describe your changes and why they're needed

## Feature Ideas

Here are some ideas for potential enhancements:

- [ ] Save/load games (using Room database or SharedPreferences)
- [ ] Multiple game modes (gross skins, net skins, etc.)
- [ ] Player statistics and history
- [ ] Custom hole count (9-hole, 18-hole, custom)
- [ ] Dark theme support
- [ ] Export results (share, CSV, etc.)
- [ ] Undo/edit previous hole scores
- [ ] Multiple concurrent games
- [ ] Course information integration
- [ ] Betting/payment tracking

## Questions or Issues?

- Open an issue on GitHub for bugs or feature requests
- Check existing issues before creating new ones
- Provide as much detail as possible for bugs (steps to reproduce, expected vs actual behavior, screenshots)

## License

By contributing, you agree that your contributions will be licensed under the same Unlicense as the project.
