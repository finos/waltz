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

import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class SurveyTemplateEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-template");

    private final SurveyTemplateService surveyTemplateService;


    @Autowired
    public SurveyTemplateEndpoint(SurveyTemplateService surveyTemplateService) {
        checkNotNull(surveyTemplateService, "surveyTemplateService must not be null");

        this.surveyTemplateService = surveyTemplateService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, ":id");
        String findAllActivePath = mkPath(BASE_URL, "active");

        DatumRoute<SurveyTemplate> getByIdRoute = (request, response) ->
                surveyTemplateService.getById(getId(request));

        ListRoute<SurveyTemplate> findAllActiveRoute = (request, response) ->
                surveyTemplateService.findAllActive();

        getForList(findAllActivePath, findAllActiveRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
