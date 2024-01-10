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


import org.finos.waltz.model.attestation.SyncRecipientsResponse;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.survey.SurveyInstanceService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.HierarchyQueryScope.EXACT;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class SurveyInstanceEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstanceEndpoint.class);
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
        String reassignRecipientsPath = mkPath(BASE_URL, "reassign-recipients");
        String reassignRecipientsCountsPath = mkPath(BASE_URL, "reassign-recipients-counts");
        String reassignOwnersPath = mkPath(BASE_URL, "reassign-owners");
        String reassignOwnersCountsPath = mkPath(BASE_URL, "reassign-owners-counts");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForRecipientIdPath = mkPath(BASE_URL, "recipient", "id", ":id");
        String findForSurveyRunPath = mkPath(BASE_URL, "run", ":id");
        String findPreviousVersionsPath = mkPath(BASE_URL, "id", ":id", "previous-versions");
        String findVersionsPath = mkPath(BASE_URL, "id", ":id", "versions");
        String findGroupApproversPath = mkPath(BASE_URL, ":id", "group-approvers");
        String findRecipientsPath = mkPath(BASE_URL, ":id", "recipients");
        String findOwnersPath = mkPath(BASE_URL, ":id", "owners");
        String findResponsesPath = mkPath(BASE_URL, ":id", "responses");
        String findPossibleActionsPath = mkPath(BASE_URL, ":id", "actions");
        String saveResponsePath = mkPath(BASE_URL, ":id", "response");
        String updateStatusPath = mkPath(BASE_URL, ":id", "status");
        String updateSubmissionDueDatePath = mkPath(BASE_URL, ":id", "submission-due-date");
        String updateApprovalDueDatePath = mkPath(BASE_URL, ":id", "approval-due-date");
        String recipientPath = mkPath(BASE_URL, ":id", "recipient");
        String deleteRecipientPath = mkPath(BASE_URL, ":id", "recipient", ":personId");
        String ownerPath = mkPath(BASE_URL, ":id", "owner");
        String deleteOwnerPath = mkPath(BASE_URL, ":id", "owner", ":personId");
        String reportProblemWithQuestionResponsePath = mkPath(BASE_URL, ":id", "response", ":questionId", "problem");
        String copyResponsesPath = mkPath(BASE_URL, ":id", "copy-responses");
        String withdrawOpenSurveysForRunPath = mkPath(BASE_URL, "run", ":id", "withdraw-open");
        String withdrawOpenSurveysForTemplatePath = mkPath(BASE_URL, "template", ":id", "withdraw-open");

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

        ListRoute<SurveyInstanceQuestionResponse> findResponsesRoute =
                (req, res) -> surveyInstanceService.findResponses(getId(req));

        ListRoute<Person> findRecipientsRoute =
                (req, res) -> surveyInstanceService.findRecipients(getId(req));

        ListRoute<Person> findGroupApproversRoute =
                (req, res) -> surveyInstanceService.findGroupApprovers(getId(req));

        ListRoute<Person> findOwnersRoute =
                (req, res) -> surveyInstanceService.findOwners(getId(req));

        DatumRoute<SyncRecipientsResponse> reassignRecipientsRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.ADMIN);
            LOG.info("User: {}, requested reassign recipients for surveys", getUsername(req));
            return surveyInstanceService.reassignRecipients();
        };

        DatumRoute<SyncRecipientsResponse> reassignRecipientsCountsRoute = (req, res) -> surveyInstanceService.getReassignRecipientsCounts();


        DatumRoute<SyncRecipientsResponse> reassignOwnersRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.ADMIN);
            LOG.info("User: {}, requested reassign owners for surveys", getUsername(req));
            return surveyInstanceService.reassignOwners();
        };

        DatumRoute<SyncRecipientsResponse> reassignOwnersCountsRoute = (req, res) -> surveyInstanceService.getReassignOwnersCounts();

        ListRoute<SurveyInstance> findForSurveyRunRoute =
                (req, res) -> surveyInstanceService.findForSurveyRun(getId(req));

        ListRoute<SurveyInstance> findPreviousVersionsRoute =
                (req, res) -> surveyInstanceService.findPreviousVersionsForInstance(getId(req));

        ListRoute<SurveyInstance> findVersionsRoute =
                (req, res) -> surveyInstanceService.findVersionsForInstance(getId(req));

        ListRoute<SurveyInstanceAction> findPossibleActionsRoute =
                (req, res) -> surveyInstanceService.findPossibleActionsForInstance(getUsername(req), getId(req));

        DatumRoute<Integer> copyResponsesRoute = (req, res) -> {
            CopySurveyResponsesCommand cloneCommand = readBody(req, CopySurveyResponsesCommand.class);
            return surveyInstanceService.copyResponses(getId(req), cloneCommand, getUsername(req));
        };

        DatumRoute<Boolean> saveResponseRoute = (req, res) -> {
            String userName = getUsername(req);
            Long instanceId = getId(req);
            SurveyQuestionResponse questionResponse = readBody(req, SurveyQuestionResponse.class);

            boolean result = surveyInstanceService.saveResponse(userName, instanceId, questionResponse);

            // set status to in progress
            surveyInstanceService.updateStatus(
                    Optional.empty(),
                    userName,
                    instanceId,
                    ImmutableSurveyInstanceStatusChangeCommand.builder()
                            .action(SurveyInstanceAction.SAVING)
                            .build());

            return result;
        };

        DatumRoute<Boolean> reportProblemWithQuestionResponseRoute = (req, res) -> {
            String userName = getUsername(req);
            Long instanceId = getId(req);
            Long questionId = getLong(req, "questionId");

            String message = req.body();

            boolean result = surveyInstanceService.reportProblemWithQuestionResponse(
                    instanceId,
                    questionId,
                    message,
                    userName);

            return result;
        };

        DatumRoute<SurveyInstanceStatus> updateStatusRoute =
                (req, res) -> {
                    SurveyInstanceStatusChangeCommand command = readBody(req, SurveyInstanceStatusChangeCommand.class);

                    return surveyInstanceService.updateStatus(
                            Optional.empty(),
                            getUsername(req),
                            getId(req),
                            command
                    );
                };

        DatumRoute<Integer> updateSubmissionDueDateRoute = (req, res) -> {
            LocalDate newDate = readBody(req, LocalDate.class);

            return surveyInstanceService.updateSubmissionDueDate(
                    getUsername(req),
                    getId(req),
                    newDate);
        };


        DatumRoute<Integer> updateApprovalDueDateRoute = (req, res) -> {
            LocalDate newDate = readBody(req, LocalDate.class);

            return surveyInstanceService.updateApprovalDueDate(
                    getUsername(req),
                    getId(req),
                    newDate);
        };

        DatumRoute<Long> addRecipientRoute =
                (req, res) -> {
                    SurveyInstanceRecipientCreateCommand command = ImmutableSurveyInstanceRecipientCreateCommand
                            .builder()
                            .surveyInstanceId(getId(req))
                            .personId(readBody(req, Long.class))
                            .build();

                    return surveyInstanceService.addRecipient(getUsername(req), command);
                };

        DatumRoute<Boolean> deleteRecipientRoute =
                (req, res) -> surveyInstanceService.deleteRecipient(
                        getUsername(req),
                        getId(req),
                        getLong(req, "personId"));

        DatumRoute<Long> addOwnerRoute =
                (req, res) -> {
                    SurveyInstanceOwnerCreateCommand command = ImmutableSurveyInstanceOwnerCreateCommand
                            .builder()
                            .surveyInstanceId(getId(req))
                            .personId(readBody(req, Long.class))
                            .build();
                    return surveyInstanceService.addOwner(getUsername(req), command);
                };

        DatumRoute<Boolean> deleteOwnerRoute =
                (req, res) -> surveyInstanceService.deleteOwner(
                        getUsername(req),
                        getId(req),
                        getLong(req, "personId"));

        DatumRoute<Integer> withdrawOpenSurveysForRunRoute =
                (req, res) -> surveyInstanceService
                        .withdrawOpenSurveysForRun(
                                getId(req),
                                getUsername(req));

        DatumRoute<Integer> withdrawOpenSurveysForTemplateRoute =
                (req, res) -> surveyInstanceService
                        .withdrawOpenSurveysForTemplate(
                                getId(req),
                                getUsername(req));

        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getPermissionsPath, getPermissionsRoute);
        postForDatum(reassignRecipientsPath, reassignRecipientsRoute);
        getForDatum(reassignRecipientsCountsPath, reassignRecipientsCountsRoute);
        postForDatum(reassignOwnersPath, reassignOwnersRoute);
        getForDatum(reassignOwnersCountsPath, reassignOwnersCountsRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findForRecipientIdPath, findForRecipientIdRoute);
        getForList(findForSurveyRunPath, findForSurveyRunRoute);
        getForList(findPreviousVersionsPath, findPreviousVersionsRoute);
        getForList(findVersionsPath, findVersionsRoute);
        getForList(findRecipientsPath, findRecipientsRoute);
        getForList(findGroupApproversPath, findGroupApproversRoute);
        getForList(findOwnersPath, findOwnersRoute);
        getForList(findResponsesPath, findResponsesRoute);
        getForList(findPossibleActionsPath, findPossibleActionsRoute);
        putForDatum(saveResponsePath, saveResponseRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
        putForDatum(updateSubmissionDueDatePath, updateSubmissionDueDateRoute);
        putForDatum(updateApprovalDueDatePath, updateApprovalDueDateRoute);
        postForDatum(recipientPath, addRecipientRoute);
        postForDatum(ownerPath, addOwnerRoute);
        deleteForDatum(deleteRecipientPath, deleteRecipientRoute);
        deleteForDatum(deleteOwnerPath, deleteOwnerRoute);
        postForDatum(reportProblemWithQuestionResponsePath, reportProblemWithQuestionResponseRoute);
        postForDatum(copyResponsesPath, copyResponsesRoute);
        postForDatum(withdrawOpenSurveysForRunPath, withdrawOpenSurveysForRunRoute);
        postForDatum(withdrawOpenSurveysForTemplatePath, withdrawOpenSurveysForTemplateRoute);
    }

}
