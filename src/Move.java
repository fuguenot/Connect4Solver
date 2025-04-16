public class Move {
    private final short score, col;

    public Move(short score, short col) {
        this.score = score;
        this.col = col;
    }

    public Move(short score) {
        this.score = score;
        col = -1;
    }

    public short getScore() {
        return score;
    }

    public short getCol() {
        return col;
    }

    public boolean isNone() {
        return col == -1;
    }
}