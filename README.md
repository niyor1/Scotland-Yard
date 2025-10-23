## Scotland Yard project

This project is an implementation of the Scotland Yard game, including an AI to play as Mr X.
Within the MyGameState factory file, we implemented the GameState interface using the
class MyGameState. To accomplish this, we first implemented the build() method, which
included validation such as checking for null references and correctly initialising the game
state. We ensured that the game state was returned with Mr X as the first player to move.
After this, we implemented the basic interface getter methods to return values of attributes
and other necessary information.
Next, we focused on move generation and updating the game state. To start, we fully
implemented the getAvailableMoves() method, which creates an immutable set of all possible
moves given the current game state. To accomplish this, we used helper functions for single
and double moves. In these helper functions, we checked the ValueGraph to determine valid
moves based on other player locations and the quantity of tickets available.
We then moved on to implementing the advance() method, which is vital for processing a
move and returning a new game state. In this method, we utilised the Visitor design pattern to
update the game state. This involved handling single and double moves separately. In each of
these methods, we first validated whether the chosen move was legal for the player making it.
Once validation was complete, we performed the move by updating the player’s location,
using the corresponding ticket, and modifying the relevant player attributes before returning a
new game state.
