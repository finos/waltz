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

package com.khartec.waltz.model.utils;

import com.khartec.waltz.model.IdProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.indexBy;


public class IdUtilities {


    /**
     * Convert the given collection of idProviders to their id values.
     * Empty ids are skipped in the resulting list.
     * @param xs collection of idProvider objects
     * @return list of ids
     */
    public static List<Long> toIds(Collection<? extends IdProvider> xs) {
        checkNotNull(xs, "Cannot convert a null collection to a list of ids");
        return xs
                .stream()
                .map(x -> x.id().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * Convert the given list of idProviders to their id values.
     * Empty ids are skipped in the resulting array.
     * @param xs collection of id providers
     * @return and array of ids
     */
    public static Long[] toIdArray(Collection<? extends IdProvider> xs) {
        checkNotNull(xs, "Cannot convert a null collection to an array of ids");
        return toIds(xs)
                .toArray(new Long[xs.size()]);
    }


    /**
     * If the given id provider has an id of Optional.empty then
     * throw an IllegalArgumentException with the given message.
     * <br>
     * Returns .
     * @param idProvider object which implements IdProvider
     * @param exceptionMessage, message to use if idProvider.id() == empty
     * @return the id if it is present
     */
    public static Long ensureHasId(IdProvider idProvider, String exceptionMessage) {
        return idProvider
                .id()
                .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

    }


    public static <T extends IdProvider> Map<Long, T> indexById(List<T> ts) {
        return indexBy(t -> t.id().get(), ts);
    }
}
