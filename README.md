# Scotland Yard Project

An implementation of the Scotland Yard board game with an AI player for Mr X, built using Java and JavaFX.

## Overview

This project implements the classic Scotland Yard game where detectives try to catch Mr X as he moves around London. The implementation includes:

- A complete game model with move generation and state management
- A graphical user interface built with JavaFX
- An AI implementation for playing as Mr X using minimax algorithm

## Project Structure

The project consists of two main modules:

- **`cw-model`**: Core game model and UI implementation
  - Game state management (`MyGameState`)
  - Move generation and validation
  - JavaFX-based graphical interface
  - Main entry point: `uk.ac.bris.cs.scotlandyard.Main`

- **`cw-ai_no_downcast`**: AI implementation for Mr X
  - Minimax algorithm implementation (`MinMaxTree`)
  - AI player class (`MyAi`)
  - Remote game support (`Remote`)

## Requirements

- Java 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- JavaFX 22 (included as dependency)

## Building the Project

### Build the Model Module

```bash
cd cw-model
mvn clean compile
```

Or using the Maven wrapper:

```bash
cd cw-model
./mvnw clean compile  # On Unix/Mac
mvnw.cmd clean compile  # On Windows
```

### Build the AI Module

```bash
cd cw-ai_no_downcast
mvn clean compile
```

## Running the Game

### Run the Main Game

To play the game with the graphical interface:

```bash
cd cw-model
mvn exec:java
```

Or using the Maven wrapper:

```bash
cd cw-model
./mvnw exec:java  # On Unix/Mac
mvnw.cmd exec:java  # On Windows
```

The main class is located at: `uk.ac.bris.cs.scotlandyard.Main`

## Running Tests

To run the test suite for the model:

```bash
cd cw-model
mvn test
```

## Implementation Details

### Game State Implementation

The `MyGameState` class implements the `GameState` interface and provides:

- **`build()` method**: Validates input parameters, checks for null references, and initializes the game state with Mr X as the first player
- **Getter methods**: Returns game state attributes and player information
- **`getAvailableMoves()` method**: Generates an immutable set of all possible moves for the current player, using helper functions for:
  - Single moves
  - Double moves
  - Validation based on the ValueGraph, other player locations, and available tickets
- **`advance()` method**: Processes a move and returns a new game state using the Visitor design pattern:
  - Handles single and double moves separately
  - Validates move legality
  - Updates player location and ticket counts
  - Returns a new immutable game state

### Design Patterns

- **Visitor Pattern**: Used in the `advance()` method to handle different move types
- **Factory Pattern**: `MyGameStateFactory` and `MyModelFactory` for creating game instances
- **Immutable Objects**: Game states are immutable, ensuring thread safety and preventing accidental modifications

### AI Implementation

The AI module implements a minimax algorithm to play as Mr X, evaluating possible moves and selecting the optimal strategy.

## License

See `cw-model/src/main/resources/LICENSE.txt` for license information.

## Additional Resources

- Game manual: `cw-model/src/main/resources/manual/`
- Class diagrams: `cw-model/class diagram 1.png` and `cw-model/big class diagram.png`
- Project report: `report.pdf`
