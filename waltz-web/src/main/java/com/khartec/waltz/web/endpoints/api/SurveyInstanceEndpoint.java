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
import com.khartec.waltz.model.StringChangeCommand;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

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
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForUserPath = mkPath(BASE_URL, "user");
        String findForSurveyRunPath = mkPath(BASE_URL, "run", ":id");
        String findPreviousVersionsPath = mkPath(BASE_URL, "id", ":id", "previous-versions");
        String findRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String findResponsesPath = mkPath(BASE_URL, ":id", "responses");
        String saveResponsePath = mkPath(BASE_URL, ":id", "response");
        String updateStatusPath = mkPath(BASE_URL, ":id", "status");
        String updateDueDatePath = mkPath(BASE_URL, ":id", "due-date");
        String markApprovedPath = mkPath(BASE_URL, ":id", "approval");
        String recipientPath = mkPath(BASE_URL, ":id", "recipient");
        String deleteRecipientPath = mkPath(BASE_URL, ":id", "recipient", ":instanceRecipientId");

        DatumRoute<SurveyInstance> getByIdRoute =
                (req, res) -> surveyInstanceService.getById(getId(req));

        ListRoute<SurveyInstance> findByEntityRefRoute = (req, res)
                -> surveyInstanceService.findBySurveyInstanceIdSelector(mkOpts(getEntityReference(req), EXACT));

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
                            .newStatus(SurveyInstanceStatus.IN_PROGRESS)
                            .build());

            return result;
        };

        DatumRoute<Integer> updateStatusRoute =
                (req, res) -> {
                    SurveyInstanceStatusChangeCommand command = readBody(req, SurveyInstanceStatusChangeCommand.class);

                    if (command.newStatus() != SurveyInstanceStatus.COMPLETED) {
                        ensureUserHasAdminRights(req);
                    }

                    return surveyInstanceService.updateStatus(
                            getUsername(req),
                            getId(req),
                            command
                    );
                };

        DatumRoute<Integer> updateDueDateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            DateChangeCommand command = readBody(req, DateChangeCommand.class);

            return surveyInstanceService.updateDueDate(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command);
        };

        DatumRoute<Integer> markApprovedRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            StringChangeCommand command = readBody(req, StringChangeCommand.class);

            return surveyInstanceService.markApproved(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command.newStringVal().orElse(null));
        };


        DatumRoute<Boolean> updateRecipientRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);

                    SurveyInstanceRecipientUpdateCommand command = readBody(req, SurveyInstanceRecipientUpdateCommand.class);
                    return surveyInstanceService.updateRecipient(command);
                };

        DatumRoute<Long> addRecipientRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);

                    SurveyInstanceRecipientCreateCommand command = readBody(req, SurveyInstanceRecipientCreateCommand.class);
                    return surveyInstanceService.addRecipient(command);
                };

        DatumRoute<Boolean> deleteRecipientRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);

                    long instanceRecipientId = getLong(req, "instanceRecipientId");
                    return surveyInstanceService.delete(instanceRecipientId);
                };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findForUserPath, findForUserRoute);
        getForList(findForSurveyRunPath, findForSurveyRunRoute);
        getForList(findPreviousVersionsPath, findPreviousVersionsRoute);
        getForList(findRecipientsPath, findRecipientsRoute);
        getForList(findResponsesPath, findResponsesRoute);
        putForDatum(saveResponsePath, saveResponseRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
        putForDatum(updateDueDatePath, updateDueDateRoute);
        putForDatum(markApprovedPath, markApprovedRoute);
        putForDatum(recipientPath, updateRecipientRoute);
        postForDatum(recipientPath, addRecipientRoute);
        deleteForDatum(deleteRecipientPath, deleteRecipientRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SURVEY_ADMIN);
    }
}
