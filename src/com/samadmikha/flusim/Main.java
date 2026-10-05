package com.samadmikha.flusim;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** CLI: java -cp out com.samadmikha.flusim.Main [agents] [days] [seed] [output.csv] */
public final class Main {
    public static void main(String[] args) throws IOException {
        int agents = args.length > 0 ? Integer.parseInt(args[0]) : 100_000;
        int days = args.length > 1 ? Integer.parseInt(args[1]) : 120;
        long seed = args.length > 2 ? Long.parseLong(args[2]) : 42L;
        Path csv = Path.of(args.length > 3 ? args[3] : "results.csv");

        Config cfg = Config.defaults(agents, days, seed);
        long t0 = System.nanoTime();
        Population pop = Population.generate(agents, seed);
        List<DayStats> stats = new Simulation(cfg, pop, new SamplingContactEngine()).run();
        double sec = (System.nanoTime() - t0) / 1e9;

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(csv))) {
            w.println("day,susceptible,exposed,infectious,recovered,new_infections");
            for (DayStats d : stats)
                w.printf("%d,%d,%d,%d,%d,%d%n", d.day(), d.susceptible(), d.exposed(), d.infectious(), d.recovered(), d.newInfections());
        }
        DayStats peak = stats.stream().max(java.util.Comparator.comparingInt(DayStats::infectious)).get();
        DayStats last = stats.get(stats.size() - 1);
        System.out.printf("Agents=%,d days=%d seed=%d%n", agents, days, seed);
        System.out.printf("Peak infectious: %,d (%.1f%%) on day %d%n", peak.infectious(), 100.0 * peak.infectious() / agents, peak.day());
        System.out.printf("Final attack rate: %.1f%%%n", 100.0 * last.recovered() / agents);
        System.out.printf("Runtime: %.2f s  ->  %s%n", sec, csv);
    }
}
