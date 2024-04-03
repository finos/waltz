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
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.lambda.tuple.Tuple2;

import javax.swing.text.html.parser.Entity;
import java.util.*;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.head;
import static org.finos.waltz.common.CollectionUtilities.sort;
import static org.finos.waltz.common.MapUtilities.groupAndThen;
import static org.finos.waltz.common.MapUtilities.isEmpty;
import static org.jooq.lambda.tuple.Tuple.tuple;


public class FlowClassificationRuleResolver {

    private final Map<EntityReference, Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>>> byOuThenDataTypeThenSubject;
    private final FlowDirection direction;

    /**
     * Construct the Resolver with an internal structure as follows:
     * OrgUnit -> [DataTypeId -> [AppId -> Rating] ]
     * @param flowClassificationVantagePoints
     */
    public FlowClassificationRuleResolver(FlowDirection direction, List<FlowClassificationRuleVantagePoint> flowClassificationVantagePoints) {
        checkNotNull(flowClassificationVantagePoints, "flowClassificationVantagePoints cannot be null");

        byOuThenDataTypeThenSubject =
                groupAndThen(
                        flowClassificationVantagePoints,
                        FlowClassificationRuleVantagePoint::vantagePoint,
                        byOus -> groupAndThen(
                                byOus,
                                byOu -> byOu.dataType().id(),
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
     * @param vantagePoint  typically the ou of the consuming app
     * @param subject  typically the app ref of the provider
     * @param dataTypeId  the data type in question
     * @return  How this should be rated
     */
    public Tuple2<AuthoritativenessRatingValue, Optional<Long>> resolve(EntityReference vantagePoint,
                                                                        EntityReference subject,
                                                                        Long dataTypeId) {

        Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>> ouGroup = byOuThenDataTypeThenSubject.get(vantagePoint);

        // if a match cannot be found for the ou and the dt then no opinion, if a match can be found for these but the subject application
        // doesn't match then the rating should be discouraged

        if(isEmpty(ouGroup)) {
            return tuple(AuthoritativenessRatingValue.NO_OPINION, Optional.empty());
        }

        Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeId);

        if(isEmpty(dataTypeGroup)) {
            return tuple(AuthoritativenessRatingValue.NO_OPINION, Optional.empty());
        }

        Optional<FlowClassificationRuleVantagePoint> maybeRule = dataTypeGroup.getOrDefault(
                subject,
                Optional.empty());

        AuthoritativenessRatingValue defaultRating = direction == FlowDirection.OUTBOUND && maybeRule.isPresent()
                ? AuthoritativenessRatingValue.DISCOURAGED
                : AuthoritativenessRatingValue.NO_OPINION;

        AuthoritativenessRatingValue ratingValue = maybeRule
                .map(r -> AuthoritativenessRatingValue.of(r.classificationCode()))
                .orElse(defaultRating);

        return tuple(ratingValue, maybeRule.map(FlowClassificationRuleVantagePoint::ruleId));
    }

    // Might want to change this to return a Rating/Rule or Rule and Match Outcome

    public Optional<FlowClassificationRuleVantagePoint> resolveAuthSource(EntityReference vantagePoint,
                                                                          EntityReference source,
                                                                          Long dataTypeId) {

        Map<Long, Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>>> ouGroup = byOuThenDataTypeThenSubject.get(vantagePoint);
        if(isEmpty(ouGroup)) return Optional.empty();

        Map<EntityReference, Optional<FlowClassificationRuleVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeId);
        if(isEmpty(dataTypeGroup)) return Optional.empty();

        return  dataTypeGroup.getOrDefault(
                source,
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
