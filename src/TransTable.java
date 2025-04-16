public class TransTable {
    private static final int SIZE = (1 << 23) + 9;
    private final long[] keys = new long[SIZE];
    private final short[] vals = new short[SIZE];
    private final short[] cols = new short[SIZE];

    private int idx(long key) {
        return (int) (key % SIZE);
    }

    public void put(long key, short val, short col) {
        int i = idx(key);
        keys[i] = key;
        vals[i] = val;
        cols[i] = col;
    }

    public short getScore(long key) {
        int i = idx(key);
        return keys[i] == key ? vals[i] : 0;
    }

    public short getCol(long key) {
        int i = idx(key);
        return keys[i] == key ? cols[i] : -1;
    }
}
