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

import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.service.assessment_definition.AssessmentDefinitionService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class AssessmentDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "assessment-definition");

    private final AssessmentDefinitionService assessmentDefinitionService;


    @Autowired
    public AssessmentDefinitionEndpoint(AssessmentDefinitionService assessmentDefinitionService) {
        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");

        this.assessmentDefinitionService = assessmentDefinitionService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, ":id");
        String findAllPath = mkPath(BASE_URL);
        String findByKind = mkPath(BASE_URL, "kind", ":kind");

        DatumRoute<AssessmentDefinition> getByIdRoute = (request, response) -> assessmentDefinitionService.getById(getId(request));
        ListRoute<AssessmentDefinition> findAllRoute = (request, response) -> assessmentDefinitionService.findAll();
        ListRoute<AssessmentDefinition> findByKindRoute = (request, response) -> assessmentDefinitionService.findByEntityKind(getKind(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findAllPath, findAllRoute);
        getForList(findByKind, findByKindRoute);
    }

}
