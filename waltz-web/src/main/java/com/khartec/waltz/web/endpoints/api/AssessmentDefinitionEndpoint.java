/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
