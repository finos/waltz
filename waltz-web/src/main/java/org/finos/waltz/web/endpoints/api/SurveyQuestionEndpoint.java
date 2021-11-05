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

import org.finos.waltz.service.survey.SurveyQuestionDropdownEntryService;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.json.SurveyQuestionInfo;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.finos.waltz.model.user.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class SurveyQuestionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-question");

    private final SurveyQuestionService surveyQuestionService;
    private final SurveyQuestionDropdownEntryService surveyQuestionDropdownEntryService;
    private final UserRoleService userRoleService;


    @Autowired
    public SurveyQuestionEndpoint(SurveyQuestionService surveyQuestionService,
                                  SurveyQuestionDropdownEntryService surveyQuestionDropdownEntryService,
                                  UserRoleService userRoleService) {
        checkNotNull(surveyQuestionService, "surveyQuestionService cannot be null");
        checkNotNull(surveyQuestionDropdownEntryService, "surveyQuestionDropdownEntryService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.surveyQuestionService = surveyQuestionService;
        this.surveyQuestionDropdownEntryService = surveyQuestionDropdownEntryService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String deletePath = mkPath(BASE_URL, ":id");

        ListRoute<SurveyQuestion> findQuestionsForInstance = (req, res) -> surveyQuestionService.findForSurveyInstance(getId(req));
        ListRoute<SurveyQuestionDropdownEntry> findDropdownEntriesForInstance = (req, res) -> surveyQuestionDropdownEntryService.findForSurveyInstance(getId(req));

        ListRoute<SurveyQuestion> findQuestionsForTemplate = (req, res) -> surveyQuestionService.findForSurveyTemplate(getId(req));
        ListRoute<SurveyQuestionDropdownEntry> findDropdownEntriesForTemplate = (req, res) -> surveyQuestionDropdownEntryService.findForSurveyTemplate(getId(req));

        DatumRoute<Long> createRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    SurveyQuestionInfo surveyQuestionInfo = readBody(req, SurveyQuestionInfo.class);
                    long questionId = surveyQuestionService.create(surveyQuestionInfo.question());
                    mayBeSaveDropdownEntries(questionId, surveyQuestionInfo);
                    return questionId;
                };

        DatumRoute<Integer> updateRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    SurveyQuestionInfo surveyQuestionInfo = readBody(req, SurveyQuestionInfo.class);
                    int updateCount = surveyQuestionService.update(surveyQuestionInfo.question());
                    mayBeSaveDropdownEntries(surveyQuestionInfo.question().id().get(), surveyQuestionInfo);
                    return updateCount;
                };

        DatumRoute<Integer> deleteRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyQuestionService.delete(getId(req));
                };

        getForList(mkPath(BASE_URL, "questions", "instance", ":id"), findQuestionsForInstance);
        getForList(mkPath(BASE_URL, "dropdown-entries", "instance", ":id"), findDropdownEntriesForInstance);

        getForList(mkPath(BASE_URL, "questions", "template", ":id"), findQuestionsForTemplate);
        getForList(mkPath(BASE_URL, "dropdown-entries", "template", ":id"), findDropdownEntriesForTemplate);

        postForDatum(BASE_URL, createRoute);
        putForDatum(BASE_URL, updateRoute);
        deleteForDatum(deletePath, deleteRoute);
    }


    private boolean mayBeSaveDropdownEntries(long questionId, SurveyQuestionInfo questionInfo) {
        checkNotNull(questionInfo.question(), "questionInfo.question() cannot be null");

        if (questionInfo.question().fieldType() == SurveyQuestionFieldType.DROPDOWN
                || questionInfo.question().fieldType() == SurveyQuestionFieldType.DROPDOWN_MULTI_SELECT) {
            return surveyQuestionDropdownEntryService.saveEntries(
                    questionId,
                    questionInfo.dropdownEntries());
        }

        return false;
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.SURVEY_TEMPLATE_ADMIN);
    }
}
