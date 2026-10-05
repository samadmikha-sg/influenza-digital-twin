package com.samadmikha.flusim;

import java.util.SplittableRandom;

/** Synthetic population: every agent belongs to a household, optionally a school/workplace, and a community. */
public final class Population {
    public final int size;
    public final Layer household, work, community;

    private Population(int size, Layer h, Layer w, Layer c) {
        this.size = size; this.household = h; this.work = w; this.community = c;
    }

    public static Population generate(int n, long seed) {
        SplittableRandom rnd = new SplittableRandom(seed);
        int[] hh = new int[n], wk = new int[n], cm = new int[n];

        // Households of size 1-5 (weights roughly follow census-style distributions).
        int[] sizeWeights = {15, 28, 22, 22, 13};
        int a = 0, hid = 0;
        while (a < n) {
            int s = pick(sizeWeights, rnd) + 1;
            for (int i = 0; i < s && a < n; i++) hh[a++] = hid;
            hid++;
        }

        // Age bands: 0-17 school, 18-64 workplace, 65+ none (they only mix at home / in the community).
        int wid = 0, inGroup = 0, groupCap = 20 + rnd.nextInt(20);
        for (int i = 0; i < n; i++) {
            double r = rnd.nextDouble();
            if (r < 0.80) {                       // attends school or works
                wk[i] = wid;
                if (++inGroup >= groupCap) { wid++; inGroup = 0; groupCap = 20 + rnd.nextInt(20); }
            } else wk[i] = -1;
        }
        int workGroups = (inGroup == 0 ? wid : wid + 1);

        // Neighbourhood-style community groups of ~200 people, assigned at random.
        int commGroups = Math.max(1, n / 200);
        for (int i = 0; i < n; i++) cm[i] = rnd.nextInt(commGroups);

        return new Population(n, new Layer("household", hh, hid),
                new Layer("work", wk, Math.max(workGroups, 1)), new Layer("community", cm, commGroups));
    }

    private static int pick(int[] w, SplittableRandom rnd) {
        int total = 0; for (int x : w) total += x;
        int r = rnd.nextInt(total);
        for (int i = 0; i < w.length; i++) { if ((r -= w[i]) < 0) return i; }
        return w.length - 1;
    }
}
