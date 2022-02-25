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

package org.finos.waltz.model.utils;

import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;

import java.util.*;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.indexBy;


public class IdUtilities {


    /**
     * Convert the given collection of idProviders to their id values.
     * Empty ids are skipped in the resulting set.
     * @param xs collection of idProvider objects
     * @return set of ids
     */
    public static Set<Long> toIds(Collection<? extends IdProvider> xs) {
        checkNotNull(xs, "Cannot convert a null collection to a set of ids");
        return xs
                .stream()
                .map(x -> x.id().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
     * @param exceptionMessage message to use if idProvider.id() == empty
     * @return the id if it is present
     */
    public static Long ensureHasId(IdProvider idProvider, String exceptionMessage) {
        return idProvider
                .id()
                .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));

    }


    @Deprecated
    public static <T extends IdProvider> Map<Optional<Long>, T> indexByOptId(Collection<T> ts) {
        return indexBy(IdProvider::id, ts);
    }


    public static <T extends IdProvider> Map<Long, T> indexByOptionalId(Collection<T> ts) {
        return indexBy(ts, t -> t.id().orElse(null));
    }


    public static <T extends IdProvider> Map<Long, T> indexById(Collection<T> ts) {
        return indexBy(t -> t.id().get(), ts);
    }


    public static Long getIdOrDefault(IdProvider d, Long dfltValue) {
        return d == null
                ? dfltValue
                : d.id().orElse(dfltValue);

    }
}
