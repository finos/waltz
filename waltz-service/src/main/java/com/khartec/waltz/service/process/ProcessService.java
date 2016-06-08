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
