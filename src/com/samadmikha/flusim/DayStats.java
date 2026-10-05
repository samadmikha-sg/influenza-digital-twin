package com.samadmikha.flusim;

public record DayStats(int day, int susceptible, int exposed, int infectious, int recovered, int newInfections) {}
