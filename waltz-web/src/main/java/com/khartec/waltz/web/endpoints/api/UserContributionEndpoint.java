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

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.tally.OrderedTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.user_contribution.UserContributionService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.parseInteger;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class UserContributionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "user-contribution");
    private static final int DEFAULT_LIMIT = 10;

    private final UserContributionService userContributionService;


    @Autowired
    public UserContributionEndpoint(UserContributionService userContributionService) {
        checkNotNull(userContributionService, "userContributionService cannot be null");

        this.userContributionService = userContributionService;
    }


    @Override
    public void register() {
        String getLeaderBoardPath = mkPath(BASE_URL, "leader-board");
        String getScoreForUserPath = mkPath(BASE_URL, "user", ":userId", "score");
        String findScoresForDirectReportsPath = mkPath(BASE_URL, "user", ":userId", "directs", "score");
        String getLeaderBoardLastMonthPath = mkPath(BASE_URL, "monthly-leader-board");
        String getRankedLeaderBoardPath = mkPath(BASE_URL, "user", ":userId", "ordered-leader-board");

        ListRoute<OrderedTally<String>> getLeaderBoardRoute = (request, response) -> {
            String limitStr = request.queryParams("limit");
            int limit = parseInteger(limitStr, DEFAULT_LIMIT);

            return userContributionService.getLeaderBoard(limit);
        };

        ListRoute<OrderedTally<String>> getLeaderBoardLastMonthRoute = (request, response) -> {
            String limitStr = request.queryParams("limit");
            int limit = parseInteger(limitStr, DEFAULT_LIMIT);

            return userContributionService.getLeaderBoardLastMonth(limit);
        };

        ListRoute<OrderedTally<String>> getRankedLeaderBoardRoute = (request, response) ->
                userContributionService.getRankedLeaderBoard(request.params("userId"));

        DatumRoute<Double> getScoreForUserRoute = (request, response) ->
                userContributionService.getScoreForUser(request.params("userId"));

        ListRoute<Tally<String>> findScoresForDirectReportsRoute = (request, response) ->
                userContributionService.findScoresForDirectReports(request.params("userId"));

        getForList(getLeaderBoardPath, getLeaderBoardRoute);
        getForList(getLeaderBoardLastMonthPath, getLeaderBoardLastMonthRoute);
        getForDatum(getScoreForUserPath, getScoreForUserRoute);
        getForList(findScoresForDirectReportsPath, findScoresForDirectReportsRoute);
        getForList(getRankedLeaderBoardPath, getRankedLeaderBoardRoute);
    }
}
