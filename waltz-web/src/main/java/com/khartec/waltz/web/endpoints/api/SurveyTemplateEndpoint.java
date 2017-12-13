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

import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplateChangeCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class SurveyTemplateEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-template");

    private final SurveyTemplateService surveyTemplateService;
    private final UserRoleService userRoleService;

    @Autowired
    public SurveyTemplateEndpoint(SurveyTemplateService surveyTemplateService,
                                  UserRoleService userRoleService) {
        checkNotNull(surveyTemplateService, "surveyTemplateService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.surveyTemplateService = surveyTemplateService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, ":id");
        String updateStatusPath = mkPath(BASE_URL, ":id", "status");
        String clonePath = mkPath(BASE_URL, ":id", "clone");

        DatumRoute<SurveyTemplate> getByIdRoute = (request, response) ->
                surveyTemplateService.getById(getId(request));

        ListRoute<SurveyTemplate> findAllRoute = (request, response) ->
                surveyTemplateService.findAll(getUsername(request));

        DatumRoute<Long> createRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyTemplateService.create(
                            getUsername(req),
                            readBody(req, SurveyTemplateChangeCommand.class));
                };

        DatumRoute<Integer> updateRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyTemplateService.update(
                            getUsername(req),
                            readBody(req, SurveyTemplateChangeCommand.class));
                };

        DatumRoute<Integer> updateStatusRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyTemplateService.updateStatus(
                            getUsername(req),
                            getId(req),
                            readBody(req, ReleaseLifecycleStatusChangeCommand.class));
                };

        DatumRoute<Long> cloneRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyTemplateService.clone(
                            getUsername(req),
                            getId(req));
        };

        getForList(BASE_URL, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForDatum(BASE_URL, createRoute);
        postForDatum(clonePath, cloneRoute);
        putForDatum(BASE_URL, updateRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SURVEY_TEMPLATE_ADMIN);
    }
}
