/**
 * Originally by Pascal Pons, adapted by Isaac Yee
 * (I made sure to understand how it works on his <a href="http://blog.gamesolver.org/solving-connect-four">blog page</a>)
 */
public class Position {
    public static final short WIDTH = 7;
    public static final short HEIGHT = 6;

    public static final long BOTTOM = bottom(WIDTH, HEIGHT);
    public static final long BOARD = BOTTOM * ((1 << HEIGHT) - 1);

    // pos: player checkers, mask: all checkers
    private long pos, mask;

    private short moves;

    private static long bottom(int w, int h) {
        return w == 0 ? 0 : bottom(w - 1, h) | (1L << (w - 1) * (h + 1));
    }

    public Position(long pos, long mask, short moves) {
        this.pos = pos;
        this.mask = mask;
        this.moves = moves;
    }

    public Position(String[][] grid, String curr) {
        moves = 0;
        for (int j = 0; j < WIDTH; j++) {
            for (int i = 0; i < HEIGHT; i++) {
                mask |= (grid[i][j] == null ? 0L : 1L) << (j * (HEIGHT + 1) + (HEIGHT - i - 1));
                pos |= (grid[i][j] != null && grid[i][j].equals(curr) ? 1L : 0L) << (j * (HEIGHT + 1) + (HEIGHT - i - 1));
                if (grid[i][j] != null) moves++;
            }
        }
    }

    @Override
    public Position clone() {
        return new Position(pos, mask, moves);
    }

    public void play(int col) {
        // swaps player/opponent (because now opponent's turn)
        pos ^= mask;
        // only update mask bc opponent's move (not on player pos mask)
        mask |= mask + bottomMask(col);
        moves++;
    }

    public short moveScore(long move) {
        return popCount(calcWinPos(pos | move, mask));
    }

    private static short popCount(long b) {
        short c = 0;
        for (; b != 0; c++) b &= b - 1;
        return c;
    }

    public short canWinNext() {
        long win = winPos() & possible();
        for (short i = 0; i < WIDTH; i++)
            if ((win & colMask(i)) > 0) return i;
        return -1;
    }

    public short getMoves() {
        return moves;
    }

    public long getKey() {
        return pos + mask;
    }

    public long possibleNonLosingMoves() {
        assert canWinNext() == -1;
        long possible = possible();
        long oppWin = oppWinPos();
        long forced = possible & oppWin;
        if (forced > 0) {
            // check for more than 1 losing move (fork)
            if ((forced & (forced - 1)) > 0) return 0;
            // possible move is the one forced move
            else possible = forced;
        }
        // don't play below an opponent's win
        return possible & ~(oppWin >>> 1);
    }

    private long winPos() {
        return calcWinPos(pos, mask);
    }

    private long oppWinPos() {
        return calcWinPos(pos ^ mask, mask);
    }

    private long possible() {
        return (mask + BOTTOM) & BOARD;
    }

    private static long calcWinPos(long pos, long mask) {
        // vertical:
        long r = (pos << 1) & (pos << 2) & (pos << 3);

        // horizontal:
        long p = (pos << (HEIGHT + 1)) & (pos << 2 * (HEIGHT + 1));
        r |= p & (pos << 3 * (HEIGHT + 1));
        r |= p & (pos >>> (HEIGHT + 1));
        p >>>= 3 * (HEIGHT + 1);
        r |= p & (pos << (HEIGHT + 1));
        r |= p & (pos >>> 3 * (HEIGHT + 1));

        // diagonal 1:
        p = (pos << HEIGHT) & (pos << 2 * HEIGHT);
        r |= p & (pos << 3 * HEIGHT);
        r |= p & (pos >>> HEIGHT);
        p >>>= 3 * HEIGHT;
        r |= p & (pos << HEIGHT);
        r |= p & (pos >>> 3 * HEIGHT);

        // diagonal 2:
        p = (pos << (HEIGHT + 2)) & (pos << 2 * (HEIGHT + 2));
        r |= p & (pos << 3 * (HEIGHT + 2));
        r |= p & (pos >>> (HEIGHT + 2));
        p >>>= 3 * (HEIGHT + 2);
        r |= p & (pos << (HEIGHT + 2));
        r |= p & (pos >>> 3 * (HEIGHT + 2));

        return r & (BOARD ^ mask);
    }

    private static long bottomMask(int col) {
        // transpose cols
        return 1L << (col * (HEIGHT + 1));
    }

    public static long colMask(int col) {
        // to next col, then subtract for full first col, then transpose cols
        return ((1L << HEIGHT) - 1L) << (col * (HEIGHT + 1));
    }
}
