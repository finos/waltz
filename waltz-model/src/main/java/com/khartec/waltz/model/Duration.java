package com.khartec.waltz.model;

public enum Duration {

    DAY(1),
    WEEK(7),
    MONTH(31),
    QUARTER(MONTH.numDays * 3),
    HALF_YEAR(MONTH.numDays * 6),
    YEAR(MONTH.numDays * 12),
    ALL(Integer.MAX_VALUE);


    private final int numDays;


    Duration(int numDays) {
        this.numDays = numDays;
    }


    public int numDays() {
        return numDays;
    }
}
