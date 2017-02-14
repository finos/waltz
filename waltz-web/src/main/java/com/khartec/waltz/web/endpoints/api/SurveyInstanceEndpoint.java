package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

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
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForUserPath = mkPath(BASE_URL, "user");
        String findResponsesPath = mkPath(BASE_URL, ":id", "responses");
        String saveResponsePath = mkPath(BASE_URL, ":id", "response");
        String updateStatusPath = mkPath(BASE_URL, ":id", "status");

        DatumRoute<SurveyInstance> getByIdRoute =
                (req, res) -> surveyInstanceService.getById(getId(req));

        ListRoute<SurveyInstance> findByEntityRefRoute = (req, res)
                -> surveyInstanceService.findBySurveyInstanceIdSelector(mkOpts(getEntityReference(req), EXACT));

        ListRoute<SurveyInstance> findForUserRoute =
                (req, res) -> surveyInstanceService.findForRecipient(getUsername(req));

        ListRoute<SurveyInstanceQuestionResponse> findResponsesRoute =
                (req, res) -> surveyInstanceService.findResponses(getId(req));

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
                (req, res) -> surveyInstanceService.updateStatus(
                        getUsername(req),
                        getId(req),
                        readBody(req, SurveyInstanceStatusChangeCommand.class)
                );

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findForUserPath, findForUserRoute);
        getForList(findResponsesPath, findResponsesRoute);
        putForDatum(saveResponsePath, saveResponseRoute);
        postForDatum(updateStatusPath, updateStatusRoute);
    }
}
