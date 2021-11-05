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


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import com.khartec.waltz.model.flow_classification_rule.ImmutableFlowClassificationRuleVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRatingValue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.service.flow_classification_rule.FlowClassificationRuleResolver.getMostSpecificRanked;

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

        Assert.assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating);
    }


    @Test
    public void whenResolveWithExistingVantageButMissingDataTypeThenReturnsNoOpinion() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(EntityReference.mkRef(EntityKind.DATA_TYPE, 10))
                .dataTypeRank(1)
                .applicationId(200L)
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        Assert.assertEquals(AuthoritativenessRatingValue.NO_OPINION, rating);
    }



    @Test
    public void existingVantageAndDataTypeAndDifferentSourceThenDiscouraged() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(EntityReference.mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(1)
                .applicationId(205L)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        Assert.assertEquals(AuthoritativenessRatingValue.DISCOURAGED, rating);
    }



    @Test
    public void existingEntriesThenReturnsMostSpecificRating() {

        List<FlowClassificationRuleVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(EntityReference.mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(2)
                .applicationId(205L)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build());


        vantagePoints.add(ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(2)
                .dataType(EntityReference.mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(3)
                .applicationId(200L)
                .classificationCode(AuthoritativenessRatingValue.of("SECONDARY").value())
                .ruleId(1L)
                .build());

        FlowClassificationRuleResolver flowClassificationRuleResolver = new FlowClassificationRuleResolver(vantagePoints);

        AuthoritativenessRatingValue rating = flowClassificationRuleResolver.resolve(vantagePoint, sourceApp, 20L);

        Assert.assertEquals(AuthoritativenessRatingValue.of("SECONDARY"), rating);

    }


    @Test
    public void getBestRankedIsCorrect() {

        ImmutableFlowClassificationRuleVantagePoint rank12 = ImmutableFlowClassificationRuleVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .vantagePointRank(1)
                .dataType(EntityReference.mkRef(EntityKind.DATA_TYPE, 20))
                .dataTypeRank(2)
                .applicationId(205L)
                .classificationCode(AuthoritativenessRatingValue.of("PRIMARY").value())
                .ruleId(1L)
                .build();

        ImmutableFlowClassificationRuleVantagePoint rank22 = rank12.withVantagePointRank(2);
        ImmutableFlowClassificationRuleVantagePoint rank11 = rank12.withDataTypeRank(1);


        Optional<FlowClassificationRuleVantagePoint> bestRankedOrgUnit = getMostSpecificRanked(newArrayList(rank12, rank22));
        Optional<FlowClassificationRuleVantagePoint> bestRankedDataType = getMostSpecificRanked(newArrayList(rank12, rank11));

        Assert.assertTrue(bestRankedOrgUnit.isPresent());
        Assert.assertEquals(rank22, bestRankedOrgUnit.get());

        Assert.assertTrue(bestRankedDataType.isPresent());
        Assert.assertEquals(rank12, bestRankedDataType.get());
    }



    @Test
    public void getBestRankedWorksWithEmpty() {

        Optional<FlowClassificationRuleVantagePoint> bestRanked = getMostSpecificRanked(newArrayList());

        Assert.assertFalse(bestRanked.isPresent());
    }

}