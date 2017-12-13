/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        return "RangeBand{" +
                "low=" + low +
                ", high=" + high +
                '}';
    }
}
