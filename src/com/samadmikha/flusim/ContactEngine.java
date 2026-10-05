package com.samadmikha.flusim;

import java.util.SplittableRandom;

/** Strategy for deciding who gets exposed on a given day. Both engines write into {@code newlyExposed}. */
public interface ContactEngine {
    /** @return number of new exposures this day. */
    int spread(Population pop, byte[] state, boolean[] newlyExposed, Config cfg, SplittableRandom rnd);
    String name();
}
