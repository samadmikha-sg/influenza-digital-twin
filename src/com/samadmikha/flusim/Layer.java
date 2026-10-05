package com.samadmikha.flusim;

/**
 * One contact layer (household, school/workplace, community).
 * Group membership is stored in compressed (CSR) form so that "who is in my group?"
 * is an O(1) array slice and the whole layer costs two int arrays, not one list per group.
 */
public final class Layer {
    public final String name;
    final int[] groupOf;      // groupOf[agent] = group id, or -1 if the agent is not in this layer
    final int[] start;        // members of group g are members[start[g] .. start[g+1])
    final int[] members;
    public final int groups;

    public Layer(String name, int[] groupOf, int groups) {
        this.name = name;
        this.groupOf = groupOf;
        this.groups = groups;
        this.start = new int[groups + 1];
        int total = 0;
        for (int g : groupOf) if (g >= 0) { start[g + 1]++; total++; }
        for (int g = 0; g < groups; g++) start[g + 1] += start[g];
        this.members = new int[total];
        int[] fill = new int[groups];
        for (int a = 0; a < groupOf.length; a++) {
            int g = groupOf[a];
            if (g >= 0) members[start[g] + fill[g]++] = a;
        }
    }

    public int groupSize(int g) { return start[g + 1] - start[g]; }
    public int member(int g, int i) { return members[start[g] + i]; }
}
