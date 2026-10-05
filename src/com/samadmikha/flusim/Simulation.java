package com.samadmikha.flusim;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/** Runs a daily-step SEIR agent-based simulation over a multi-layer contact network. */
public final class Simulation {
    private final Config cfg;
    private final Population pop;
    private final ContactEngine engine;
    private final SplittableRandom rnd;
    private final byte[] state;
    private final int[] timer;       // days left in current E or I state
    private final boolean[] exposed; // scratch buffer, reused every day (no per-day allocation)

    public Simulation(Config cfg, Population pop, ContactEngine engine) {
        this.cfg = cfg; this.pop = pop; this.engine = engine;
        this.rnd = new SplittableRandom(cfg.seed() ^ 0x9E3779B97F4A7C15L);
        this.state = new byte[pop.size];
        this.timer = new int[pop.size];
        this.exposed = new boolean[pop.size];
        for (int i = 0; i < Math.min(cfg.initialInfected(), pop.size); i++) {
            int a = rnd.nextInt(pop.size);
            state[a] = State.I;
            timer[a] = range(cfg.infectiousMin(), cfg.infectiousMax());
        }
    }

    public List<DayStats> run() {
        List<DayStats> out = new ArrayList<>(cfg.days() + 1);
        out.add(snapshot(0, 0));
        for (int day = 1; day <= cfg.days(); day++) out.add(step(day));
        return out;
    }

    public DayStats step(int day) {
        java.util.Arrays.fill(exposed, false);
        int newInf = engine.spread(pop, state, exposed, cfg, rnd);
        // progress existing infections first, then apply new exposures
        for (int a = 0; a < pop.size; a++) {
            byte s = state[a];
            if (s == State.E && --timer[a] <= 0) { state[a] = State.I; timer[a] = range(cfg.infectiousMin(), cfg.infectiousMax()); }
            else if (s == State.I && --timer[a] <= 0) state[a] = State.R;
        }
        for (int a = 0; a < pop.size; a++)
            if (exposed[a]) { state[a] = State.E; timer[a] = range(cfg.latentMin(), cfg.latentMax()); }
        return snapshot(day, newInf);
    }

    private DayStats snapshot(int day, int newInf) {
        int s = 0, e = 0, i = 0, r = 0;
        for (byte b : state) { switch (b) { case State.S -> s++; case State.E -> e++; case State.I -> i++; default -> r++; } }
        return new DayStats(day, s, e, i, r, newInf);
    }

    private int range(int lo, int hi) { return lo + rnd.nextInt(hi - lo + 1); }
}
