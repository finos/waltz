package com.khartec.waltz.service.orphan;

import com.khartec.waltz.data.orphan.OrphanDao;
import com.khartec.waltz.model.orphan.OrphanRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class OrphanService {

    private final OrphanDao orphanDao;

    @Autowired
    public OrphanService(OrphanDao orphanDao) {
        checkNotNull(orphanDao, "orphanDao cannot be null");
        this.orphanDao = orphanDao;
    }


    public Collection<OrphanRelationship> findApplicationsWithNonExistingOrgUnit() {
        return orphanDao.findApplicationsWithNonExistentOrgUnit();
    }


    public Collection<OrphanRelationship> findOrphanApplicationCapabilities() {
        return orphanDao.findOrphanApplicationCapabilities();
    }


    public Collection<OrphanRelationship> findOrphanAuthoritativeSourceByOrgUnit() {
        return orphanDao.findOrphanAuthoritativeSourceByOrgUnit();
    }


    public Collection<OrphanRelationship> findOrphanAuthoritativeSourceByApp() {
        return orphanDao.findOrphanAuthoritativeSourceByApp();
    }


    public Collection<OrphanRelationship> findOrphanAuthoritiveSourceByDataType() {
        return orphanDao.findOrphanAuthoritiveSourceByDataType();
    }


    public List<OrphanRelationship> findOrphanChangeInitiatives() {
        return orphanDao.findOrphanChangeInitiatives();
    }


    public List<OrphanRelationship> findOrphanLogicalDataFlows() {
        return orphanDao.findOrphanLogicalDataFlows();
    }
}
