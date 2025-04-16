public class MoveSorter {
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
