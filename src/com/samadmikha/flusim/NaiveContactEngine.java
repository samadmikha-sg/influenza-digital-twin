package com.samadmikha.flusim;

import java.util.SplittableRandom;

/**
 * Baseline engine used for benchmarking: every SUSCEPTIBLE agent scans every member of each of its
 * groups and counts infectious neighbours (O(population x group size) per day).
 * Per-pair probability is scaled to match the sampling engine's expected contact rate.
 */
public final class NaiveContactEngine implements ContactEngine {
    public String name() { return "naive (susceptible scans group)"; }

    @Override
    public int spread(Population pop, byte[] state, boolean[] exposed, Config cfg, SplittableRandom rnd) {
        int count = 0;
        for (int a = 0; a < pop.size; a++) {
            if (state[a] != State.S) continue;
            double escape = 1.0;
            escape *= escape(pop.household, a, state, cfg.householdBeta(), Integer.MAX_VALUE);
            escape *= escape(pop.work, a, state, cfg.workBeta(), cfg.workContactsPerDay());
            escape *= escape(pop.community, a, state, cfg.communityBeta(), cfg.communityContactsPerDay());
            if (rnd.nextDouble() > escape) { exposed[a] = true; count++; }
        }
        return count;
    }

    private static double escape(Layer l, int a, byte[] state, double beta, int contactsPerDay) {
        int g = l.groupOf[a];
        if (g < 0) return 1.0;
        int size = l.groupSize(g);
        if (size < 2) return 1.0;
        double pPair = contactsPerDay == Integer.MAX_VALUE ? beta : beta * contactsPerDay / (size - 1);
        int infectious = 0;
        for (int i = l.start[g]; i < l.start[g + 1]; i++) {
            int t = l.members[i];
            if (t != a && state[t] == State.I) infectious++;
        }
        return Math.pow(1.0 - Math.min(1.0, pPair), infectious);
    }
}
