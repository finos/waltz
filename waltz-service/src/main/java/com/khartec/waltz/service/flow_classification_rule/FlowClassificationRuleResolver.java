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

package com.khartec.waltz.service.flow_classification_rule;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRatingValue;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.head;
import static com.khartec.waltz.common.CollectionUtilities.sort;
import static com.khartec.waltz.common.MapUtilities.groupAndThen;
import static com.khartec.waltz.common.MapUtilities.isEmpty;


public class FlowClassificationRuleResolver {

    private final Map<EntityReference, Map<Long, Map<Long, Optional<FlowClassificationRuleVantagePoint>>>> byOuThenDataTypeThenApp;

    /**
     * Construct the Resolver with an internal structure as follows:
     * OrgUnit -> [DataTypeId -> [AppId -> Rating] ]
     * @param flowClassificationVantagePoints
     */
    public FlowClassificationRuleResolver(List<FlowClassificationRuleVantagePoint> flowClassificationVantagePoints) {
        checkNotNull(flowClassificationVantagePoints, "flowClassificationVantagePoints cannot be null");

        byOuThenDataTypeThenApp =
                groupAndThen(
                        FlowClassificationRuleVantagePoint::vantagePoint,
                        byOus -> groupAndThen(
                                byOu -> byOu.dataType().id(),
                                byDts -> groupAndThen(
                                        FlowClassificationRuleVantagePoint::applicationId,
                                        a -> getMostSpecificRanked(a),
                                        byDts),
                                byOus),
                        flowClassificationVantagePoints);
    }


    /**
     * Given a vantage point, a supplier and a data type this method will give back
     * an authoritativeness rating.
     *
     * @param vantagePoint  typically the ou of the consuming app
     * @param source  typically the app ref of the provider
     * @param dataTypeId  the data type in question
     * @return  How this should be rated
     */
    public AuthoritativenessRatingValue resolve(EntityReference vantagePoint,
                                                EntityReference source,
                                                Long dataTypeId) {

        Map<Long, Map<Long, Optional<FlowClassificationRuleVantagePoint>>> ouGroup = byOuThenDataTypeThenApp.get(vantagePoint);

        // if a match cannot be found for the ou and the dt then no opinion, if a match can be found for these but the source application
        // doesn't match then the rating should be discouraged

        if(isEmpty(ouGroup)) {
            return AuthoritativenessRatingValue.NO_OPINION;
        }

        Map<Long, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeId);

        if(isEmpty(dataTypeGroup)) {
            return AuthoritativenessRatingValue.NO_OPINION;
        }

        Optional<FlowClassificationRuleVantagePoint> maybeRating = dataTypeGroup.getOrDefault(
                source.id(),
                Optional.empty());

        return maybeRating
                .map(r -> AuthoritativenessRatingValue.of(r.classificationCode()))
                .orElse(AuthoritativenessRatingValue.DISCOURAGED);
    }


    public Optional<FlowClassificationRuleVantagePoint> resolveAuthSource(EntityReference vantagePoint, EntityReference source, Long dataTypeId) {

        Map<Long, Map<Long, Optional<FlowClassificationRuleVantagePoint>>> ouGroup = byOuThenDataTypeThenApp.get(vantagePoint);
        if(isEmpty(ouGroup)) return Optional.empty();

        Map<Long, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeId);
        if(isEmpty(dataTypeGroup)) return Optional.empty();

        return  dataTypeGroup.getOrDefault(
                source.id(),
                Optional.empty());
    }


    /**
     * Given a collection of vantages points (maybe) return the first
     * after sorting them in (descending) rank order.
     *
     * @param vantagePoints
     * @return
     */
    public static Optional<FlowClassificationRuleVantagePoint> getMostSpecificRanked(Collection<FlowClassificationRuleVantagePoint> vantagePoints) {
        Comparator<FlowClassificationRuleVantagePoint> comparator = Comparator
                .comparingInt(FlowClassificationRuleVantagePoint::vantagePointRank)
                .thenComparingInt(FlowClassificationRuleVantagePoint::dataTypeRank);
        return head(
                sort(
                    vantagePoints,
                    (x, y) -> comparator.compare(y, x))); //note the reversal of parameters because we want descending order
    }

}
