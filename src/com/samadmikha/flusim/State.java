package com.samadmikha.flusim;

/** SEIR disease states stored as bytes to keep the per-agent footprint small. */
public final class State {
    private State() {}
    public static final byte S = 0, E = 1, I = 2, R = 3;
}
