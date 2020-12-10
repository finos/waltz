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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRating;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.head;
import static com.khartec.waltz.common.CollectionUtilities.sort;
import static com.khartec.waltz.common.MapUtilities.groupAndThen;
import static com.khartec.waltz.common.MapUtilities.isEmpty;


public class AuthoritativeSourceResolver {

    private final Map<EntityReference, Map<String, Map<Long, Optional<AuthoritativeRatingVantagePoint>>>> byOuThenDataTypeThenApp;


    /**
     * Construct the Resolver with an internal structure as follows:
     * OrgUnit -> [DataType -> [AppId -> Rating] ]
     * @param authoritativeRatingVantagePoints
     */
    public AuthoritativeSourceResolver(List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints) {
        checkNotNull(authoritativeRatingVantagePoints, "authoritativeRatingVantagePoints cannot be null");

        byOuThenDataTypeThenApp =
                groupAndThen(
                    authRatingVantagePoint -> authRatingVantagePoint.vantagePoint(),
                    byOus -> groupAndThen(
                            byOu -> byOu.dataTypeCode(),
                            byDts -> groupAndThen(
                                    dt -> dt.applicationId(),
                                    a -> getMostSpecificRanked(a),
                                    byDts),
                            byOus),
                    authoritativeRatingVantagePoints);
    }


    /**
     * Given a vantage point, a supplier and a data type this method will give back
     * an authoritativeness rating.
     *
     * @param vantagePoint  typically the ou of the consuming app
     * @param source  typically the app ref of the provider
     * @param dataTypeCode  the data type in question
     * @return  How this should be rated
     */
    public AuthoritativenessRating resolve(EntityReference vantagePoint, EntityReference source, String dataTypeCode) {

        Map<String, Map<Long, Optional<AuthoritativeRatingVantagePoint>>> ouGroup = byOuThenDataTypeThenApp.get(vantagePoint);
        if(isEmpty(ouGroup)) return AuthoritativenessRating.NO_OPINION;

        Map<Long, Optional<AuthoritativeRatingVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeCode);
        if(isEmpty(dataTypeGroup)) return AuthoritativenessRating.NO_OPINION;

        Optional<AuthoritativeRatingVantagePoint> maybeRating = dataTypeGroup.getOrDefault(
                source.id(),
                Optional.empty());

        return maybeRating
                .map(r -> r.rating())
                .orElse(AuthoritativenessRating.DISCOURAGED);
    }


    /**
     * Given a collection of vantages points (maybe) return the first
     * after sorting them in (descending) rank order.
     *
     * @param vantagePoints
     * @return
     */
    static Optional<AuthoritativeRatingVantagePoint> getMostSpecificRanked(Collection<AuthoritativeRatingVantagePoint> vantagePoints) {
        Comparator<AuthoritativeRatingVantagePoint> comparator = Comparator
                .comparingInt(AuthoritativeRatingVantagePoint::vantagePointRank)
                .thenComparingInt(AuthoritativeRatingVantagePoint::dataTypeRank);
        return head(
                sort(
                    vantagePoints,
                    (x, y) -> comparator.compare(y, x))); //note the reversal of parameters because we want descending order
    }

}
