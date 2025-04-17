import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static int nodes = 0;

    private static float avg(long[] times) {
        return Arrays.stream(times).sum() / 1000f / 1000000000f;
    }

    private static float min(long[] times) {
        return Arrays.stream(times).min().getAsLong() / 1000000000f;
    }

    private static float max(long[] times) {
        return Arrays.stream(times).max().getAsLong() / 1000000000f;
    }

    private static Move solve(Position pos) {
        // check immediate win
        short c = pos.canWinNext();
        if (c != -1) return new Move((short) (Position.WIDTH * Position.HEIGHT + 1 - pos.getMoves() / 2), c);

        // restrict to moves remaining
        short min = (short) (-(Position.WIDTH * Position.HEIGHT - pos.getMoves()) / 2);
        short max = (short) ((Position.WIDTH * Position.HEIGHT + 1 - pos.getMoves()) / 2);

        Move minMove = new Move(min);

        while (min < max) {
            short med = (short) (min + (max - min) / 2);
            if (med <= 0 && min / 2 < med) med = (short) (min / 2);
            else if (med >= 0 && max / 2 > med) med = (short) (max / 2);
            // null window search for better/worse score to narrow min/max
            Move move = Game.negamax(pos, med, (short) (med + 1), (short) -1);
            // adjust window
            if (move.getScore() <= med) max = move.getScore();
            else {
                min = move.getScore();
                minMove = move;
            }
        }
        return minMove;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        long[] times = new long[1000];
        for (int i = 0; i < 1000; i++) {
            Position pos = new Position(0, 0, (short) 0);
            String[] line = sc.nextLine().split(" ");
            String moves = line[0];
            int score = Integer.parseInt(line[1]);
            for (char c : moves.toCharArray())
                pos.play(Integer.parseInt(String.valueOf(c)) - 1);
            long start = System.nanoTime();
            Move move = solve(pos);
            if (move.getCol() == -1) {
                System.out.println("AAAAAAAAAAA");
                System.out.println(moves + ": " + move.getScore());
                break;
            }
            long end = System.nanoTime();
            if (move.getScore() != score) {
                System.out.println("NO!!!!!!!!");
                System.out.println(moves + ": " + move.getScore() + " (expected: " + score + ")");
                break;
            }
            System.out.println((i + 1) + " :: " + moves + ", score: " + move.getScore() + " :: " + (end - start) / 1_000_000_000.0 + " s");
            times[i] = end - start;
//            System.out.println(move.getCol() + " (took " + (end - start) + " ns)");
        }
        System.out.println();
        System.out.println("Avg. time: " + avg(times) + "s, range: [" + min(times) + "s, " + max(times) + "s]");
    }
}
