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
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.flow_classification_rule.ImmutableFlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleResolver.getMostSpecificRanked;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlowClassificationRuleResolverTest {

    private final ImmutableEntityReference ouVantagePoint = ImmutableEntityReference.builder()
            .kind(EntityKind.ORG_UNIT)
            .id(20L)
            .build();


    private final ImmutableEntityReference sourceApp = ImmutableEntityReference.builder()
            .kind(EntityKind.APPLICATION)
            .id(200L)
            .build();

    private final ImmutableEntityReference targetApp = ImmutableEntityReference.builder()
            .kind(EntityKind.APPLICATION)
            .id(300L)
            .build();


    @Test
    public void whenResolveWithMissingVantagePointThenReturnsNoOpinion() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating.v1);
    }


    @Test
    public void whenResolveWithExistingVantageButMissingDataTypeThenReturnsNoOpinion() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(10L)
                .dataTypeRank(1)
                .subjectReference(mkRef(EntityKind.APPLICATION, 200L))
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating.v1);
    }



    @Test
    public void existingVantageAndDataTypeAndDifferentSourceThenDiscouraged() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(1)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.DISCOURAGED, rating.v1);
    }



    @Test
    public void existingEntriesThenReturnsMostSpecificRating() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(2)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());


        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(2)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(mkRef(EntityKind.APPLICATION, 200L))
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("SECONDARY"), rating.v1);

    }


    @Test
    public void getBestRankedIsCorrect() {

        ImmutableFlowClassificationRuleVantagePoint rank12 = ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(2)
                .subjectReference(mkRef(EntityKind.APPLICATION, 205L))
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build();

        ImmutableFlowClassificationRuleVantagePoint rank22 = rank12.withVantagePointRank(2);
        ImmutableFlowClassificationRuleVantagePoint rank11 = rank12.withDataTypeRank(1);


        ArrayList<FlowClassificationRuleVantagePoint> orgUnitTestList = newArrayList(rank12, rank22);
        Optional<FlowClassificationRuleVantagePoint> bestRankedOrgUnit = getMostSpecificRanked(orgUnitTestList);
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

    @Test
    public void pointToPointRulesApplied() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(sourceApp)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(targetApp)
                .vantagePointRank(0)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(sourceApp)
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(2L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("SECONDARY"), rating.v1);
        assertEquals(2, rating.v2.map(FlowClassificationRuleVantagePoint::ruleId).orElse(null));

    }

    @Test
    public void worksForInboundRules() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(sourceApp)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.INBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("PRIMARY"), rating.v1);
        assertEquals(1, rating.v2.map(FlowClassificationRuleVantagePoint::ruleId).orElse(null));

    }


    @Test
    public void inboundRulesReturnNoOpinionWhenOutOfVantageScope() {

        ImmutableEntityReference differentVantagePoint = ImmutableEntityReference.builder()
                .kind(EntityKind.ORG_UNIT)
                .id(21L)
                .build();

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(sourceApp)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.INBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(differentVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("NO_OPINION"), rating.v1);
        assertFalse(rating.v2.isPresent());
    }

    @Test
    public void inboundRuleHandleActorsSource() {

        ImmutableEntityReference actor = ImmutableEntityReference.builder()
                .kind(EntityKind.ACTOR)
                .id(1L)
                .build();

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(1)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(actor)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.INBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, sourceApp, actor, 20L);

        assertEquals(AuthoritativenessRatingValue.of("PRIMARY"), rating.v1);
        assertEquals(1, rating.v2.map(FlowClassificationRuleVantagePoint::ruleId).orElse(null));

    }

    @Test
    public void inboundRuleHandleActorsScope() {

        ImmutableEntityReference actor = ImmutableEntityReference.builder()
                .kind(EntityKind.ACTOR)
                .id(1L)
                .build();

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(actor)
                .vantagePointRank(0)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(sourceApp)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.INBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(null, actor, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("PRIMARY"), rating.v1);
        assertEquals(1, rating.v2.map(FlowClassificationRuleVantagePoint::ruleId).orElse(null));

    }


    @Test
    public void whenMultipleDiscourageRulesConflictTheSmallestIdWins() {

        ImmutableEntityReference app1 = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(1L)
                .build();

        ImmutableEntityReference app2 = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(2L)
                .build();

        ImmutableEntityReference app3 = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(3L)
                .build();

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(3)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(app1)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(3)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(app2)
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(2L)
                .build());

        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(ouVantagePoint)
                .vantagePointRank(3)
                .dataTypeId(20L)
                .dataTypeRank(3)
                .subjectReference(app3)
                .classificationCode(AuthoritativenessRatingValue.of("TERTIARY").value())
                .ruleId(3L)
                .build());

        // All the above have the same
        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(FlowDirection.OUTBOUND, vantagePoints);

        Tuple2<AuthoritativenessRatingValue, Optional<FlowClassificationRuleVantagePoint>> rating = flowClassificationRuleResolver.resolve(ouVantagePoint, targetApp, sourceApp, 20L);

        assertEquals(AuthoritativenessRatingValue.of("DISCOURAGED"), rating.v1);
        assertEquals(1, rating.v2.map(FlowClassificationRuleVantagePoint::ruleId).orElse(null));

    }

}