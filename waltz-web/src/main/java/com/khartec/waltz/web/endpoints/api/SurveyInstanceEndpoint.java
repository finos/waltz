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


import com.khartec.waltz.model.DateChangeCommand;
import com.khartec.waltz.model.StringChangeCommand;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class SurveyInstanceEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-instance");

    private final SurveyInstanceService surveyInstanceService;
    private final UserRoleService userRoleService;


    @Autowired
    public SurveyInstanceEndpoint(SurveyInstanceService surveyInstanceService,
                                  UserRoleService userRoleService) {
        checkNotNull(surveyInstanceService, "surveyInstanceService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.surveyInstanceService = surveyInstanceService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String getPermissionsPath = mkPath(BASE_URL, ":id", "permissions");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForRecipientIdPath = mkPath(BASE_URL, "recipient", "id", ":id");
        String findForUserPath = mkPath(BASE_URL, "user");
        String findForSurveyRunPath = mkPath(BASE_URL, "run", ":id");
        String findPreviousVersionsPath = mkPath(BASE_URL, "id", ":id", "previous-versions");
        String findRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String findResponsesPath = mkPath(BASE_URL, ":id", "responses");
        String findPossibleActionsPath = mkPath(BASE_URL, ":id", "actions");
        String saveResponsePath = mkPath(BASE_URL, ":id", "response");
        String updateStatusPath = mkPath(BASE_URL, ":id", "status");
        String updateDueDatePath = mkPath(BASE_URL, ":id", "due-date");
        String recipientPath = mkPath(BASE_URL, ":id", "recipient");
        String deleteRecipientPath = mkPath(BASE_URL, ":id", "recipient", ":instanceRecipientId");

        DatumRoute<SurveyInstance> getByIdRoute =
                (req, res) -> surveyInstanceService.getById(getId(req));

        DatumRoute<SurveyInstancePermissions> getPermissionsRoute = (req, res) -> {
            String userName = getUsername(req);
            Long instanceId = getId(req);
            return surveyInstanceService.getPermissions(userName, instanceId);
        };

        ListRoute<SurveyInstance> findByEntityRefRoute = (req, res)
                -> surveyInstanceService.findBySurveyInstanceIdSelector(mkOpts(getEntityReference(req), EXACT));

        ListRoute<SurveyInstance> findForRecipientIdRoute = (req, res)
                -> surveyInstanceService.findForRecipient(getId(req));

        ListRoute<SurveyInstance> findForUserRoute =
                (req, res) -> surveyInstanceService.findForRecipient(getUsername(req));

        ListRoute<SurveyInstanceQuestionResponse> findResponsesRoute =
                (req, res) -> surveyInstanceService.findResponses(getId(req));

        ListRoute<SurveyInstanceRecipient> findRecipientsRoute =
                (req, res) -> surveyInstanceService.findRecipients(getId(req));

        ListRoute<SurveyInstance> findForSurveyRunRoute =
                (req, res) -> surveyInstanceService.findForSurveyRun(getId(req));

        ListRoute<SurveyInstance> findPreviousVersionsRoute =
                (req, res) -> surveyInstanceService.findPreviousVersionsForInstance(getId(req));

        ListRoute<SurveyInstanceAction> findPossibleActionsRoute =
                (req, res) -> surveyInstanceService.findPossibleActionsForInstance(getUsername(req), getId(req));

        DatumRoute<Boolean> saveResponseRoute = (req, res) -> {
            String userName = getUsername(req);
            Long instanceId = getId(req);
            SurveyQuestionResponse questionResponse = readBody(req, SurveyQuestionResponse.class);

            boolean result = surveyInstanceService.saveResponse(userName, instanceId, questionResponse);

            // set status to in progress
            surveyInstanceService.updateStatus(
                    userName,
                    instanceId,
                    ImmutableSurveyInstanceStatusChangeCommand.builder()
                            .action(SurveyInstanceAction.SAVING)
                            .build());

            return result;
        };

        DatumRoute<SurveyInstanceStatus> updateStatusRoute =
                (req, res) -> {
                    SurveyInstanceStatusChangeCommand command = readBody(req, SurveyInstanceStatusChangeCommand.class);

                    return surveyInstanceService.updateStatus(
                            getUsername(req),
                            getId(req),
                            command
                    );
                };

        DatumRoute<Integer> updateDueDateRoute = (req, res) -> {
            DateChangeCommand command = readBody(req, DateChangeCommand.class);

            return surveyInstanceService.updateDueDate(
                    getUsername(req),
                    getId(req),
                    command);
        };

        DatumRoute<Boolean> updateRecipientRoute =
                (req, res) -> {
                    SurveyInstanceRecipientUpdateCommand command = readBody(req, SurveyInstanceRecipientUpdateCommand.class);
                    return surveyInstanceService.updateRecipient(getUsername(req), command);
                };

        DatumRoute<Long> addRecipientRoute =
                (req, res) -> {
                    SurveyInstanceRecipientCreateCommand command = readBody(req, SurveyInstanceRecipientCreateCommand.class);
                    return surveyInstanceService.addRecipient(getUsername(req), command);
                };

        DatumRoute<Boolean> deleteRecipientRoute =
                (req, res) -> surveyInstanceService.deleteRecipient(
                        getUsername(req),
                        getId(req),
                        getLong(req, "instanceRecipientId"));


        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getPermissionsPath, getPermissionsRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findForRecipientIdPath, findForRecipientIdRoute);
        getForList(findForUserPath, findForUserRoute);
        getForList(findForSurveyRunPath, findForSurveyRunRoute);
        getForList(findPreviousVersionsPath, findPreviousVersionsRoute);
        getForList(findRecipientsPath, findRecipientsRoute);
        getForList(findResponsesPath, findResponsesRoute);
        getForList(findPossibleActionsPath, findPossibleActionsRoute);
        putForDatum(saveResponsePath, saveResponseRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
        putForDatum(updateDueDatePath, updateDueDateRoute);
        putForDatum(recipientPath, updateRecipientRoute);
        postForDatum(recipientPath, addRecipientRoute);
        deleteForDatum(deleteRecipientPath, deleteRecipientRoute);
    }

}
