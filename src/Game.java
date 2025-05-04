public class Game {
    /**
     * Originally by Pascal Pons, adapted by Isaac Yee
     * (I made sure to understand how it works on his <a href="http://blog.gamesolver.org/solving-connect-four">blog page</a>)
     */
    public static class Position {
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

        public Position() {
            pos = 0;
            mask = 0;
            moves = 0;
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

    public static class Move {
        private final short score, col;

        public Move(short score, short col) {
            this.score = score;
            this.col = col;
        }

        public static Move unknown(short score) {
            return new Move(score, (short) -1);
        }

        public static Move any(short score) {
            return new Move(score, (short) -2);
        }

        public short getScore() {
            return score;
        }

        public short getCol() {
            return col;
        }
    }

    private static class MoveSorter {
        private final Move[] moves = new Move[Position.WIDTH];
        private int size;

        public void add(short score, short col) {
            int i = size++;
            for (; i > 0 && moves[i - 1].getScore() > score; i--) moves[i] = moves[i - 1];
            moves[i] = new Move(score, col);
        }

        public short getNext() {
            return size > 0 ? moves[--size].getCol() : 0;
        }

        public boolean hasNext() {
            return size > 0;
        }
    }

    private static class TransTable {
        private static final int SIZE = (1 << 23) + 9;
        private final long[] keys = new long[SIZE];
        private final short[] scores = new short[SIZE];
        private final short[] cols = new short[SIZE];

        private int idx(long key) {
            return (int) (key % SIZE);
        }

        public void put(long key, short score, short col) {
            int i = idx(key);
            keys[i] = key;
            scores[i] = score;
            cols[i] = col;
        }

        public short getScore(long key) {
            int i = idx(key);
            return keys[i] == key ? scores[i] : 0;
        }

        public short getCol(long key) {
            int i = idx(key);
            return keys[i] == key ? cols[i] : -1;
        }
    }

    private final TransTable table = new TransTable();
    private final short[] COLUMN_ORDER = {3, 4, 2, 5, 1, 6, 0};

    public Move solve(Position pos) {
        // check immediate win
        short c = pos.canWinNext();
        if (c != -1) return new Move((short) (Position.WIDTH * Position.HEIGHT + 1 - pos.getMoves() / 2), c);

        // restrict to moves remaining
        short min = (short) (-(Position.WIDTH * Position.HEIGHT - pos.getMoves()) / 2);
        short max = (short) ((Position.WIDTH * Position.HEIGHT + 1 - pos.getMoves()) / 2);

        while (min < max) {
            short med = (short) (min + (max - min) / 2);
            if (med <= 0 && min / 2 < med) med = (short) (min / 2);
            else if (med >= 0 && max / 2 > med) med = (short) (max / 2);
            // null window search for better/worse score to narrow min/max
            Move move = negamax(pos, med, (short) (med + 1));
            // adjust window
            if (move.getScore() <= med) max = move.getScore();
            else min = move.getScore();
        }
        // negamax to find best move with correct a/b
        return negamax(pos, (short) (min - 1), (short) (max + 1));
    }

    private Move negamax(Position pos, short a, short b) {
        assert a < b;
        assert pos.canWinNext() == -1;

        // check next possible moves
        long next = pos.possibleNonLosingMoves();
        if (next == 0) // no possible non-losing moves (fork)
            return Move.any((short) (-(Position.WIDTH * Position.HEIGHT - pos.getMoves()) / 2));

        // tie
        if (pos.getMoves() == Position.WIDTH * Position.HEIGHT) return Move.any((short) 0);

        // min score: move now - 2 moves (since opp no immediate win) + optimize window
        short min = (short) (-(Position.WIDTH * Position.HEIGHT - 2 - pos.getMoves()) / 2);
        if (a < min) {
            a = min;
            if (a >= b) return Move.unknown(a);
        }

        // max score: move now - 2 moves (since no immediate win) + optimize window
        short max = (short) ((Position.WIDTH * Position.HEIGHT - 1 - pos.getMoves()) / 2);
        short score = table.getScore(pos.getKey());
        if (score != 0) max = score;
        short c = table.getCol(pos.getKey());
        if (b > max) {
            b = max;
            if (a >= b) return new Move(b, c);
        }

        // add to move sorter
        MoveSorter sorter = new MoveSorter();
        for (int i = Position.WIDTH; i-- > 0; ) {
            long move = next & Position.colMask(COLUMN_ORDER[i]);
            if (move > 0)
                sorter.add(pos.moveScore(move), COLUMN_ORDER[i]);
        }

        // iterate through all possible next moves, saving best move
        while (sorter.hasNext()) {
            c = sorter.getNext();
            Position pos2 = pos.clone();
            pos2.play(c);
            // calculate opponent's score after playing that move
            score = (short) -(negamax(pos2, (short) -b, (short) -a).getScore());
            // exit early if score is better than our window
            if (score >= b) return new Move(score, c);
            // optimize window to look for moves better than this
            if (score > a) a = score;
        }

        table.put(pos.getKey(), a, c);
        return new Move(a, c);
    }
}
