package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class SurveyQuestionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-question");

    private final SurveyQuestionService surveyQuestionService;
    private final UserRoleService userRoleService;


    @Autowired
    public SurveyQuestionEndpoint(SurveyQuestionService surveyQuestionService,
                                  UserRoleService userRoleService) {
        checkNotNull(surveyQuestionService, "surveyQuestionService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.surveyQuestionService = surveyQuestionService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findForInstancePath = mkPath(BASE_URL, "instance", ":id");
        String findForTemplatePath = mkPath(BASE_URL, "template", ":id");
        String deletePath = mkPath(BASE_URL, ":id");

        ListRoute<SurveyQuestion> findForInstanceRoute =
                (req, res) -> surveyQuestionService.findForSurveyInstance(getId(req));

        ListRoute<SurveyQuestion> findForTemplateRoute =
                (req, res) -> surveyQuestionService.findForSurveyTemplate(getId(req));

        DatumRoute<Long> createRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyQuestionService.create(readBody(req, SurveyQuestion.class));
                };

        DatumRoute<Integer> updateRoute =
                (req, res) -> {
                    ensureUserHasAdminRights(req);
                    return surveyQuestionService.update(readBody(req, SurveyQuestion.class));
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


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.SURVEY_ADMIN);
    }
}
