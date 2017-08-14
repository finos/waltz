package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.attestation.AttestCommand;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.service.attestation.AttestationInstanceService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class AttestationInstanceEndpoint implements Endpoint {
    private static final String BASE_URL = mkPath("api", "attestation-instance");
    private final AttestationInstanceService attestationInstanceService;


    @Autowired
    public AttestationInstanceEndpoint (AttestationInstanceService attestationInstanceService) {
        checkNotNull(attestationInstanceService, "attestationInstanceService cannot be null");

        this.attestationInstanceService = attestationInstanceService;
    }


    @Override
    public void register() {
        String findByUserPath = mkPath(BASE_URL, "user");
        String attestInstancePath = mkPath(BASE_URL, "attest", ":id");

        ListRoute<AttestationInstance> findByUserRoute =
                (req, res) -> attestationInstanceService.findByRecipient(getUsername(req));

        DatumRoute<Boolean> attestInstanceRoute =
                (req, res) -> {
                    AttestCommand command = readBody(req, AttestCommand.class);
                    return attestationInstanceService.attestInstance(
                            getId(req),
                            getUsername(req),
                            command.comments());
                };


        getForList(findByUserPath, findByUserRoute);
        postForDatum(attestInstancePath, attestInstanceRoute);
    }

}
