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

package org.finos.waltz.web.endpoints.api;


import org.finos.waltz.model.survey.SurveyInstanceFormDetails;
import org.finos.waltz.model.survey.SurveyInstanceInfo;
import org.finos.waltz.model.survey.SurveyInstanceUserInvolvement;
import org.finos.waltz.service.survey.SurveyInstanceViewService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class SurveyInstanceViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-instance-view");

    private final SurveyInstanceViewService surveyInstanceViewService;


    @Autowired
    public SurveyInstanceViewEndpoint(SurveyInstanceViewService surveyInstanceViewService) {
        checkNotNull(surveyInstanceViewService, "surveyInstanceViewService cannot be null");
        this.surveyInstanceViewService = surveyInstanceViewService;
    }


    @Override
    public void register() {
        String findForUserPath = mkPath(BASE_URL, "user");
        String findForEntityReferencePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForPersonIdPath = mkPath(BASE_URL, "person", "id", ":id");
        String getInfoByIdPath = mkPath(BASE_URL, "id", ":id");
        String getFormDetailsByIdPath = mkPath(BASE_URL, "form-details", ":id");

        DatumRoute<SurveyInstanceInfo> getInfoByIdRoute = (req, resp) -> surveyInstanceViewService.getInfoById(getId(req));
        DatumRoute<SurveyInstanceFormDetails> getFormDetailsByIdRoute = (req, resp) -> surveyInstanceViewService.getFormDetailsById(getId(req));

        ListRoute<SurveyInstanceUserInvolvement> findForUserRoute =
                (req, res) -> surveyInstanceViewService.findForUser(getUsername(req));

        ListRoute<SurveyInstanceInfo> findForPersonIdRoute =
                (req, res) -> surveyInstanceViewService.findByPersonId(getId(req));

        ListRoute<SurveyInstanceInfo> findForEntityReferenceRoute =
                (req, res) -> surveyInstanceViewService.findByEntityReference(getEntityReference(req));

        getForList(findForUserPath, findForUserRoute);
        getForList(findForEntityReferencePath, findForEntityReferenceRoute);
        getForList(findForPersonIdPath, findForPersonIdRoute);
        getForDatum(getInfoByIdPath, getInfoByIdRoute);
        getForDatum(getFormDetailsByIdPath, getFormDetailsByIdRoute);
    }

}
