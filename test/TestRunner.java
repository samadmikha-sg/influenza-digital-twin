import com.samadmikha.flusim.*;
import java.util.List;

/** Dependency-free tests. Run: ./build.sh test */
public class TestRunner {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("PASS  " + name); }
        else { failed++; System.out.println("FAIL  " + name); }
    }

    public static void main(String[] args) {
        Population pop = Population.generate(5_000, 1);

        // Layer structure
        int inHousehold = 0;
        for (int g = 0; g < pop.household.groups; g++) {
            inHousehold += pop.household.groupSize(g);
            check("household " + g + " size 1..5", pop.household.groupSize(g) >= 1 && pop.household.groupSize(g) <= 5);
            if (g > 20) break;
        }
        int total = 0;
        for (int g = 0; g < pop.household.groups; g++) total += pop.household.groupSize(g);
        check("every agent is in exactly one household", total == 5_000);

        // Population conservation
        Config cfg = Config.defaults(5_000, 60, 1);
        List<DayStats> stats = new Simulation(cfg, pop, new SamplingContactEngine()).run();
        boolean conserved = stats.stream().allMatch(d -> d.susceptible() + d.exposed() + d.infectious() + d.recovered() == 5_000);
        check("S+E+I+R equals population every day", conserved);

        boolean monotone = true;
        for (int i = 1; i < stats.size(); i++) {
            monotone &= stats.get(i).susceptible() <= stats.get(i - 1).susceptible();
            monotone &= stats.get(i).recovered() >= stats.get(i - 1).recovered();
        }
        check("susceptible never increases, recovered never decreases", monotone);

        // Determinism
        List<DayStats> again = new Simulation(cfg, pop, new SamplingContactEngine()).run();
        check("same seed gives identical results", stats.equals(again));

        // No transmission when beta = 0
        Config zero = new Config(5_000, 40, 1, 20, 0, 0, 0, 5, 3, 1, 3, 3, 6);
        List<DayStats> none = new Simulation(zero, pop, new SamplingContactEngine()).run();
        check("no new infections when beta = 0", none.stream().allMatch(d -> d.newInfections() == 0));
        check("everyone seeded recovers eventually", none.get(none.size() - 1).infectious() == 0);

        // Epidemic actually spreads with default parameters
        DayStats last = stats.get(stats.size() - 1);
        check("epidemic spreads beyond seed cases", last.recovered() > 500);

        // Naive and sampling engines should give a similar epidemic size (within 15 points)
        List<DayStats> naive = new Simulation(cfg, pop, new NaiveContactEngine()).run();
        double a = naive.get(naive.size() - 1).recovered() / 5000.0, b = last.recovered() / 5000.0;
        check("naive vs sampling attack rate within 0.15 (" + String.format("%.2f vs %.2f", a, b) + ")", Math.abs(a - b) < 0.15);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
