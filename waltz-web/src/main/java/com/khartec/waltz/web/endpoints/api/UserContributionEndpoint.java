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

package com.khartec.waltz.web.endpoints.api;


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

        ListRoute<Tally<String>> getLeaderBoardRoute = (request, response) -> {
            String limitStr = request.queryParams("limit");
            int limit = parseInteger(limitStr, DEFAULT_LIMIT);

            return userContributionService.getLeaderBoard(limit);
        };

        DatumRoute<Double> getScoreForUserRoute = (request, response) ->
                userContributionService.getScoreForUser(request.params("userId"));

        ListRoute<Tally<String>> findScoresForDirectReportsRoute = (request, response) ->
                userContributionService.findScoresForDirectReports(request.params("userId"));

        getForList(getLeaderBoardPath, getLeaderBoardRoute);
        getForDatum(getScoreForUserPath, getScoreForUserRoute);
        getForList(findScoresForDirectReportsPath, findScoresForDirectReportsRoute);
    }
}
