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

import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.survey.SurveyRunService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class SurveyRunEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SurveyRunEndpoint.class);

    private static final String BASE_URL = mkPath("api", "survey-run");

    private final SurveyRunService surveyRunService;


    @Autowired
    public SurveyRunEndpoint(SurveyRunService surveyRunService) {
        checkNotNull(surveyRunService, "surveyRunService must not be null");

        this.surveyRunService = surveyRunService;
    }


    @Override
    public void register() {
        String surveyRunUpdatePath = mkPath(BASE_URL, ":id");
        String generateSurveyRunRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String createSurveyRunInstancesAndRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String updateSurveyRunStatusPath = mkPath(BASE_URL, ":id", "status");

        DatumRoute<SurveyRunCreateResponse> surveyRunCreateRoute = (req, res) -> {
            res.type(WebUtilities.TYPE_JSON);
            SurveyRunCreateCommand surveyRunChangeCommand = readBody(req, SurveyRunCreateCommand.class);

            return surveyRunService
                    .createSurveyRun(WebUtilities.getUsername(req), surveyRunChangeCommand);
        };

        DatumRoute<Integer> surveyRunUpdateRoute = (req, res) -> {
            res.type(WebUtilities.TYPE_JSON);
            SurveyRunChangeCommand surveyRunChangeCommand = readBody(req, SurveyRunChangeCommand.class);

            return surveyRunService
                    .updateSurveyRun(WebUtilities.getUsername(req),
                            getId(req),
                            surveyRunChangeCommand);
        };

        DatumRoute<Integer> surveyRunUpdateStatusRoute = (req, res) -> {
            res.type(WebUtilities.TYPE_JSON);
            SurveyRunStatusChangeCommand surveyRunStatusChangeCommand = readBody(req, SurveyRunStatusChangeCommand.class);

            return surveyRunService
                    .updateSurveyRunStatus(
                            WebUtilities.getUsername(req),
                            getId(req),
                            surveyRunStatusChangeCommand.newStatus());
        };

        ListRoute<SurveyInstanceRecipient> generateSurveyRunRecipientsRoute = (request, response) ->
                surveyRunService.generateSurveyInstanceRecipients(getId(request));

        DatumRoute<Boolean> createSurveyRunInstancesAndRecipientsRoute = (request, response) ->
                surveyRunService.createSurveyInstancesAndRecipients(getId(request), newArrayList(readBody(request, SurveyInstanceRecipient[].class)));

        getForList(generateSurveyRunRecipientsPath, generateSurveyRunRecipientsRoute);
        postForDatum(BASE_URL, surveyRunCreateRoute);
        putForDatum(surveyRunUpdatePath, surveyRunUpdateRoute);
        postForDatum(createSurveyRunInstancesAndRecipientsPath, createSurveyRunInstancesAndRecipientsRoute);
        putForDatum(updateSurveyRunStatusPath, surveyRunUpdateStatusRoute);
    }
}
