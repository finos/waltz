package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.*;
import com.khartec.waltz.model.rating.RagRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.model.EntityReference.mkRef;

@Service
public class AppHelper {

    @Autowired
    private ApplicationDao applicationDao;


    public EntityReference createNewApp(String name, Long ouId) {
        AppRegistrationResponse resp = applicationDao
                .registerApp(ImmutableAppRegistrationRequest.builder()
                        .name(name)
                        .organisationalUnitId(ouId != null ? ouId : 1L)
                        .applicationKind(ApplicationKind.IN_HOUSE)
                        .businessCriticality(Criticality.MEDIUM)
                        .lifecyclePhase(LifecyclePhase.PRODUCTION)
                        .overallRating(RagRating.G)
                        .businessCriticality(Criticality.MEDIUM)
                        .build());

        return resp.id().map(id -> mkRef(EntityKind.APPLICATION, id)).get();
    }


    public void removeApp(Long appId){
        Application app = applicationDao.getById(appId);
        applicationDao
                .update(ImmutableApplication
                        .copyOf(app)
                        .withIsRemoved(true)
                        .withEntityLifecycleStatus(EntityLifecycleStatus.REMOVED));
    }

}
