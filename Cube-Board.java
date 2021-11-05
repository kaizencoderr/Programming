package jump61;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;

import java.util.function.Consumer;

import static jump61.Side.*;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 */
class Board {

    /**
     * An uninitialized Board.  Only for use by subtypes.
     */
    protected Board() {
        _notifier = NOP;
    }

    /**
     * An N x N board in initial configuration.
     */
    Board(int N) {
        this();
        _N = N;
        _jumpbool = false;
        _currboard = initializeboard(N);
        _history = new ArrayList<>();
        _current = 0;
    }

    /**
     * A board whose initial contents are copied from BOARD0, but whose
     * undo history is clear, and whose notifier does nothing.
     */
    Board(Board board0) {
        this(board0.size());
        _jumpbool = false;
        _N = board0.size();
        this.copy(board0);
        _readonlyBoard = new ConstantBoard(this);
    }

    /**
     * Returns a readonly version of this board.
     */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /**
     * (Re)initialize me to a cleared board with N squares on a side. Clears
     * the undo history and sets the number of moves to 0.
     */
    void clear(int N) {
        _currboard = initializeboard(N);
        _history = new ArrayList<>();
        _current = 0;
        _N = N;
        announce();
    }

    /**
     * Copy the contents of BOARD into me.
     */
    void copy(Board board) {
        this.internalCopy(board);
        this._history = new ArrayList<>();
        this._current = 0;
    }

    /**
     * Copy the contents of BOARD into me, without modifying my undo
     * history. Assumes BOARD and I have the same size.
     */
    private void internalCopy(Board board) {
        assert size() == board.size();
        int sqcounter = 0;
        for (int i = 0; i < _N; i++) {
            for (int j = 0; j < _N; j++) {
                Side side = board.get(sqcounter).getSide();
                int spots = board.get(sqcounter).getSpots();
                this._currboard[i][j] = square(side, spots);
                sqcounter++;
            }
        }
    }

    /**
     * Return the number of rows and of columns of THIS.
     */
    int size() {
        return _currboard.length;
    }

    /**
     * Initialize and return a @newboard. @N is the size of the board.
     */
    private Square[][] initializeboard(int N) {
        Square[][] newboard = new Square[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                newboard[i][j] = Square.INITIAL;
            }
        }
        return newboard;
    }

    /**
     * Returns the contents of the square at row R, column C
     * 1 <= R, C <= size ().
     */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /**
     * Returns the contents of square #N, numbering squares by rows, with
     * squares in row 1 number 0 - size()-1, in row 2 numbered
     * size() - 2*size() - 1, etc.
     */
    Square get(int n) {
        int row = row(n);
        int col = col(n);
        return _currboard[row - 1][col - 1];
    }

    /**
     * Returns the total number of spots on the board.
     */
    int numPieces() {
        int total = 0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                total += _currboard[i][j].getSpots();
            }
        }
        return total;
    }

    /**
     * Returns the Side of the player who would be next to move.  If the
     * game is won, this will return the loser (assuming legal position).
     */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /**
     * Return true iff row R and column C denotes a valid square.
     */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /**
     * Return true iff S is a valid square number.
     */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /**
     * Return the row number for square #N.
     */
    final int row(int n) {
        return n / size() + 1;
    }

    /**
     * Return the column number for square #N.
     */
    final int col(int n) {
        return n % size() + 1;
    }

    /**
     * Return the square number of row R, column C.
     */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /**
     * Return a string denoting move (ROW, COL)N.
     */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /**
     * Return a string denoting move N.
     */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /**
     * Returns true iff it would currently be legal for PLAYER to add a spot
     * to square at row R, column C.
     */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /**
     * Returns true iff it would currently be legal for PLAYER to add a spot
     * to square #N.
     */
    boolean isLegal(Side player, int n) {
        if (n >= _currboard.length * _currboard.length) {
            return false;
        } else {
            Square square = get(n);
            Side side = square.getSide();
            return player.playableSquare(side);
        }
    }

    /**
     * Returns true iff PLAYER is allowed to move at this point.
     */
    boolean isLegal(Side player) {
        for (int i = 0; i < _currboard.length; i++) {
            for (int j = 0; j < _currboard.length; j++) {
                if (!player.equals(_currboard[i][j].getSide())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the winner of the current position, if the game is over,
     * and otherwise null.
     */
    final Side getWinner() {
        if (!isLegal(RED)) {
            return RED;
        } else if (!isLegal(BLUE)) {
            return BLUE;
        } else {
            return null;
        }
    }

    /**
     * Return the number of squares of given SIDE.
     */
    int numOfSide(Side side) {
        int total = 0;
        for (int i = 0; i < _currboard.length; i++) {
            for (int j = 0; j < _currboard.length; j++) {
                if (side.equals(_currboard[i][j].getSide())) {
                    total += _currboard[i][j].getSpots();
                }
            }
        }
        return total;
    }

    /**
     * Add a spot from PLAYER at row R, column C.  Assumes
     * isLegal(PLAYER, R, C).
     */
    void addSpot(Side player, int r, int c) {
        if (_current == 0) {
            Board x = new Board(_N);
            x.internalCopy(this);
            _history.add(0, x);
        }
        if (!isLegal(player, r, c) && !_jumpbool) {
            throw new GameException("not legal for player to add spot");
        } else {
            int currspots = get(r, c).getSpots();
            if (neighbors(r, c) == currspots) {
                internalSet(r, c, 1, player);
                jump(sqNum(r, c));
            } else {
                internalSet(r, c, currspots + 1, player);
            }
        }
        if (!_jumpbool) {
            Board x = new Board(_N);
            x.internalCopy(this);
            _current++;
            _history.add(_current, x);
        }
    }

    /**
     * Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N).
     */
    void addSpot(Side player, int n) {
        int row = row(n);
        int col = col(n);
        addSpot(player, row, col);
    }

    /**
     * Set the square at row R, column C to NUM spots (0 <= NUM), and give
     * it color PLAYER if NUM > 0 (otherwise, white).
     */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /**
     * Set the square at row R, column C to NUM spots (0 <= NUM), and give
     * it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     * changes.
     */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /**
     * Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     * if NUM > 0 (otherwise, white). Does not announce changes.
     */
    private void internalSet(int n, int num, Side player) {
        Square sq = get(n);
        int row = row(n);
        int col = col(n);
        if (num > 0) {
            _currboard[row - 1][col - 1] = sq.getSquare(player, num);
        } else {
            _currboard[row - 1][col - 1] = sq.getSquare(WHITE, num);
        }
    }


    /**
     * Undo the effects of one move (that is, one addSpot command).  One
     * can only undo back to the last point at which the undo history
     * was cleared, or the construction of this Board.
     */
    void undo() {
        if (_current == 0) {
            throw new GameException("cannot undo, numMoves is 0");
        } else {
            _current -= 1;
            Board previous = _history.get(_current);
            _currboard = previous._currboard;
        }
    }

    /**
     * Record the beginning of a move in the undo history.
     */
    private void markUndo() {
    }

    /**
     * Add DELTASPOTS spots of side PLAYER to row R, column C,
     * updating counts of numbers of squares of each color.
     */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /**
     * Add DELTASPOTS spots of color PLAYER to square #N,
     * updating counts of numbers of squares of each color.
     */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /**
     * Used in jump to keep track of squares needing processing.  Allocated
     * here to cut down on allocations.
     */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /**
     * Do all jumping on this board, assuming that initially, S is the only
     * square that might be over-full.
     */
    private void jump(int S) {
        Side side = get(S).getSide();
        int max = _N * _N;
        int col = col(S);
        int row = row(S);
        if (getWinner() == null) {
            if (S + 1 < max && col < size()) {
                _jumpbool = true;
                addSpot(side, S + 1);
                _jumpbool = false;
            }
            if (S + _N < max && row < size()) {
                _jumpbool = true;
                addSpot(side, S + _N);
                _jumpbool = false;
            }
            if (S - 1 >= 0 && col > 1) {
                _jumpbool = true;
                addSpot(side, S - 1);
                _jumpbool = false;
            }
            if (S - _N >= 0 && row > 1) {
                _jumpbool = true;
                addSpot(side, S - _N);
                _jumpbool = false;
            }
        }
    }

    /**
     * Returns my dumped representation.
     */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int i = 0; i < _N; i++) {
            out.format("    ");
            for (int j = 0; j < _N; j++) {
                Square sq = _currboard[i][j];
                Side side = sq.getSide();
                String sidestr = "";
                if (side == WHITE) {
                    sidestr = "-";
                } else if (side == RED) {
                    sidestr = "r";
                } else if (side == BLUE) {
                    sidestr = "b";
                }
                out.format("%d%s ", sq.getSpots(), sidestr);
            }
            out.format("%n");
        }
        out.format("===%n");
        return out.toString();
    }

    /**
     * Returns an external rendition of me, suitable for human-readable
     * textual display, with row and column numbers.  This is distinct
     * from the dumped representation (returned by toString).
     */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /**
     * Returns the number of neighbors of the square at row R, column C.
     */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /**
     * Returns the number of neighbors of square #N.
     */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            return this._currboard.equals(B._currboard);
        }
    }

    @Override
    public int hashCode() {
        return numPieces();
    }

    /**
     * Set my notifier to NOTIFY.
     */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /**
     * Take any action that has been set for a change in my state.
     */
    private void announce() {
        _notifier.accept(this);
    }

    /**
     * A notifier that does nothing.
     */
    private static final Consumer<Board> NOP = (s) -> {
    };

    /**
     * A read-only version of this Board.
     */
    private ConstantBoard _readonlyBoard;

    /**
     * Use _notifier.accept(B) to announce changes to this board.
     */
    private Consumer<Board> _notifier;

    /**
     * An int representing number of rows or number of columns.
     */
    private int _N;

    /**
     * A 2d enum array of my board, containing associated colors and spots.
     */
    private Square[][] _currboard;

    /**
     * A sequence of Board states.  The initial Board is at index 0.
     * _history[_current] is equal to the current Board state.
     * _history[_current+1] through _history[_lastHistory] are undone
     * states that can be redone.  _lastHistory is reset to _current after
     * each move.  _history only expands: there can be more than
     * _lastHistory+1 elements in it at any time, with those following
     * _lastHistory being available for re-use.  This is basically an
     * optimization to avoid constant allocation and deallocation of
     * arrays.
     */
    private ArrayList<Board> _history;

    /**
     * The position of the current state in _history.  This is always
     * non-negative and <=_lastHistory.
     */
    private int _current;

    /**
     * boolean to indicate whether a player .
     */
    private boolean _jumpbool;
}
