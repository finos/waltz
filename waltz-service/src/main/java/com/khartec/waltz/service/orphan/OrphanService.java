package com.khartec.waltz.service.orphan;

import com.khartec.waltz.data.orphan.OrphanDao;
import com.khartec.waltz.model.EntityReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class OrphanService {

    private final OrphanDao orphanDao;

    @Autowired
    public OrphanService(OrphanDao orphanDao) {
        checkNotNull(orphanDao, "orphanDao cannot be null");
        this.orphanDao = orphanDao;
    }


    public Collection<EntityReference> findApplicationsWithNonExistingOrgUnit() {
        return orphanDao.findApplicationsWithNonExistingOrgUnit();
    }

}
