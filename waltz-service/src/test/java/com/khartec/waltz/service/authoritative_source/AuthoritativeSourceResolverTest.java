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


import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.ImmutableAuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthoritativeSourceResolverTest {

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

        List<AuthoritativeRatingVantagePoint> vantagePoints = new ArrayList<>();
        AuthoritativeSourceResolver authoritativeSourceResolver = new AuthoritativeSourceResolver(vantagePoints);

        AuthoritativenessRating rating = authoritativeSourceResolver.resolve(vantagePoint, sourceApp, "REF_DATA");

        Assert.assertEquals(AuthoritativenessRating.NO_OPINION, rating);
    }


    @Test
    public void whenResolveWithExistingVantageButMissingDataTypeThenReturnsNoOpinion() {

        List<AuthoritativeRatingVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .rank(1)
                .dataTypeCode("TRADE_DATA")
                .applicationId(200L)
                .rating(AuthoritativenessRating.SECONDARY)
                .build());

        AuthoritativeSourceResolver authoritativeSourceResolver = new AuthoritativeSourceResolver(vantagePoints);

        AuthoritativenessRating rating = authoritativeSourceResolver.resolve(vantagePoint, sourceApp, "REF_DATA");

        Assert.assertEquals(AuthoritativenessRating.NO_OPINION, rating);
    }



    @Test
    public void existingVantageAndDataTypeAndDifferentSourceThenDiscouraged() {

        List<AuthoritativeRatingVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .rank(1)
                .dataTypeCode("REF_DATA")
                .applicationId(205L)
                .rating(AuthoritativenessRating.PRIMARY)
                .build());

        AuthoritativeSourceResolver authoritativeSourceResolver = new AuthoritativeSourceResolver(vantagePoints);

        AuthoritativenessRating rating = authoritativeSourceResolver.resolve(vantagePoint, sourceApp, "REF_DATA");

        Assert.assertEquals(AuthoritativenessRating.DISCOURAGED, rating);
    }



    @Test
    public void existingEntriesThenReturnsMostSpecificRating() {

        List<AuthoritativeRatingVantagePoint> vantagePoints = new ArrayList<>();
        vantagePoints.add(ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .rank(1)
                .dataTypeCode("REF_DATA")
                .applicationId(205L)
                .rating(AuthoritativenessRating.PRIMARY)
                .build());


        vantagePoints.add(ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .rank(2)
                .dataTypeCode("REF_DATA")
                .applicationId(200L)
                .rating(AuthoritativenessRating.SECONDARY)
                .build());

        AuthoritativeSourceResolver authoritativeSourceResolver = new AuthoritativeSourceResolver(vantagePoints);

        AuthoritativenessRating rating = authoritativeSourceResolver.resolve(vantagePoint, sourceApp, "REF_DATA");

        Assert.assertEquals(AuthoritativenessRating.SECONDARY, rating);

    }


    @Test
    public void getBestRankedIsCorrect() {

        ImmutableAuthoritativeRatingVantagePoint rank1 = ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(vantagePoint)
                .rank(1)
                .dataTypeCode("REF_DATA")
                .applicationId(205L)
                .rating(AuthoritativenessRating.PRIMARY)
                .build();


        ImmutableAuthoritativeRatingVantagePoint rank2 = rank1.withRank(2);


        Optional<AuthoritativeRatingVantagePoint> bestRanked = AuthoritativeSourceResolver.getBestRanked(ListUtilities.newArrayList(rank1, rank2));


        Assert.assertTrue(bestRanked.isPresent());
        Assert.assertEquals(rank2, bestRanked.get());
    }



    @Test
    public void getBestRankedWorksWithEmpty() {

        Optional<AuthoritativeRatingVantagePoint> bestRanked = AuthoritativeSourceResolver.getBestRanked(ListUtilities.newArrayList());


        Assert.assertFalse(bestRanked.isPresent());
    }

}