/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.common;

import java.util.function.Predicate;

import static com.khartec.waltz.common.Checks.*;

/**
 * An inclusive range band.  Items can be tested against this range
 * to see if they fall within it.  As a convenience this object may
 * also be used as a {@link Predicate}
 */
public class RangeBand<T extends Comparable<T>> implements Predicate<T> {

    private final T low;
    private final T high;

    public RangeBand(T low, T high) {
        boolean bothNull = low == null && high == null;
        boolean bothDefined = low != null && high != null;

        checkFalse(bothNull, "Both low and high cannot be null");
        if (bothDefined) {
            checkTrue(low.compareTo(high) <= 0, "Low must be <= high");
        }

        this.low = low;
        this.high = high;
    }


    public boolean contains(T item) {
        checkNotNull(item, "item cannot be null");

        if (low == null) {
            return item.compareTo(high) <= 0;
        }

        if (high == null) {
            return item.compareTo(low) >= 0;
        }

        return item.compareTo(high) <= 0 && item.compareTo(low) >= 0;
    }


    @Override
    public boolean test(T t) {
        return contains(t);
    }


    public T getLow() {
        return low;
    }


    public T getHigh() {
        return high;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RangeBand<?> rangeBand = (RangeBand<?>) o;

        if (low != null ? !low.equals(rangeBand.low) : rangeBand.low != null) return false;
        return high != null ? high.equals(rangeBand.high) : rangeBand.high == null;
    }


    @Override
    public int hashCode() {
        int result = low != null ? low.hashCode() : 0;
        result = 31 * result + (high != null ? high.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return new StringBuilder()
                .append(getLow() == null ? "*" : getLow())
                .append(" - ")
                .append(getHigh() == null ? "*" : getHigh())
                .toString();
    }
}
