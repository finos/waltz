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

import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplateChangeCommand;
import com.khartec.waltz.model.user.SystemRole;
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
        requireRole(userRoleService, request, SystemRole.SURVEY_TEMPLATE_ADMIN);
    }
}
