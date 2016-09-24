package com.khartec.waltz.model;

public enum Duration {

    DAY(1),
    WEEK(7),
    MONTH(31),
    YEAR(365),
    ALL(Integer.MAX_VALUE);


    private final int numDays;


    Duration(int numDays) {
        this.numDays = numDays;
    }


    public int numDays() {
        return numDays;
    }
}
