package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.attestation.AttestationCreateSummary;
import com.khartec.waltz.model.attestation.AttestationRun;
import com.khartec.waltz.model.attestation.AttestationRunCreateCommand;
import com.khartec.waltz.model.survey.SurveyRun;
import com.khartec.waltz.model.survey.SurveyRunCreateCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.attestation.AttestationRunService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class AttestationRunEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "attestation-run");

    private final AttestationRunService attestationRunService;
    private final UserRoleService userRoleService;


    @Autowired
    public AttestationRunEndpoint(AttestationRunService attestationRunService,
                                  UserRoleService userRoleService) {
        checkNotNull(attestationRunService, "attestationRunService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.attestationRunService = attestationRunService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByRecipientPath = mkPath(BASE_URL, "user");
        String getCreateSummaryPath = mkPath(BASE_URL, "create-summary");


        DatumRoute<AttestationRun> getByIdRoute = (req, res) ->
                attestationRunService.getById(getId(req));

        ListRoute<AttestationRun> findByRecipientRoute = (req, res) ->
                attestationRunService.findByRecipient(WebUtilities.getUsername(req));

        DatumRoute<AttestationCreateSummary> getCreateSummaryRoute = (req, res) -> {
            AttestationRunCreateCommand createCommand = readBody(req, AttestationRunCreateCommand.class);
            return attestationRunService
                    .getCreateSummary(createCommand);
        };

        DatumRoute<IdCommandResponse> attestationRunCreateRoute = (req, res) -> {
            ensureUserHasAdminRights(req);

            res.type(WebUtilities.TYPE_JSON);
            AttestationRunCreateCommand createCommand = readBody(req, AttestationRunCreateCommand.class);

            return attestationRunService
                    .create(WebUtilities.getUsername(req), createCommand);
        };

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByRecipientPath, findByRecipientRoute);
        postForDatum(BASE_URL, attestationRunCreateRoute);
        postForDatum(getCreateSummaryPath, getCreateSummaryRoute);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.ADMIN);
    }
}
