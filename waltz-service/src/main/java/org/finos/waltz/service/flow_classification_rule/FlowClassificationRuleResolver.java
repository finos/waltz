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

package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.head;
import static org.finos.waltz.common.CollectionUtilities.sort;
import static org.finos.waltz.common.MapUtilities.groupAndThen;
import static org.finos.waltz.common.MapUtilities.isEmpty;
import static org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleUtilities.flowClassificationRuleVantagePointComparator;
import static org.jooq.lambda.tuple.Tuple.tuple;


public class FlowClassificationRuleResolver {

    private final Map<EntityReference, Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>>> byVantagePointThenDataTypeThenSubject;
    private final FlowDirection direction;


    /**
     * Construct the Resolver with an internal structure as follows:
     * OrgUnit -> [DataTypeId -> [AppId -> Rating] ]
     * @param flowClassificationVantagePoints
     */
    public FlowClassificationRuleResolver(FlowDirection direction, List<FlowClassificationRuleVantagePoint> flowClassificationVantagePoints) {
        checkNotNull(flowClassificationVantagePoints, "flowClassificationVantagePoints cannot be null");

        List<FlowClassificationRuleVantagePoint> orderedRvps = flowClassificationVantagePoints
                .stream()
                .sorted(flowClassificationRuleVantagePointComparator)
                .collect(Collectors.toList());

        byVantagePointThenDataTypeThenSubject =
                groupAndThen(
                        orderedRvps,
                        FlowClassificationRuleVantagePoint::vantagePoint,
                        byVps -> groupAndThen(
                                byVps,
                                byVp -> byVp.dataType().id(),
                                byDts -> groupAndThen(
                                        byDts,
                                        FlowClassificationRuleVantagePoint::subjectReference,
                                        FlowClassificationRuleResolver::getMostSpecificRanked)));
        this.direction = direction;
    }


    /**
     * Given a vantage point, a supplier and a data type this method will give back
     * an authoritativeness rating.
     *
     * @param vantagePointOrgUnit typically the ou of the receiving app of the rule, will be null if the vantage point is an actor
     * @param vantagePointEntity  typically the entity on the receiving end of the rule
     * @param subject             typically the ref of the rule provider
     * @param dataTypeId          the data type in question
     * @return How this should be rated
     */
    public Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> resolve(EntityReference vantagePointOrgUnit,
                                                                                                      EntityReference vantagePointEntity,
                                                                                                      EntityReference subject,
                                                                                                      Long dataTypeId) {

        //Attempt lookup for rule based upon both org unit and direct rule, prioritise direct lookup as more specific rule
        Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>> vpGroup = byVantagePointThenDataTypeThenSubject.getOrDefault(vantagePointOrgUnit, emptyMap());
        Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>> vpEntity = byVantagePointThenDataTypeThenSubject.getOrDefault(vantagePointEntity, emptyMap());

        // if a match cannot be found for the ou and the dt then no opinion, if a match can be found for these but the subject application
        // doesn't match then the rating should be discouraged

        if(isEmpty(vpEntity) && isEmpty(vpGroup)) {
            return tuple(AuthoritativenessRatingValue.NO_OPINION, Optional.empty());
        }

        Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>> dtEntity = vpEntity.getOrDefault(dataTypeId, emptyMap());
        Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup = vpGroup.getOrDefault(dataTypeId, emptyMap());

        if(isEmpty(dataTypeGroup) && isEmpty(dtEntity)) {
            return tuple(AuthoritativenessRatingValue.NO_OPINION, Optional.empty());
        }

        Optional<FlowClassificationRuleVantagePoint> maybeRule =  isEmpty(dtEntity)
                ? dataTypeGroup.getOrDefault(
                        subject,
                Optional.empty())
                : dtEntity.getOrDefault(
                        subject,
                Optional.empty());

        AuthoritativenessRatingValue defaultRating = direction == FlowDirection.OUTBOUND
                ? AuthoritativenessRatingValue.DISCOURAGED // at least one rule covering this scope and data type to reach this point
                : AuthoritativenessRatingValue.NO_OPINION;

        AuthoritativenessRatingValue ratingValue = maybeRule
                .map(r -> AuthoritativenessRatingValue.of(r.classificationCode()))
                .orElse(defaultRating);

        Optional<FlowClassificationRuleVantagePoint> discouragedRule = determineDefaultRuleId(dataTypeGroup);

        return tuple(ratingValue, ofNullable(maybeRule.orElse(discouragedRule.orElse(null))));
    }

    private Optional<FlowClassificationRuleVantagePoint> determineDefaultRuleId(Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup) {
        return dataTypeGroup
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(flowClassificationRuleVantagePointComparator)
                .findFirst();
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
