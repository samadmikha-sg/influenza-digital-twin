package com.samadmikha.flusim;

/** Immutable simulation parameters. */
public record Config(
        int agents,
        int days,
        long seed,
        int initialInfected,
        double householdBeta,   // per-contact transmission probability inside a household
        double workBeta,        // ... at school / workplace
        double communityBeta,   // ... in the wider community
        int workContactsPerDay,
        int communityContactsPerDay,
        int latentMin, int latentMax,
        int infectiousMin, int infectiousMax) {

    public static Config defaults(int agents, int days, long seed) {
        return new Config(agents, days, seed, Math.max(20, agents / 2_000),
                0.14, 0.035, 0.012, 5, 3, 1, 3, 3, 6);
    }
}
