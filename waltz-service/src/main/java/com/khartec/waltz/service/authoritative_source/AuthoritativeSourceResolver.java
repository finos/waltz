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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRating;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                                    a -> getBestRanked(a),
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
    static Optional<AuthoritativeRatingVantagePoint> getBestRanked(Collection<AuthoritativeRatingVantagePoint> vantagePoints) {
        return head(
                sort(
                    vantagePoints,
                    (x, y) -> Integer.compare(y.rank(), x.rank())));
    }

}
