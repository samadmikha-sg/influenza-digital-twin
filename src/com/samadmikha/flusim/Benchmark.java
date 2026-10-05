package com.samadmikha.flusim;

import java.util.Arrays;

/** Compares the naive and sampling contact engines. Usage: Benchmark [days=200] [runs=3] */
public final class Benchmark {
    public static void main(String[] args) {
        int days = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        int runs = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        System.out.println("| Agents | Naive (s) | Sampling (s) | Speed-up | Reduction | Attack rate naive / sampling |");
        System.out.println("|---:|---:|---:|---:|---:|:---|");
        for (int n : new int[] {10_000, 50_000, 100_000, 250_000}) {
            Config cfg = Config.defaults(n, days, 7);
            Population pop = Population.generate(n, 7);
            run(cfg, pop, new SamplingContactEngine()); // JIT warm-up
            double[] nv = new double[runs], sm = new double[runs];
            double ar1 = 0, ar2 = 0;
            for (int r = 0; r < runs; r++) {
                long t = System.nanoTime(); var a = new Simulation(cfg, pop, new NaiveContactEngine()).run();
                nv[r] = (System.nanoTime() - t) / 1e9; ar1 = a.get(a.size() - 1).recovered() / (double) n;
                t = System.nanoTime(); var b = new Simulation(cfg, pop, new SamplingContactEngine()).run();
                sm[r] = (System.nanoTime() - t) / 1e9; ar2 = b.get(b.size() - 1).recovered() / (double) n;
            }
            Arrays.sort(nv); Arrays.sort(sm);
            double tn = nv[runs / 2], ts = sm[runs / 2];
            System.out.printf("| %,d | %.2f | %.2f | %.1fx | %.0f%% | %.1f%% / %.1f%% |%n", n, tn, ts, tn / ts, 100 * (1 - ts / tn), 100 * ar1, 100 * ar2);
        }
    }
    private static void run(Config c, Population p, ContactEngine e) { new Simulation(c, p, e).run(); }
}
