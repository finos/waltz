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


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.flow_classification_rule.ImmutableFlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleResolver.getMostSpecificRanked;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class FlowClassificationRuleResolverTest {

    private final ImmutableEntityReference vantagePoint = ImmutableEntityReference.builder()
            .kind(EntityKind.ORG_UNIT)
            .id(20L)
            .build();


    private final ImmutableEntityReference sourceApp = ImmutableEntityReference.builder()
            .kind(EntityKind.APPLICATION)
            .id(200L)
            .build();


    @Test
    public void whenResolveWithMissingVantagePointThenReturnsNoOpinion() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating);
    }


    @Test
    public void whenResolveWithExistingVantageButMissingDataTypeThenReturnsNoOpinion() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(mkRef(EntityKind.DATA_TYPE, 10))
                .dataTypeRank(1)
                .subjectReference(mkRef(EntityKind.APPLICATION, 200L))
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating);
    }



    @Test
    public void existingVantageAndDataTypeAndDifferentSourceThenDiscouraged() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(1)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.DISCOURAGED, rating);
    }



    @Test
    public void existingEntriesThenReturnsMostSpecificRating() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(2)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());


        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(2)
                .dataType(mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(3)
                .subjectReference(mkRef(EntityKind.APPLICATION, 200L))
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("SECONDARY"), rating);

    }


    @Test
    public void getBestRankedIsCorrect() {

        ImmutableFlowClassificationRuleVantagePoint rank12 = ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(2)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build();

        ImmutableFlowClassificationRuleVantagePoint rank22 = rank12.withVantagePointRank(2);
        ImmutableFlowClassificationRuleVantagePoint rank11 = rank12.withDataTypeRank(1);


        Optional<FlowClassificationRuleVantagePoint> bestRankedOrgUnit = getMostSpecificRanked(newArrayList(rank12, rank22));
        Optional<FlowClassificationRuleVantagePoint> bestRankedDataType = getMostSpecificRanked(newArrayList(rank12, rank11));

        assertTrue(bestRankedOrgUnit.isPresent());
        assertEquals(rank22, bestRankedOrgUnit.get());

        assertTrue(bestRankedDataType.isPresent());
        assertEquals(rank12, bestRankedDataType.get());
    }



    @Test
    public void getBestRankedWorksWithEmpty() {

        Optional<FlowClassificationRuleVantagePoint> bestRanked = getMostSpecificRanked(newArrayList());

        assertFalse(bestRanked.isPresent());
    }

}