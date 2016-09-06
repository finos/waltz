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
