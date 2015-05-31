Project Timeline- December 2014

######Details
The Resources specify the basic game rules. The current implementation is for a class submission thus it's 5x5 (requirement) instead of the tradiditional 5x9 format. Other relevant information for the specific implementation is as follows:

1. A piece can only move from one intersection to an adjacent one along the indicated lines (vertically, horizontally, or diagonally.)
2. The two players alternate their turns making moves, starting with White.
3. There are two kinds of moves: non-capturing and capturing. A non-capturing move is called a Paika move.
4. A Paika move consists of moving one piece along a line to an adjacent intersection.
5. Capturing moves are mandatory and have to be played in preference to Paika moves.
6. A capturing move removes one or more pieces of the opponent. There are two types of capturing moves:
  1. Approach – move a token to an intersection adjacent to the opponent's token; the opponent’s token must lie along the line in the direction of the capturing token’s move.
  2. Withdrawal – move a token away from an intersection adjacent to the opponent's token; the opponent’s token must lie along the line in the (opposite) direction of the capturing token’s move.
7. When an opponent’s piece is captured, all consecutive opponent pieces that lie along the line beyond that piece are captured as well.
8. Successive captures are optional. A player can also choose to stop at any point during a sequence of successive captures, even the board configuration allows him/her to continue with the capture. But both the White and Black player have the option to make successive captures, with these restrictions:
  1. The piece is not allowed to arrive at the same position twice.
  2. It is not permitted to move twice consecutively in the same direction (first to make a withdrawal capture, and then to make an approach capture) as part of a capturing sequence.
9. An approach capture and a withdrawal capture cannot be made in the same move. A player must choose one or the other.
10. The game ends when one player captures all the pieces of the opponent. If neither player can achieve this – for instance, if the game reaches a state where neither player can attack the other without overly weakening their own position – then the game is a draw.

######Implementation Extras
1. DECLARING A DRAW: If more than 5 paika moves are made consecutively then the gameplay is declared as draw. This also ensures that the players don’t keep moving around the same positions indefinitely. Tunable based on heap space of machine + desired search tree depath you want to go till

2. The configuration board is printed to console after each turn, please enter valid indices in the format specified (at Run time) otherwise the turn will be lost. This is only to discourage human error and does not reflect upon game rule implementation.

3. EVALUATION FUNCTION: Ideally cutoff needs to be implemented if alpha beta search takes a significant amount of time to find a solution. My program returns the move within one second thus I did not implement evaluation here is a proposed if required
  1. SIGNATURE: *Function Evaluation(char turn, char[][] before, char[][] after) returns utility
  2. program already implements a function token counter that takes in configuration of the board and returns the count of whichChar, using that function if before alphaBetaSearch is called and at cutoff level if tokenCtr of turn reduces then return -1 (opponent is winning), iF tokenCtr of opponent reduces then +1 (opponent is losing) other wise return 0 (series of paika moves).
