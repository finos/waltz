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

import com.khartec.waltz.model.DateChangeCommand;
import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.survey.SurveyRunService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class SurveyRunEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-run");

    private final SurveyRunService surveyRunService;

    private final UserRoleService userRoleService;


    @Autowired
    public SurveyRunEndpoint(SurveyRunService surveyRunService,
                             UserRoleService userRoleService) {
        checkNotNull(surveyRunService, "surveyRunService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.surveyRunService = surveyRunService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByTemplateIdPath = mkPath(BASE_URL, "template-id", ":id");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String surveyRunListForUserPath = mkPath(BASE_URL, "user");
        String surveyRunUpdatePath = mkPath(BASE_URL, ":id");
        String generateSurveyRunRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String createSurveyRunInstancesAndRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String createSurveyInstancesPath = mkPath(BASE_URL, ":id", "create-instances");
        String updateSurveyRunStatusPath = mkPath(BASE_URL, ":id", "status");
        String updateSurveyRunDueDatePath = mkPath(BASE_URL, ":id", "due-date");
        String getSurveyRunCompletionRate = mkPath(BASE_URL, ":id", "completion-rate");

        DatumRoute<SurveyRun> getByIdRoute = (req, res) ->
                surveyRunService.getById(getId(req));

        ListRoute<SurveyRun> findByTemplateIdRoute = (req, res) ->
                surveyRunService.findByTemplateId(getId(req));

        ListRoute<SurveyRun> findByEntityRoute = (req, res)
                -> surveyRunService.findBySurveyInstanceIdSelector(mkOpts(getEntityReference(req), EXACT));

        ListRoute<SurveyRun> surveyRunListForUserRoute = (req, res)
                -> surveyRunService.findForRecipient(getUsername(req));

        DatumRoute<IdCommandResponse> surveyRunCreateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            SurveyRunCreateCommand surveyRunChangeCommand = readBody(req, SurveyRunCreateCommand.class);

            return surveyRunService
                    .createSurveyRun(WebUtilities.getUsername(req), surveyRunChangeCommand);
        };

        DatumRoute<Integer> surveyRunUpdateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            SurveyRunChangeCommand surveyRunChangeCommand = readBody(req, SurveyRunChangeCommand.class);

            return surveyRunService.updateSurveyRun(
                    WebUtilities.getUsername(req),
                    getId(req),
                    surveyRunChangeCommand);
        };

        DatumRoute<Integer> surveyRunUpdateStatusRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            SurveyRunStatusChangeCommand surveyRunStatusChangeCommand = readBody(req, SurveyRunStatusChangeCommand.class);

            return surveyRunService.updateSurveyRunStatus(
                    WebUtilities.getUsername(req),
                    getId(req),
                    surveyRunStatusChangeCommand.newStatus());
        };

        DatumRoute<Integer> surveyRunUpdateDueDateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            DateChangeCommand command = readBody(req, DateChangeCommand.class);

            return surveyRunService.updateSurveyRunDueDate(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command);
        };

        ListRoute<SurveyInstanceRecipient> generateSurveyRunRecipientsRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            return surveyRunService.generateSurveyInstanceRecipients(getId(request));
        };

        DatumRoute<Boolean> createSurveyRunInstancesAndRecipientsRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            return surveyRunService.createSurveyInstancesAndRecipients(
                    getId(request),
                    newArrayList(readBody(request, SurveyInstanceRecipient[].class)));
        };

        DatumRoute<Boolean> createSurveyInstancesRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            long runId = getId(request);
            List<Long> personIds = readIdsFromBody(request);
            return surveyRunService.createDirectSurveyInstances(
                    runId,
                    personIds);
        };

        DatumRoute<SurveyRunCompletionRate> getSurveyRunCompletionRateRoute = (request, response)
                -> surveyRunService.getCompletionRate(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByTemplateIdPath, findByTemplateIdRoute);
        getForList(findByEntityRefPath, findByEntityRoute);
        getForList(generateSurveyRunRecipientsPath, generateSurveyRunRecipientsRoute);
        getForList(surveyRunListForUserPath, surveyRunListForUserRoute);
        postForDatum(BASE_URL, surveyRunCreateRoute);
        putForDatum(surveyRunUpdatePath, surveyRunUpdateRoute);
        postForDatum(createSurveyRunInstancesAndRecipientsPath, createSurveyRunInstancesAndRecipientsRoute);
        postForDatum(createSurveyInstancesPath, createSurveyInstancesRoute);
        putForDatum(updateSurveyRunStatusPath, surveyRunUpdateStatusRoute);
        putForDatum(updateSurveyRunDueDatePath, surveyRunUpdateDueDateRoute);
        getForDatum(getSurveyRunCompletionRate, getSurveyRunCompletionRateRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SURVEY_ADMIN);
    }

}
