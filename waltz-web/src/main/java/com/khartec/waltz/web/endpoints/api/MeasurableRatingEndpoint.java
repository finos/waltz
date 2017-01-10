/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.measurable_rating.*;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.model.tally.MeasurableRatingTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MeasurableRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating");


    private final MeasurableRatingService measurableRatingService;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableRatingEndpoint(MeasurableRatingService measurableRatingService,
                                    UserRoleService userRoleService) {
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.measurableRatingService = measurableRatingService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String modifyPath = mkPath(BASE_URL, "entity", ":kind", ":id", ":measurableId");
        String findByMeasurableSelectorPath = mkPath(BASE_URL, "measurable-selector");
        String findByAppSelectorPath = mkPath(BASE_URL, "app-selector");
        String countByMeasurablePath = mkPath(BASE_URL, "count-by", "measurable");
        String statsByAppSelectorPath = mkPath(BASE_URL, "stats-by", "app-selector");

        ListRoute<MeasurableRating> findForEntityRoute = (request, response)
                -> measurableRatingService.findForEntity(getEntityReference(request));

        ListRoute<MeasurableRating> findByMeasurableSelectorRoute = (request, response)
                -> measurableRatingService.findByMeasurableIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<MeasurableRating> findByAppSelectorRoute = (request, response)
                -> measurableRatingService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<Tally<Long>> countByMeasurableRoute = (request, response)
                -> measurableRatingService.tallyByMeasurableId();

        ListRoute<MeasurableRatingTally> statsByAppSelectorRoute = (request, response)
                -> measurableRatingService.statsByAppSelector(readIdSelectionOptionsFromBody(request));

        getForList(findForEntityPath, findForEntityRoute);
        postForList(findByMeasurableSelectorPath, findByMeasurableSelectorRoute);
        postForList(findByAppSelectorPath, findByAppSelectorRoute);
        postForList(modifyPath, this::createRoute);
        putForList(modifyPath, this::updateRoute);
        deleteForList(modifyPath, this::removeRoute);
        getForList(countByMeasurablePath, countByMeasurableRoute);
        postForList(statsByAppSelectorPath, statsByAppSelectorRoute);
    }


    private Collection<MeasurableRating> updateRoute(Request request, Response z) throws IOException {
        requireRole(userRoleService, request, Role.RATING_EDITOR);
        SaveMeasurableRatingCommand command = mkCommand(request);
        return measurableRatingService.update(command);
    }


    private Collection<MeasurableRating> removeRoute(Request request, Response z) throws IOException {
        requireRole(userRoleService, request, Role.RATING_EDITOR);
        String username = getUsername(request);
        RemoveMeasurableRatingCommand command = ImmutableRemoveMeasurableRatingCommand.builder()
                .entityReference(getEntityReference(request))
                .measurableId(getLong(request, "measurableId"))
                .lastUpdate(LastUpdate.mkForUser(username))
                .build();
        return measurableRatingService.remove(command);
    }


    private Collection<MeasurableRating> createRoute(Request request, Response z) throws IOException {
        requireRole(userRoleService, request, Role.RATING_EDITOR);
        SaveMeasurableRatingCommand command = mkCommand(request);
        return measurableRatingService.create(command);
    }


    private SaveMeasurableRatingCommand mkCommand(Request request) throws IOException {
        String username = getUsername(request);

        Map<String, String> body = readBody(request, Map.class);

        return ImmutableSaveMeasurableRatingCommand.builder()
                .entityReference(getEntityReference(request))
                .measurableId(getLong(request, "measurableId"))
                .rating(RagRating.valueOf(body.getOrDefault("rating", "Z")))
                .description(body.getOrDefault("description", ""))
                .lastUpdate(LastUpdate.mkForUser(username))
                .build();
    }

}
