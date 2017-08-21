package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.person.Person;
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
        String attestInstancePath = mkPath(BASE_URL, "attest", ":id");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByRunIdPath = mkPath(BASE_URL, "run", ":id");
        String findByUserPath = mkPath(BASE_URL, "user");
        String findPersonsByInstancePath = mkPath(BASE_URL, ":id", "person");

        DatumRoute<Boolean> attestInstanceRoute =
                (req, res) -> attestationInstanceService.attestInstance(
                            getId(req),
                            getUsername(req));

        ListRoute<AttestationInstance> findByEntityRefRoute =
                (req, res) -> attestationInstanceService.findByEntityReference(getEntityReference(req));
        ListRoute<AttestationInstance> findByUserRoute =
                (req, res) -> attestationInstanceService.findByRecipient(getUsername(req));

        ListRoute<AttestationInstance> findByRunIdRoute =
                (req, res) -> attestationInstanceService.findByRunId(getId(req));

        ListRoute<Person> findPersonsByInstanceRoute = (request, response) -> {
            long id = Long.valueOf(request.params("id"));
            return attestationInstanceService.findPersonsByInstanceId(id);
        };


        postForDatum(attestInstancePath, attestInstanceRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findByUserPath, findByUserRoute);
        getForList(findByRunIdPath, findByRunIdRoute);
        getForList(findPersonsByInstancePath, findPersonsByInstanceRoute);
    }

}
