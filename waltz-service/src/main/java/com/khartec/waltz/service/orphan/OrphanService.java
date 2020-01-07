/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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


    public Collection<OrphanRelationship> findOrphanMeasurableRatings() {
        return orphanDao.findOrphanMeasurableRatings();
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


    public List<OrphanRelationship> findOrphanPhysicalFlows() {
        return orphanDao.findOrphanPhysicalFlows();
    }


    public List<OrphanRelationship> findOrphanAttestatations() {
        return orphanDao.findOrphanAttestatations();
    }

}
