package jump61;

import java.util.ArrayList;
import java.util.Random;

import static jump61.Side.*;

/** An automated Player.
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();

        assert getSide() == board.whoseMove();
        int choice = searchForMove();
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        assert getSide() == work.whoseMove();
        _bvalidmoves = new ArrayList<>();
        _rvalidmoves = new ArrayList<>();
        int infiniti = Math.round(Float.POSITIVE_INFINITY);
        int neginfiniti = Math.round(Float.NEGATIVE_INFINITY);
        _foundMove = -1;
        if (getSide() == RED) {
            _foundMove = minMax(work, 4, false, 1, infiniti, neginfiniti);
        } else {
            _foundMove = minMax(work, 4, false, -1, neginfiniti, infiniti);
        }
        return _foundMove;
    }

    /** Returns maximized value from our game tree at position BOARD.
     * DEPTH is the depth we are searching. SAVEMOVE describes
     * whether to save a move or not. SENSE tells us which player.
     * ALPHA is our best outcome BETA is our worst outcome.*/
    private int maxplayerval(Board board, int depth, boolean saveMove,
                          int sense, int alpha, int beta) {
        if (board.getWinner() != null || depth == 0) {
            return staticEval(board, _foundMove);
        }
        int bestSoFar = Math.round(Float.NEGATIVE_INFINITY);
        for (int m = 0; m < _rvalidmoves.size(); m++) {
            Board next = new Board(board.size());
            next.copy(board);
            next.addSpot(RED, m);
            int response = minplayerval(next, depth - 1, false,
                    -sense, alpha, beta);
            if (response > bestSoFar) {
                bestSoFar = response;
                alpha = Math.max(alpha, bestSoFar);
                if (alpha >= beta) {
                    return bestSoFar;
                }
            }
        }
        return bestSoFar;
    }

    /** Returns minimized value from our game tree at position BOARD.
     * DEPTH is the depth we are searching. SAVEMOVE describes
     * whether to save a move or not. SENSE tells us which player.
     * ALPHA is our best outcome BETA is our worst outcome.*/
    private int minplayerval(Board board, int depth, boolean saveMove,
                          int sense, int alpha, int beta) {
        if (board.getWinner() != null || depth == 0) {
            return staticEval(board, _foundMove);
        }
        int bestSoFar = Math.round(Float.POSITIVE_INFINITY);
        for (int m = 0; m < _bvalidmoves.size(); m++) {
            Board next = new Board(board.size());
            next.copy(board);
            next.addSpot(BLUE, m);
            int response = maxplayerval(next, depth - 1, false,
                    -sense, alpha, beta);
            if (response < bestSoFar) {
                bestSoFar = response;
                beta = Math.min(alpha, bestSoFar);
                if (alpha >= beta) {
                    return bestSoFar;
                }
            }
        }
        return bestSoFar;

    }


    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        int brdsize = board.size();
        if (sense == -1) {
            for (int i = 0; i < brdsize * brdsize; i++) {
                Square currsq = board.get(i);
                Side currside = currsq.getSide();
                if (!currside.equals(RED)) {
                    _bvalidmoves.add(i);
                }
            }
            _foundMove = _bvalidmoves.get(_bvalidmoves.size() - 1);
            return _foundMove;
        } else if (sense == 1) {
            for (int i = 0; i < brdsize * brdsize; i++) {
                Square currsq = board.get(i);
                Side currside = currsq.getSide();
                if (!currside.equals(BLUE)) {
                    _rvalidmoves.add(i);
                }
            }
            _foundMove = _rvalidmoves.get(_rvalidmoves.size() - 1);
            return _foundMove;
        } else {
            throw new GameException("sense must equal either -1 or 1");
        }
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        int heuristic = b.numOfSide(RED) - b.numOfSide(BLUE);
        return heuristic;
    }


    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;

    /** An array that holds the possible moves
     * that red can make at a given position. */
    private ArrayList<Integer> _rvalidmoves;

    /** An array that holds the possible moves
     * that blue can make at a given position. */
    private ArrayList<Integer> _bvalidmoves;
}
