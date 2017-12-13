/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
