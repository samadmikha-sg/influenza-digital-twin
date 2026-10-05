package com.samadmikha.flusim;

import java.util.SplittableRandom;

/**
 * Optimised engine: work is driven by INFECTIOUS agents only.
 * Each infectious agent contacts everyone in its household and a small random sample in the
 * other layers, so cost per day is O(infectious x contacts) instead of O(population x group size).
 */
public final class SamplingContactEngine implements ContactEngine {
    public String name() { return "sampling (infectious-driven)"; }

    @Override
    public int spread(Population pop, byte[] state, boolean[] exposed, Config cfg, SplittableRandom rnd) {
        int count = 0;
        for (int a = 0; a < pop.size; a++) {
            if (state[a] != State.I) continue;
            count += household(pop.household, a, state, exposed, cfg.householdBeta(), rnd);
            count += sample(pop.work, a, cfg.workContactsPerDay(), cfg.workBeta(), state, exposed, rnd);
            count += sample(pop.community, a, cfg.communityContactsPerDay(), cfg.communityBeta(), state, exposed, rnd);
        }
        return count;
    }

    private static int household(Layer l, int a, byte[] state, boolean[] exposed, double beta, SplittableRandom rnd) {
        int g = l.groupOf[a], c = 0;
        for (int i = l.start[g]; i < l.start[g + 1]; i++) {
            int t = l.members[i];
            if (t != a && state[t] == State.S && !exposed[t] && rnd.nextDouble() < beta) { exposed[t] = true; c++; }
        }
        return c;
    }

    private static int sample(Layer l, int a, int contacts, double beta, byte[] state, boolean[] exposed, SplittableRandom rnd) {
        int g = l.groupOf[a];
        if (g < 0) return 0;
        int size = l.groupSize(g), c = 0;
        if (size < 2) return 0;
        for (int k = 0; k < contacts; k++) {
            int t = l.member(g, rnd.nextInt(size));
            if (t != a && state[t] == State.S && !exposed[t] && rnd.nextDouble() < beta) { exposed[t] = true; c++; }
        }
        return c;
    }
}
