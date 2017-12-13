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

import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionFieldType;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.survey.SurveyQuestionDropdownEntryService;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ImmutableSurveyQuestionInfo;
import com.khartec.waltz.web.json.SurveyQuestionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.util.stream.Collectors.toList;


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
        String findForInstancePath = mkPath(BASE_URL, "instance", ":id");
        String findForTemplatePath = mkPath(BASE_URL, "template", ":id");
        String deletePath = mkPath(BASE_URL, ":id");

        ListRoute<SurveyQuestionInfo> findForInstanceRoute =
                (req, res) -> {
                    List<SurveyQuestion> questions = surveyQuestionService.findForSurveyInstance(getId(req));
                    return questions.stream()
                            .map(q -> mkQuestionInfo(q))
                            .collect(toList());
                };

        ListRoute<SurveyQuestionInfo> findForTemplateRoute =
                (req, res) -> {
                    List<SurveyQuestion> questions = surveyQuestionService.findForSurveyTemplate(getId(req));
                    return questions.stream()
                            .map(q -> mkQuestionInfo(q))
                            .collect(toList());
                };

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

        getForList(findForInstancePath, findForInstanceRoute);
        getForList(findForTemplatePath, findForTemplateRoute);
        postForDatum(BASE_URL, createRoute);
        putForDatum(BASE_URL, updateRoute);
        deleteForDatum(deletePath, deleteRoute);
    }


    private SurveyQuestionInfo mkQuestionInfo(SurveyQuestion question) {
        ImmutableSurveyQuestionInfo.Builder builder = ImmutableSurveyQuestionInfo.builder();

        if (question.fieldType() == SurveyQuestionFieldType.DROPDOWN) {
            builder.dropdownEntries(surveyQuestionDropdownEntryService.findForQuestion(question.id().get()));
        }
        return builder
                .question(question)
                .build();
    }


    private boolean mayBeSaveDropdownEntries(long questionId, SurveyQuestionInfo questionInfo) {
        checkNotNull(questionInfo.question(), "questionInfo.question() cannot be null");

        if (questionInfo.question().fieldType() == SurveyQuestionFieldType.DROPDOWN) {
            return surveyQuestionDropdownEntryService.saveEntries(
                    questionId,
                    questionInfo.dropdownEntries());
        }

        return false;
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SURVEY_TEMPLATE_ADMIN);
    }
}
