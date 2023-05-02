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

import org.finos.waltz.model.DateChangeCommand;
import org.finos.waltz.model.IdCommandResponse;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.survey.SurveyRunService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.HierarchyQueryScope.EXACT;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

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
        String createSurveyRunPath = BASE_URL;
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByTemplateIdPath = mkPath(BASE_URL, "template-id", ":id");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForRecipientIdPath = mkPath(BASE_URL, "recipient", "id", ":id");
        String surveyRunListForUserPath = mkPath(BASE_URL, "user");
        String surveyRunUpdatePath = mkPath(BASE_URL, ":id");
        String surveyRunDeletePath = mkPath(BASE_URL, ":id");
        String generateSurveyRunRecipientsPath = mkPath(BASE_URL, "recipients");
        String createSurveyRunInstancesAndRecipientsPath = mkPath(BASE_URL, "create-instance-recipients");
        String createSurveyInstancesPath = mkPath(BASE_URL, ":id", "create-instances");
        String updateSurveyRunStatusPath = mkPath(BASE_URL, ":id", "status");
        String updateSurveyRunDueDatePath = mkPath(BASE_URL, ":id", "due-date");
        String updateSurveyRunApprovalDueDatePath = mkPath(BASE_URL, ":id", "approval-due-date");
        String updateOwningRolePath = mkPath(BASE_URL, ":id", "role");
        String getSurveyRunCompletionRate = mkPath(BASE_URL, ":id", "completion-rate");

        DatumRoute<SurveyRun> getByIdRoute = (req, res) ->
                surveyRunService.getById(getId(req));

        ListRoute<SurveyRunWithOwnerAndStats> findByTemplateIdRoute = (req, res) ->
                surveyRunService.findByTemplateId(getId(req));

        ListRoute<SurveyRun> findByEntityRoute = (req, res)
                -> surveyRunService.findBySurveyInstanceIdSelector(mkOpts(getEntityReference(req), EXACT));

        ListRoute<SurveyRun> findForRecipientIdRoute = (req, res)
                -> surveyRunService.findForRecipient(getId(req));

        ListRoute<SurveyRun> surveyRunListForUserRoute = (req, res)
                -> surveyRunService.findForRecipient(getUsername(req));

        DatumRoute<IdCommandResponse> surveyRunCreateRoute = (req, res) -> {
            SurveyRunCreateCommand surveyRunChangeCommand = readBody(req, SurveyRunCreateCommand.class);

            return surveyRunService
                    .createSurveyRun(getUsername(req), surveyRunChangeCommand);
        };

        DatumRoute<Boolean> surveyRunDeleteRoute = (req, res) ->
                surveyRunService.deleteSurveyRun(getUsername(req), getId(req));

        DatumRoute<Integer> surveyRunUpdateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            SurveyRunChangeCommand surveyRunChangeCommand = readBody(req, SurveyRunChangeCommand.class);

            return surveyRunService.updateSurveyRun(
                    getUsername(req),
                    getId(req),
                    surveyRunChangeCommand);
        };

        DatumRoute<Integer> surveyRunUpdateStatusRoute = (req, res) -> {
            SurveyRunStatusChangeCommand surveyRunStatusChangeCommand = readBody(req, SurveyRunStatusChangeCommand.class);

            return surveyRunService.updateSurveyRunStatus(
                    getUsername(req),
                    getId(req),
                    surveyRunStatusChangeCommand.newStatus());
        };

        DatumRoute<Integer> surveyRunUpdateDueDateRoute = (req, res) -> {
            DateChangeCommand command = readBody(req, DateChangeCommand.class);

            return surveyRunService.updateSurveyRunDueDate(
                    getUsername(req),
                    getId(req),
                    command);
        };


        DatumRoute<Integer> surveyRunUpdateApprovalDueDateRoute = (req, res) -> {
            DateChangeCommand command = readBody(req, DateChangeCommand.class);

            return surveyRunService.updateSurveyRunApprovalDueDate(
                    getUsername(req),
                    getId(req),
                    command);
        };

        DatumRoute<Integer> surveyRunUpdateOwningRolesRoute = (req, res) -> {

            SurveyInstanceOwningRoleSaveCommand owningRole = readBody(req, SurveyInstanceOwningRoleSaveCommand.class);

            return surveyRunService.updateSurveyInstanceOwningRoles(
                    getUsername(req),
                    getId(req),
                    owningRole);
        };

        ListRoute<SurveyInstanceRecipient> generateSurveyRunRecipientsRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            InstancesAndRecipientsCreateCommand command = readBody(request, InstancesAndRecipientsCreateCommand.class);

            return surveyRunService.generateSurveyInstanceRecipients(command);
        };

        DatumRoute<Boolean> createSurveyRunInstancesAndRecipientsRoute = (request, response) -> {
            ensureUserHasAdminRights(request);

            return surveyRunService.createSurveyInstancesAndRecipients(readBody(request, InstancesAndRecipientsCreateCommand.class));
        };

        DatumRoute<Boolean> createSurveyInstancesRoute = (request, response) -> {
            long runId = getId(request);

            SurveyInstanceRecipientsAndOwners recipientsAndOwners = readBody(request, SurveyInstanceRecipientsAndOwners.class);

            return surveyRunService.createDirectSurveyInstances(runId, recipientsAndOwners);
        };

        DatumRoute<SurveyRunCompletionRate> getSurveyRunCompletionRateRoute = (request, response)
                -> surveyRunService.getCompletionRate(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByTemplateIdPath, findByTemplateIdRoute);
        getForList(findByEntityRefPath, findByEntityRoute);
        getForList(findForRecipientIdPath, findForRecipientIdRoute);
        postForList(generateSurveyRunRecipientsPath, generateSurveyRunRecipientsRoute);
        getForList(surveyRunListForUserPath, surveyRunListForUserRoute);
        postForDatum(createSurveyRunPath, surveyRunCreateRoute);
        deleteForDatum(surveyRunDeletePath, surveyRunDeleteRoute);
        putForDatum(surveyRunUpdatePath, surveyRunUpdateRoute);
        postForDatum(createSurveyRunInstancesAndRecipientsPath, createSurveyRunInstancesAndRecipientsRoute);
        postForDatum(createSurveyInstancesPath, createSurveyInstancesRoute);
        putForDatum(updateSurveyRunStatusPath, surveyRunUpdateStatusRoute);
        putForDatum(updateSurveyRunDueDatePath, surveyRunUpdateDueDateRoute);
        putForDatum(updateSurveyRunApprovalDueDatePath, surveyRunUpdateApprovalDueDateRoute);
        putForDatum(updateOwningRolePath, surveyRunUpdateOwningRolesRoute);
        getForDatum(getSurveyRunCompletionRate, getSurveyRunCompletionRateRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.SURVEY_ADMIN);
    }

}
