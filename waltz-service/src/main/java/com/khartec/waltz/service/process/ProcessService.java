/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.process;

import com.khartec.waltz.data.capability.CapabilityDao;
import com.khartec.waltz.data.process.ProcessDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.process.Process;
import com.khartec.waltz.schema.tables.EntityRelationship;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;

@Service
public class ProcessService {
    
    private final ProcessDao processDao;
    private final CapabilityDao capabilityDao;
    private final DSLContext dsl;


    @Autowired
    public ProcessService(ProcessDao processDao,
                          CapabilityDao capabilityDao,
                          DSLContext dsl) {
        checkNotNull(processDao, "processDao cannot be null");
        checkNotNull(capabilityDao, "capabilityDao cannot be null");
        checkNotNull(dsl, "dsl cannot be null");

        this.processDao = processDao;
        this.capabilityDao = capabilityDao;
        this.dsl = dsl;
    }


    public Process getById(long id) {
        return processDao.getById(id);
    }


    public List<Process> findAll() {
        return processDao.findAll();
    }

    public Collection<Capability> findSupportingCapabilitiesRoute(long id) {
        EntityRelationship rel = ENTITY_RELATIONSHIP.as("rel");

        SelectConditionStep<Record1<Long>> selector = dsl.select(rel.ID_A)
                .from(rel)
                .where(rel.KIND_A.eq(EntityKind.CAPABILITY.name()))
                .and(rel.RELATIONSHIP.eq(RelationshipKind.SUPPORTS.name()))
                .and(rel.KIND_B.eq(EntityKind.PROCESS.name()))
                .and(rel.ID_B.eq(id));

        return capabilityDao.findByIdSelector(selector);
    }

    public Collection<Process> findForCapability(long id) {
        return processDao.findForCapability(id);
    }

    public Collection<Process> findForApplication(long id) {
        return processDao.findForApplication(id);
    }
}
