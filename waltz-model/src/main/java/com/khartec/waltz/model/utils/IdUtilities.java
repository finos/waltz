/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.utils;

import com.khartec.waltz.model.IdProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.indexBy;


public class IdUtilities {


    /**
     * Convert the given list of idProviders to their id values.
     * Empty ids are skipped in the resulting list.
     * @param xs
     * @return
     */
    public static List<Long> toIds(List<? extends IdProvider> xs) {
        checkNotNull(xs, "Cannot convert a null list to a list of ids");
        return xs.stream()
                .map(x -> x.id().orElse(null))
                .filter(x -> x != null)
                .collect(Collectors.toList());
    }


    /**
     * Convert the given list of idProviders to their id values.
     * Empty ids are skipped in the resulting array.
     * @param xs
     * @return
     */
    public static Long[] toIdArray(List<? extends IdProvider> xs) {
        checkNotNull(xs, "Cannot convert a null list to an array of ids");
        return toIds(xs)
                .toArray(new Long[xs.size()]);
    }


    /**
     * If the given id provider has an id of Optional.empty then
     * throw an IllegalArgumentException with the given message.
     * <br>
     * Returns .
     * @param idProvider
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
