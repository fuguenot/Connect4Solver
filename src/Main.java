import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static int nodes = 0;

    private static float avg(List<Long> times) {
        return times.stream().reduce(0L, Long::sum) / 1000f / 1000000000f;
    }

    private static float min(List<Long> times) {
        return times.stream().min(Long::compare).get() / 1000000000f;
    }

    private static float max(List<Long> times) {
        return times.stream().max(Long::compare).get() / 1000000000f;
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

    private static void testSet(String name) throws IOException {
        List<Long> times = new ArrayList<>();
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(name))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.isEmpty()) break;
                Position pos = new Position();
                String[] line = l.split(" ");
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
                times.add(end - start);
                i++;
            }

            System.out.println();
            System.out.println("Avg. time: " + avg(times) + "s, range: [" + min(times) + "s, " + max(times) + "s]");
        }
    }

    public static void main(String[] args) throws IOException {
        testSet("Test_L3_R1");
    }
}
