public class Game {
    private static final TransTable table = new TransTable();
    private static final short[] COLUMN_ORDER = {3, 4, 2, 5, 1, 6, 0};

    public static Move negamax(Position pos, short a, short b, short C) {
        assert a < b;
        assert pos.canWinNext() == -1;

        // check next possible moves
        long next = pos.possibleNonLosingMoves();
        if (next == 0) // no possible non-losing moves (fork)
            return new Move((short) (-(Position.WIDTH * Position.HEIGHT - pos.getMoves()) / 2));

        // tie
        if (pos.getMoves() == Position.WIDTH * Position.HEIGHT) return new Move((short) 0);

        // min score: move now - 2 moves (since opp no immediate win) + optimize window
        short min = (short) (-(Position.WIDTH * Position.HEIGHT - 2 - pos.getMoves()) / 2);
        if (a < min) {
            a = min;
            if (a >= b) return new Move(a, C);
        }

        // max score: move now - 2 moves (since no immediate win) + optimize window
        short max = (short) ((Position.WIDTH * Position.HEIGHT - 1 - pos.getMoves()) / 2);
        short score = table.getScore(pos.getKey());
        if (score != 0) {
            max = score;
        }
        short c = table.getCol(pos.getKey());
        if (c == -1) c = C;
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
        c = C;
        while (sorter.hasNext()) {
            short col = sorter.getNext();
            Position pos2 = pos.clone();
            pos2.play(col);
            // calculate opponent's score after playing that move
            score = (short) -(negamax(pos2, (short) -b, (short) -a, c).getScore());
            // exit early if score is better than our window
            if (score >= b) return new Move(score, col);
            // optimize window to look for moves better than this
            if (score > a) {
                a = score;
                c = col;
            }
        }

        table.put(pos.getKey(), a, c);
        return new Move(a, c);
    }
}
