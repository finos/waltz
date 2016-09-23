package com.khartec.waltz.model;

public enum Duration {

    Day(1),
    Week(7),
    Month(31),
    Year(365),
    All(Integer.MAX_VALUE);


    private final int numDays;


    Duration(int numDays) {
        this.numDays = numDays;
    }


    public int numDays() {
        return numDays;
    }
}
