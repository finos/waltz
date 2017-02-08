package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.survey.SurveyInstance;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getUsername;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class SurveyInstanceEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "survey-instance");

    private final SurveyInstanceService surveyInstanceService;


    @Autowired
    public SurveyInstanceEndpoint(SurveyInstanceService surveyInstanceService) {
        checkNotNull(surveyInstanceService, "surveyInstanceService cannot be null");

        this.surveyInstanceService = surveyInstanceService;
    }


    @Override
    public void register() {
        String surveyInstanceListForUserPath = mkPath(BASE_URL, "user");

        ListRoute<SurveyInstance> surveyInstanceListForUserRoute =
                (req, res) -> surveyInstanceService.findForRecipient(getUsername(req));

        getForList(surveyInstanceListForUserPath, surveyInstanceListForUserRoute);
    }
}
