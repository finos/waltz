package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.Criticality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.*;
import org.finos.waltz.model.rating.RagRating;
import org.finos.waltz.service.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class AppHelper {

    @Autowired
    private ApplicationService applicationSvc;


    public EntityReference createNewApp(String name, Long ouId) {
        return createNewApp(name, ouId, name);
    }


    public EntityReference createNewApp(String name, Long ouId, String assetCode) {
        AppRegistrationResponse resp = applicationSvc
                .registerApp(
                        ImmutableAppRegistrationRequest.builder()
                                .name(name)
                                .assetCode(assetCode)
                                .organisationalUnitId(ouId != null ? ouId : 1L)
                                .applicationKind(ApplicationKind.IN_HOUSE)
                                .businessCriticality(Criticality.MEDIUM)
                                .lifecyclePhase(LifecyclePhase.PRODUCTION)
                                .overallRating(RagRating.G)
                                .businessCriticality(Criticality.MEDIUM)
                                .build(),
                        "appHelper");

        return resp.id().map(id -> mkRef(EntityKind.APPLICATION, id, name)).get();
    }


    public void removeApp(Long appId) {
        Application app = applicationSvc.getById(appId);
        applicationSvc
                .update(ImmutableApplication
                        .copyOf(app)
                        .withIsRemoved(true)
                        .withEntityLifecycleStatus(EntityLifecycleStatus.REMOVED));
    }

}
