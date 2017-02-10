package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class SurveyQuestionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-question");

    private final SurveyQuestionService surveyQuestionService;


    @Autowired
    public SurveyQuestionEndpoint(SurveyQuestionService surveyQuestionService) {
        checkNotNull(surveyQuestionService, "surveyQuestionService cannot be null");

        this.surveyQuestionService = surveyQuestionService;
    }


    @Override
    public void register() {
        String findForInstancePath = mkPath(BASE_URL, "instance", ":id");

        ListRoute<SurveyQuestion> findForInstanceRoute =
                (req, res) -> surveyQuestionService.findForSurveyInstance(getId(req));

        getForList(findForInstancePath, findForInstanceRoute);
    }
}
