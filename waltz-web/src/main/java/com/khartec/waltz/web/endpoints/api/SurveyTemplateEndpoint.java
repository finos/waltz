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

import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.service.survey.SurveyTemplateService;
import org.finos.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.ReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.SurveyTemplate;
import org.finos.waltz.model.survey.SurveyTemplateChangeCommand;
import org.finos.waltz.model.user.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class SurveyTemplateEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-template");

    private final SurveyTemplateService surveyTemplateService;
    private final UserRoleService userRoleService;
    private final PersonService personService;


    @Autowired
    public SurveyTemplateEndpoint(SurveyTemplateService surveyTemplateService,
                                  UserRoleService userRoleService,
                                  PersonService personService) {
        checkNotNull(surveyTemplateService, "surveyTemplateService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");
        checkNotNull(personService, "personService must not be null");

        this.surveyTemplateService = surveyTemplateService;
        this.userRoleService = userRoleService;
        this.personService = personService;

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

        DatumRoute<Boolean> deleteRoute =
                (req, res) -> {
                    String username = getUsername(req);
                    long templateId = getId(req);
                    ensureUserIsOwnerOrAdmin(req, templateId, username);
                    return surveyTemplateService.delete(templateId);
        };


        getForList(BASE_URL, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForDatum(BASE_URL, createRoute);
        postForDatum(clonePath, cloneRoute);
        putForDatum(BASE_URL, updateRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
        deleteForDatum(mkPath(BASE_URL, ":id"), deleteRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.SURVEY_TEMPLATE_ADMIN);
    }


    private void ensureUserIsOwnerOrAdmin(Request request,
                                          Long templateId,
                                          String username) {

        Person person = personService.getPersonByUserId(username);
        if(person == null) {
            throw new IllegalArgumentException("User not found");
        }

        SurveyTemplate template = surveyTemplateService.getById(templateId);
        //if person record found id is always present
        person.id()
                .ifPresent(id -> {
                    if (template.ownerId().equals(id)){
                        requireRole(userRoleService, request, SystemRole.SURVEY_TEMPLATE_ADMIN);
                    } else {
                        requireRole(userRoleService, request, SystemRole.ADMIN);
                    }
                });
    }
}
