import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Game game = new Game();

    private static float avg(List<Long> times) {
        return times.stream().reduce(0L, Long::sum) / 1000f / 1000000000f;
    }

    private static float min(List<Long> times) {
        assert !times.isEmpty();
        return times.stream().min(Long::compare).get() / 1000000000f;
    }

    private static float max(List<Long> times) {
        assert !times.isEmpty();
        return times.stream().max(Long::compare).get() / 1000000000f;
    }

    private static void testSet(String name) throws IOException {
        List<Long> times = new ArrayList<>();
        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(name))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.isEmpty()) break;
                Game.Position pos = new Game.Position();
                String[] line = l.split(" ");
                String moves = line[0];
                short score = Short.parseShort(line[1]);
                for (char c : moves.toCharArray())
                    pos.play(Short.parseShort(String.valueOf(c)) - 1);
                long start = System.nanoTime();
                Game.Move move = game.solve(pos);
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

    private static void testString(String s) {
        Game.Position pos = new Game.Position();
        for (char c : s.toCharArray())
            pos.play(Short.parseShort(String.valueOf(c)) - 1);
        long start = System.nanoTime();
        Game.Move move = game.solve(pos);
        long end = System.nanoTime();
        System.out.println("Score: " + move.getScore() + ", col: " + move.getCol() + " :: " + (end - start) / 1_000_000_000f + "s");
    }

    public static void main(String[] args) throws IOException {
        testString("44536");
    }
}
